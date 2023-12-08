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

import java.util.List;
import java.util.Map;

import org.springframework.util.StringUtils;

import io.pivotal.cfenv.core.CfCredentials;
import io.pivotal.cfenv.core.CfService;
import io.pivotal.cfenv.core.UriInfo;

/**
 * Retrieve AMQP properties from {@link CfCredentials} and set {@literal spring.rabbitmq}
 * Boot properties.
 *
 * @author David Turanski
 * @author Scott Frederick
 **/
public class AmqpCfEnvProcessor implements CfEnvProcessor {

	private final static String[] amqpSchemes = new String[] { "amqp", "amqps" };

	@Override
	public boolean accept(CfService service) {
		boolean serviceIsBound = service.existsByTagIgnoreCase("rabbitmq", "amqp") ||
				service.existsByLabelStartsWith("rabbitmq") ||
				service.existsByLabelStartsWith("cloudamqp") ||
				service.existsByUriSchemeStartsWith(amqpSchemes) ||
				service.existsByCredentialsContainsUriField(amqpSchemes);
		if (serviceIsBound) {
			ConnectorLibraryDetector.assertNoConnectorLibrary();
		}
		return serviceIsBound;
	}

	@Override
	public void process(CfCredentials cfCredentials, Map<String, Object> properties) {
		String uri = cfCredentials.getUri(amqpSchemes);

		if (uri == null) {
			properties.put("spring.rabbitmq.host", cfCredentials.getHost());
			properties.put("spring.rabbitmq.password", cfCredentials.getPassword());
			properties.put("spring.rabbitmq.username", cfCredentials.getUsername());
		}
		else {
			UriInfo uriInfo = new UriInfo(uri);
			properties.put("spring.rabbitmq.host", uriInfo.getHost());
			properties.put("spring.rabbitmq.password", uriInfo.getPassword());
			properties.put("spring.rabbitmq.username", cfCredentials.getUsername());
			if (uriInfo.getScheme().equals("amqps")) {
				properties.put("spring.rabbitmq.ssl.enabled", "true");
				properties.put("spring.rabbitmq.port", uriInfo.getPort() != -1 ? Integer.toString(uriInfo.getPort()) : "5671");
				properties.put("spring.rabbitmq.virtualHost", uriInfo.getPath());
			} else {
				populateAddress(cfCredentials, properties, uri);
			}
		}

		if (cfCredentials.getMap().get("vhost") != null) {
			properties.put("spring.rabbitmq.virtualHost", cfCredentials.getMap().get("vhost"));
		}
	}

	public void populateAddress(CfCredentials cfCredentials, Map<String, Object> properties, String uri) {
		if (cfCredentials.getMap().get("uris") != null) {
			properties.put("spring.rabbitmq.addresses", StringUtils.collectionToCommaDelimitedString(
					(List<String>) cfCredentials.getMap().get("uris")));
		}
		else if (uri != null) {
			properties.put("spring.rabbitmq.addresses", uri);
		}
	}

	@Override
	public CfEnvProcessorProperties getProperties() {
		return CfEnvProcessorProperties.builder()
				.propertyPrefixes("spring.rabbitmq")
				.serviceName("Rabbit")
				.build();
	}
}
