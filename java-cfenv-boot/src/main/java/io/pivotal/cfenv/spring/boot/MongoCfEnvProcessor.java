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
 * Retrieve MongoDB properties from {@link CfCredentials} and set
 * {@literal spring.mongodb} Boot properties.
 *
 * @author Mark Pollack
 * @author Scott Frederick
 */
public class MongoCfEnvProcessor implements CfEnvProcessor {

	private static String mongoScheme = "mongodb";

	@Override
	public boolean accept(CfService service) {
		boolean serviceIsBound = service.existsByTagIgnoreCase("mongodb") ||
				service.existsByLabelStartsWith("mongolab") ||
				service.existsByUriSchemeStartsWith(mongoScheme) ||
				service.existsByCredentialsContainsUriField(mongoScheme);
		if (serviceIsBound) {
			ConnectorLibraryDetector.assertNoConnectorLibrary();
		}
		return serviceIsBound;
	}

	@Override
	public void process(CfCredentials cfCredentials, Map<String, Object> properties) {
		properties.put("spring.data.mongodb.uri", cfCredentials.getUri(mongoScheme));
	}

	@Override
	public CfEnvProcessorProperties getProperties() {
		return CfEnvProcessorProperties.builder()
				.propertyPrefixes("spring.data.mongodb")
				.serviceName("MongoDB")
				.build();
	}

}
