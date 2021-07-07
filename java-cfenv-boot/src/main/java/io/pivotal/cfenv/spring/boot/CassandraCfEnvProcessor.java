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

import java.util.ArrayList;
import java.util.Map;

import org.springframework.util.StringUtils;

import io.pivotal.cfenv.core.CfCredentials;
import io.pivotal.cfenv.core.CfService;

/**
 * @author Mark Pollack
 */
public class CassandraCfEnvProcessor implements CfEnvProcessor {

	@Override
	public boolean accept(CfService service) {
		boolean serviceIsBound = service.existsByTagIgnoreCase("cassandra") &&
				cassandraCredentialsPresent(service.getCredentials().getMap());
		if (serviceIsBound) {
			ConnectorLibraryDetector.assertNoConnectorLibrary();
		}
		return serviceIsBound;
	}

	@Override
	public void process(CfCredentials cfCredentials, Map<String, Object> properties) {
		properties.put("spring.data.cassandra.username", cfCredentials.getUsername());
		properties.put("spring.data.cassandra.password", cfCredentials.getPassword());
		properties.put("spring.data.cassandra.port", cfCredentials.getMap().get("cqlsh_port"));
		ArrayList<String> contactPoints = (ArrayList<String>) cfCredentials.getMap().get("node_ips");
		properties.put("spring.data.cassandra.contact-points",
				StringUtils.collectionToCommaDelimitedString(contactPoints));

	}

	private boolean cassandraCredentialsPresent(Map<String, Object> credentials) {
		return credentials != null &&
				credentials.containsKey("cqlsh_port") &&
				credentials.containsKey("node_ips");
	}

	@Override
	public CfEnvProcessorProperties getProperties() {
		return CfEnvProcessorProperties.builder().propertyPrefixes("spring.data.cassandra")
				.serviceName("Cassandra").build();
	}
}
