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

import java.util.Map;

import io.pivotal.cfenv.core.CfCredentials;
import io.pivotal.cfenv.core.CfService;

/**
 * Implementations will be called by {@link io.pivotal.cfenv.spring.boot.CfEnvironmentPostProcessor} to
 * find a specific type of service and set Spring Boot properties.
 *
 * Implementation should be registered using the {@code resources/META-INF/spring.factories} property file.
 *
 * @author Scott Frederick
 * @author Mark Pollack
 */
public interface CfEnvProcessor extends CfEnvEnabledProcessor {

	/**
	 * Determine if a service is supported by this processor.
	 *
	 * @param service a service to inspect
	 * @return {@code true} if the service matches criteria; {@code false} otherwise
	 */
	boolean accept(CfService service);

	/**
	 * Given the credentials of the single matching service, set the property values that will be used to
	 * create the MapPropertySource.
	 *
	 * @param cfCredentials Credentials of the single matching service
	 * @param properties map to set Spring Boot properties
	 */
	void process(CfCredentials cfCredentials, Map<String, Object> properties);

	/**
	 * Properties describing a CfEnvProcessor, mainly used for better logging messages in {@link CfEnvProcessor}.
	 * 
	 * @return processor properties
	 */
	CfEnvProcessorProperties getProperties();
}
