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
public class CassandraCfEnvProcessor {

	public final static class Boot2 implements CfEnvProcessor {

		private static final int BOOT_VERSION = 2;
		private static final String PREFIX = "spring.data.cassandra";

		@Override
		public boolean accept(CfService service) {
			if (!SpringBootVersionResolver.isBootMajorVersionEnabled(BOOT_VERSION)) {
				return false;
			}
			return CassandraCfEnvProcessor.accept(service);
		}

		@Override
		public void process(CfCredentials cfCredentials, Map<String, Object> properties) {
			CassandraCfEnvProcessor.process(cfCredentials, properties, PREFIX);
		}

		@Override
		public CfEnvProcessorProperties getProperties() {
			return CassandraCfEnvProcessor.getProperties(PREFIX);
		}
	}

	/**
	 * This is a special case for Boot 3.
	 */
	public final static class Boot3 extends SpringBootVersionResolver implements CfEnvProcessor {

		private static final int BOOT_VERSION = 3;
		private static final String PREFIX = "spring.cassandra";

		@Override
		public boolean accept(CfService service) {
			if (!SpringBootVersionResolver.isBootMajorVersionEnabled(BOOT_VERSION)) {
				return false;
			}
			return CassandraCfEnvProcessor.accept(service);
		}

		@Override
		public void process(CfCredentials cfCredentials, Map<String, Object> properties) {
			CassandraCfEnvProcessor.process(cfCredentials, properties, PREFIX);
		}

		@Override
		public CfEnvProcessorProperties getProperties() {
			return CassandraCfEnvProcessor.getProperties(PREFIX);
		}
	}

	static boolean accept(CfService service) {
		boolean serviceIsBound = service.existsByTagIgnoreCase("cassandra") &&
				cassandraCredentialsPresent(service.getCredentials().getMap());
		if (serviceIsBound) {
			ConnectorLibraryDetector.assertNoConnectorLibrary();
		}
		return serviceIsBound;
	}

	static void process(CfCredentials cfCredentials, Map<String, Object> properties, String prefix) {
		properties.put(prefix + ".username", cfCredentials.getUsername());
		properties.put(prefix + ".password", cfCredentials.getPassword());
		properties.put(prefix + ".port", cfCredentials.getMap().get("cqlsh_port"));
		ArrayList<String> contactPoints = (ArrayList<String>) cfCredentials.getMap().get("node_ips");
		properties.put(prefix + ".contact-points",
				StringUtils.collectionToCommaDelimitedString(contactPoints));

	}

	static boolean cassandraCredentialsPresent(Map<String, Object> credentials) {
		return credentials != null &&
				credentials.containsKey("cqlsh_port") &&
				credentials.containsKey("node_ips");
	}

	static CfEnvProcessorProperties getProperties(String prefix) {
		return CfEnvProcessorProperties.builder().propertyPrefixes(prefix)
				.serviceName("Cassandra").build();
	}
}
