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

import java.util.Map;
import java.util.Optional;

import io.pivotal.cfenv.core.CfCredentials;
import io.pivotal.cfenv.core.CfService;
import io.pivotal.cfenv.core.UriInfo;

/**
 * Retrieve Redis properties from {@link CfCredentials} and set {@literal spring.redis}
 * Boot properties.
 *
 * @author Mark Pollack
 * @author Scott Frederick
 */
public class RedisCfEnvProcessor {

	private final static String[] redisSchemes = { "redis", "rediss" };

	public final static class Boot2 implements CfEnvProcessor {

		private static final int BOOT_VERSION = 2;
		private static final String PREFIX = "spring.redis";

		@Override
		public boolean accept(CfService service) {
			if (!SpringBootVersionResolver.isBootMajorVersionEnabled(BOOT_VERSION)) {
				return false;
			}
			return RedisCfEnvProcessor.accept(service);
		}

		@Override
		public void process(CfCredentials cfCredentials, Map<String, Object> properties) {
			RedisCfEnvProcessor.process(cfCredentials, properties, PREFIX);
		}

		@Override
		public CfEnvProcessorProperties getProperties() {
			return RedisCfEnvProcessor.getProperties(PREFIX);
		}
	}

	/**
	 * This is a special case for Boot 3.
	 */
	public final static class Boot3 extends SpringBootVersionResolver implements CfEnvProcessor {

		private static final int BOOT_VERSION = 3;
		private static final String PREFIX = "spring.data.redis";

		@Override
		public boolean accept(CfService service) {
			if (!SpringBootVersionResolver.isBootMajorVersionEnabled(BOOT_VERSION)) {
				return false;
			}
			return RedisCfEnvProcessor.accept(service);
		}

		@Override
		public void process(CfCredentials cfCredentials, Map<String, Object> properties) {
			RedisCfEnvProcessor.process(cfCredentials, properties, PREFIX);
		}

		@Override
		public CfEnvProcessorProperties getProperties() {
			return RedisCfEnvProcessor.getProperties(PREFIX);
		}
	}

	public static boolean accept(CfService service) {
		boolean serviceIsBound = service.existsByTagIgnoreCase("redis") ||
				service.existsByLabelStartsWith("rediscloud") ||
				service.existsByUriSchemeStartsWith(redisSchemes) ||
				service.existsByCredentialsContainsUriField(redisSchemes);
		if (serviceIsBound) {
			ConnectorLibraryDetector.assertNoConnectorLibrary();
		}
		return serviceIsBound;
	}

	private static void process(CfCredentials cfCredentials, Map<String, Object> properties, String prefix) {
		String uri = cfCredentials.getUri(redisSchemes);

		if (uri == null) {
			properties.put(prefix + ".host", cfCredentials.getHost());
			properties.put(prefix + ".password", cfCredentials.getPassword());

			Optional<String> tlsPort = Optional.ofNullable(cfCredentials.getString("tls_port"));
			if (tlsPort.isPresent()) {
				properties.put(prefix + ".port", tlsPort.get());
				properties.put(prefix + ".ssl", "true");
			}
			else {
				properties.put(prefix + ".port", cfCredentials.getPort());
			}
		}
		else {
			UriInfo uriInfo = new UriInfo(uri);
			properties.put(prefix + ".host", uriInfo.getHost());
			properties.put(prefix + ".port", uriInfo.getPort());
			properties.put(prefix + ".password", uriInfo.getPassword());
			if (uriInfo.getScheme().equals("rediss")) {
				properties.put(prefix + ".ssl", "true");
			}
		}
	}

	static CfEnvProcessorProperties getProperties(String prefix) {
		return CfEnvProcessorProperties.builder()
				.propertyPrefixes(prefix)
				.serviceName("Redis")
				.build();
	}

}
