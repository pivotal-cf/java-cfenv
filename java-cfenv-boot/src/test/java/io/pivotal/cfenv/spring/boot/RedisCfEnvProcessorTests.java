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

import org.junit.Test;

import org.springframework.core.env.Environment;

import io.pivotal.cfenv.test.AbstractCfEnvTests;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Mark Pollack
 * @author David Turanski
 */
public class RedisCfEnvProcessorTests extends AbstractCfEnvTests {
	private final static int TLS_PORT = 11111;
	private static final String SPRING_DATA_REDIS = "spring.data.redis";

	public static void commonAssertions(Environment environment, String SPRING_DATA_REDIS) {
		assertThat(environment.getProperty(SPRING_DATA_REDIS + ".host")).isEqualTo(hostname);
		assertThat(environment.getProperty(SPRING_DATA_REDIS + ".port")).isEqualTo(String.valueOf(port));
		assertThat(environment.getProperty(SPRING_DATA_REDIS + ".password")).isEqualTo(password);
	}

	@Test
	public void testRedisBootPropertiesWithoutUriInCredentials() {
		String payload = payloadBuilder("test-redis-info.json").payload();
		mockVcapServices(getServicesPayload(payload));

		commonAssertions(getEnvironment(), SPRING_DATA_REDIS);
	}

	@Test
	public void testRedisBootPropertiesWithTLSEnabledInCredentials() {
		String payload = payloadBuilder("test-redis-info-with-tls-port.json")
				.withTLSPort(TLS_PORT)
				.payload();
		mockVcapServices(getServicesPayload(payload));

		Environment environment = getEnvironment();

		assertThat(environment.getProperty(SPRING_DATA_REDIS + ".host")).isEqualTo(hostname);
		assertThat(environment.getProperty(SPRING_DATA_REDIS + ".port")).isEqualTo(String.valueOf(TLS_PORT));
		assertThat(environment.getProperty(SPRING_DATA_REDIS + ".password")).isEqualTo(password);
		assertThat(environment.getProperty(SPRING_DATA_REDIS + ".ssl")).isEqualTo("true");
	}

	@Test
	public void testRedisSSlBootPropertiesWithUriInCredentials() {
		String payload = payloadBuilder("test-redis-info-no-label-no-tags-secure.json").payload();
		mockVcapServices(getServicesPayload(payload));

		Environment environment = getEnvironment();
		commonAssertions(getEnvironment(), SPRING_DATA_REDIS);
		assertThat(environment.getProperty(SPRING_DATA_REDIS+".ssl")).isEqualTo("true");
	}

	@Test
	public void testNoCredentials() {
		String payload = "{}";
		mockVcapServices(getServicesPayload(payload));

		Environment environment = getEnvironment();

		assertThat(environment.getProperty(SPRING_DATA_REDIS + ".host")).isNull();
		assertThat(environment.getProperty(SPRING_DATA_REDIS + ".port")).isNull();
		assertThat(environment.getProperty(SPRING_DATA_REDIS + ".password")).isNull();
		assertThat(environment.getProperty(SPRING_DATA_REDIS + ".ssl")).isNull();
	}

	@Test
	public void testGetProperties() {
		assertThat(new RedisCfEnvProcessor().getProperties().getPropertyPrefixes()).isEqualTo(SPRING_DATA_REDIS);
		assertThat(new RedisCfEnvProcessor().getProperties().getServiceName()).isEqualTo("Redis");
	}

	private RedisFilePayloadBuilder payloadBuilder(String filename) {
		return new RedisFilePayloadBuilder(filename)
				.withServiceName("redis-1")
				.withHostname(hostname)
				.withPassword(password)
				.withPort(port)
				.withName("redis-db");
	}

	private class RedisFilePayloadBuilder {
		private String payload;
		private String serviceName;
		private String hostname;
		private Integer port;
		private Integer tlsPort;
		private String password;
		private String name;

		RedisFilePayloadBuilder(String filename) {
			this.payload = readTestDataFile(filename);
		}

		RedisFilePayloadBuilder withServiceName(String serviceName) {
			this.serviceName = serviceName;
			return this;
		}

		RedisFilePayloadBuilder withHostname(String hostname) {
			this.hostname = hostname;
			return this;
		}

		RedisFilePayloadBuilder withPort(int port) {
			this.port = port;
			return this;
		}

		RedisFilePayloadBuilder withPassword(String password) {
			this.password = password;
			return this;
		}

		RedisFilePayloadBuilder withTLSPort(int tlsPort) {
			this.tlsPort = tlsPort;
			return this;
		}

		RedisFilePayloadBuilder withName(String name) {
			this.name = name;
			return this;
		}

		String payload() {
			return payload.replace("$serviceName", serviceName)
					.replace("$hostname", hostname)
					.replace("$port", String.valueOf(port))
					.replace("$password", password)
					.replace("$tls_port", String.valueOf(tlsPort))
					.replace("$name", name);
		}

	}
}
