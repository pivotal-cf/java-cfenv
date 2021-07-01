/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.pivotal.cfenv.spring.boot;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.cloud.CloudPlatform;
import org.springframework.boot.context.config.ConfigFileApplicationListener;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.env.CommandLinePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.stereotype.Component;

import io.pivotal.cfenv.core.CfEnv;
import io.pivotal.cfenv.core.CfService;

/**
 * An EnvironmentPostProcessor that iterates over {@link CfEnvProcessor} implementations to contribute
 * Spring Boot properties for bound Cloud Foundry Services.
 *
 * Implementation of {@link CfEnvProcessor }should be registered using the
 * {@code resources/META-INF/spring.factories} property file.
 *
 * @author Mark Pollack
 * @author David Turanski
 */
@Component
public class CfEnvironmentPostProcessor implements
	CfServiceEnablingEnvironmentPostProcessor, Ordered, ApplicationListener<ApplicationEvent> {

	private static final DeferredLog DEFERRED_LOG = new DeferredLog();

	private static int invocationCount;

	// After ConfigFileApplicationListener so values from files can be used here
	private int order = ConfigFileApplicationListener.DEFAULT_ORDER + 1;

	public CfEnvironmentPostProcessor() {
	}

	@Override
	public int getOrder() {
		return this.order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment,
									   SpringApplication application) {

		increaseInvocationCount();
		if (CloudPlatform.CLOUD_FOUNDRY.isActive(environment)) {
			CfEnv cfEnv = new CfEnv();
			List<CfService> allServices = cfEnv.findAllServices();

			List<CfEnvProcessor> cfEnvProcessors = SpringFactoriesLoader
					.loadFactories(CfEnvProcessor.class, getClass().getClassLoader());
			AnnotationAwareOrderComparator.sort(cfEnvProcessors);

			for (CfEnvProcessor processor : cfEnvProcessors) {
				List<CfService> cfServices = allServices.stream()
						.filter(processor::accept)
						.filter(cfService-> this.isEnabled(cfService, environment))
						.collect(Collectors.toList());

				if (cfServices.size() > 1) {
					List<String> names = cfServices.stream().map(CfService::getName).collect(Collectors.toList());
					DEFERRED_LOG.warn(String.format("Skipping auto-configuration: No unique %s service found. Found services named [%s]",
							processor.getProperties().getServiceName(),
							String.join(",", names)
					));
				} else if (cfServices.size() == 1) {
					CfService cfService = cfServices.get(0);
					Map<String, Object> properties = new LinkedHashMap<>();

					processor.process(cfService.getCredentials(), properties);

					MutablePropertySources propertySources = environment.getPropertySources();

					if (propertySources.contains(
							CommandLinePropertySource.COMMAND_LINE_PROPERTY_SOURCE_NAME)) {
						propertySources.addAfter(
								CommandLinePropertySource.COMMAND_LINE_PROPERTY_SOURCE_NAME,
								new MapPropertySource(processor.getClass().getSimpleName(), properties));
					} else {
						propertySources.addFirst(
								new MapPropertySource(processor.getClass().getSimpleName(), properties));
					}

					if (invocationCount == 1) {
						DEFERRED_LOG.info(
								"Setting " + processor.getProperties().getPropertyPrefixes() +
										" properties from bound service ["
										+ cfService.getName() + "] using " + processor.getClass().getName());
					}
				}
			}
		} else {
			if (invocationCount == 1) {
				DEFERRED_LOG.debug(
						"Not setting properties, Cloud Foundry Environment no detected");
			}
		}
	}

	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		if (event instanceof ApplicationPreparedEvent) {
			DEFERRED_LOG
					.switchTo(CfEnvironmentPostProcessor.class);
		}
	}

	/**
	 * EnvironmentPostProcessors can end up getting called twice due to spring-cloud-commons
	 * functionality
	 */
	protected void increaseInvocationCount() {
		synchronized (this) {
			invocationCount++;
		}
	}

}
