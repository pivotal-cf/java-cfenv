/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.pivotal.cfenv.spring.boot;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.cloud.CloudPlatform;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.env.CommandLinePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;

import io.pivotal.cfenv.jdbc.CfJdbcEnv;
import io.pivotal.cfenv.jdbc.CfJdbcService;

/**
 * @author Mark Pollack
 * @author David Turanski
 * @author Greg Meyer
 */
public class CfDataSourceEnvironmentPostProcessor implements CfServiceEnablingEnvironmentPostProcessor,
		Ordered, ApplicationListener<ApplicationEvent> {

	private static final DeferredLog DEFERRED_LOG = new DeferredLog();

	private static int invocationCount;

	// After ConfigFileApplicationListener so values from files can be used here
	private int order = ConfigDataEnvironmentPostProcessor.ORDER + 1;

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
			CfJdbcEnv cfJdbcEnv = new CfJdbcEnv();
			CfJdbcService cfJdbcService;
			try {
				cfJdbcService = cfJdbcEnv.findJdbcService();
				cfJdbcService = this.isEnabled(cfJdbcService, environment) ? cfJdbcService : null;
			} catch (Exception e) {

				List<CfJdbcService> jdbcServices = cfJdbcEnv.findJdbcServices().stream()
						.filter(service -> this.isEnabled(service, environment))
						.toList();

				if (jdbcServices.size() > 1) {
					if (invocationCount == 1) {
						DEFERRED_LOG.debug(
							"Skipping execution of CfDataSourceEnvironmentPostProcessor. "
								+ e.getMessage());
					}
					return;
				}

				cfJdbcService = jdbcServices.size() == 1 ? jdbcServices.get(0) : null;
			}
			if (cfJdbcService != null) {
				ConnectorLibraryDetector.assertNoConnectorLibrary();
				Map<String, Object> properties = new LinkedHashMap<>();
				properties.put("spring.datasource.url", cfJdbcService.getJdbcUrl());
				properties.put("spring.datasource.username", cfJdbcService.getUsername());
				properties.put("spring.datasource.password", cfJdbcService.getPassword());
				Object driverClassName = cfJdbcService.getDriverClassName();
				if (driverClassName != null) {
					properties.put("spring.datasource.driver-class-name", driverClassName);
				}

				/* R2DBC processing
				 * Split query param options and URL into two string
				 * and move options to spring.r2dbc.properties.<option>
				 */

				String[] splitJDBCUrl = cfJdbcService.getJdbcUrl().split("\\?");

				String r2dbcUrl = splitJDBCUrl[0].replaceFirst("jdbc:", "r2dbc:");

				properties.put("spring.r2dbc.url", r2dbcUrl);
				properties.put("spring.r2dbc.username", cfJdbcService.getUsername());
				properties.put("spring.r2dbc.password", cfJdbcService.getPassword());

				if (splitJDBCUrl.length == 2) {
					Map<String, String> queryOptions = parseQueryString(splitJDBCUrl[1]);

					if (queryOptions.size() > 0) {
						queryOptions.forEach((key, value) -> {

							switch (key) {
								case "enabledTLSProtocols":
									properties.put("spring.r2dbc.properties.tlsVersion", value);
									break;
								default:
									properties.put(String.format("spring.r2dbc.properties.%s", key), value);
							}
						});
					}
				}

				MutablePropertySources propertySources = environment.getPropertySources();
				if (propertySources.contains(
						CommandLinePropertySource.COMMAND_LINE_PROPERTY_SOURCE_NAME)) {
					propertySources.addAfter(
							CommandLinePropertySource.COMMAND_LINE_PROPERTY_SOURCE_NAME,
							new MapPropertySource("cfenvjdbc", properties));
				}
				else {
					propertySources
							.addFirst(new MapPropertySource("cfenvjdbc", properties));
				}
				if (invocationCount == 1) {
					DEFERRED_LOG.info(
							"Setting spring.datasource properties from bound service ["
									+ cfJdbcService.getName() + "]");
				}
			}
		}
		else {
			DEFERRED_LOG.debug(
					"Not setting spring.datasource.url, not in Cloud Foundry Environment");
		}
	}

	private Map<String, String> parseQueryString(String queryParams) {
		
		if (queryParams == null || queryParams.equals(""))
			return Collections.emptyMap(); 
		
		Map<String, String> retVal = new HashMap<>();
		
		String[] options = queryParams.split("&");
		for (String option : options) {
			
			String[] keyValue = option.split("=");
			if (keyValue.length != 2 || keyValue[0].length() == 0 || keyValue[1].length() == 0) {
				continue;
			}
            
 			retVal.put(keyValue[0], keyValue[1]);
		}
		
		return retVal;
	}
	
	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		if (event instanceof ApplicationPreparedEvent) {
			DEFERRED_LOG.switchTo(CfDataSourceEnvironmentPostProcessor.class);
		}

	}

	/**
	 * EnvironmentPostProcessors can end up getting called twice due to spring-cloud-commons
	 * functionality
	 */
	private void increaseInvocationCount() {
		synchronized (this) {
			invocationCount++;
		}
	}

}
