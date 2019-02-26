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


import java.util.List;
import java.util.Map;

import io.pivotal.cfenv.core.CfCredentials;
import io.pivotal.cfenv.core.CfEnv;
import io.pivotal.cfenv.core.CfService;


/**
 * Implementations will be called by {@see io.pivotal.cfenv.spring.boot.CfEnvironmentPostProcessor} to
 * find a specific type of service and set Spring Boot properties.
 *
 * Implementation should be registered using the {@code resources/META-INF/spring.factories} property file.
 *
 * @author Scott Frederick
 * @author Mark Pollack
 */
public interface CfEnvProcessor {

	/**
	 * Find the services that match a certain criteria.
	 * @param cfEnv The Cloud Foundry environment object
	 * @return list of services matching your custom critiera
	 */
	List<CfService> findServices(CfEnv cfEnv);

	/**
	 * Given the credentials of the single matching service, set the property values that will be used to
	 * create the {@see MapPropertySource}.
	 * @param cfCredentials Credentials of the single matching service
	 * @param properties map to set Spring Boot properties
	 */
	void process(CfCredentials cfCredentials, Map<String, Object> properties);


	/**
	 * The name of the {@see PropertySource} to be used when adding to the list of {@see MapPropertySource}s.
	 * @return name of the property source
	 */
	String getPropertySourceName();

	/**
	 * A string containing the values of property prefixes that will be set in the {@code process} method.  Used
	 * for logging purposes.
	 * @return property prefix values set in the {@code process} method
	 */
	String getPropertyPrefixes();

	/**
	 * A name that describes the service being processed, eg. 'Redis', 'MongoDB'.  Used for logging purposes.
	 * @return name that describes the service being processed
	 */
	String getServiceName();
}
