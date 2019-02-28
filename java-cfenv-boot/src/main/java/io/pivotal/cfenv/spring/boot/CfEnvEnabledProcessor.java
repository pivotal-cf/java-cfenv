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

import io.pivotal.cfenv.core.CfService;

import org.springframework.core.env.Environment;

/**
 * Implementations, called by {@see io.pivotal.cfenv.spring.boot.CfEnvironmentPostProcessor}
 * can disable a service by setting a Spring boot property
 * {@code io.pivotal.cfenv.service.<serviceName>.enabled=false}.
 *
 * @author David Turanski
 */
public interface CfEnvEnabledProcessor {
	/**
	 * Determine if a service is enabled by this processor.
	 * @param service a service to inspect
	 * @return {@code true} if the service is enabled; {@code false} otherwise
	 */
	default boolean isEnabled(CfService service, Environment environment) {
		return Boolean.valueOf(
			environment.getProperty(String.format("io.pivotal.cfenv.service.%s.enabled", service.getName()),
				"true"));
	}
}
