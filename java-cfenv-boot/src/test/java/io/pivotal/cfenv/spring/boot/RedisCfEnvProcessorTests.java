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
import java.util.List;

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
		assertThat(environment.getProperty(SPRING_DATA_REDIS + ".ssl.enabled")).isEqualTo("true");
	}

	@Test
	public void testRedisSSlBootPropertiesWithUriInCredentials() {
		String payload = payloadBuilder("test-redis-info-no-label-no-tags-secure.json").payload();
		mockVcapServices(getServicesPayload(payload));

		Environment environment = getEnvironment();
		commonAssertions(getEnvironment(), SPRING_DATA_REDIS);
		assertThat(environment.getProperty(SPRING_DATA_REDIS+".ssl.enabled")).isEqualTo("true");
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
	public void testRedisBootPropertiesSentinelSetup() {
		String payload = new RedisFileSentinelPayloadBuilder("test-redis-info-sentinel.json")
				.withServiceName("redis-ha-1")
				.withName("redis-db")
				.withPassword("password")
				.withRedisPort(port)
				.withSentinelMaster("my-master")
				.withSentinelPassword("sentinel-password")
				.withSentinel("sent1", 26379)
				.withSentinel("sent2", 26380)
				.payload();

		mockVcapServices(getServicesPayload(payload));

		Environment environment = getEnvironment();

		assertThat(environment.getProperty(SPRING_DATA_REDIS + ".password")).isEqualTo("password");
		assertThat(environment.getProperty(SPRING_DATA_REDIS + ".sentinel.master")).isEqualTo("my-master");
		assertThat(environment.getProperty(SPRING_DATA_REDIS + ".sentinel.nodes")).isEqualTo("sent1:26379,sent2:26380");
		assertThat(environment.getProperty(SPRING_DATA_REDIS + ".sentinel.username")).isEqualTo("");
		assertThat(environment.getProperty(SPRING_DATA_REDIS + ".sentinel.password")).isEqualTo("sentinel-password");
		assertThat(environment.getProperty(SPRING_DATA_REDIS + ".ssl.enabled")).isEqualTo("false");
	}

	@Test
	public void testRedisBootPropertiesSentinelTlsSetup() {
		String payload = new RedisFileSentinelPayloadBuilder("test-redis-info-sentinel-tls.json")
				.withServiceName("redis-ha-1")
				.withName("redis-db")
				.withPassword("password")
				.withRedisPort(port)
				.withSentinelMaster("my-master")
				.withSentinelPassword("sentinel-password")
				.withSentinelTls("sent1", 26379, 26380)
				.withSentinelTls("sent2", 26380, 26381)
				.payload();

		mockVcapServices(getServicesPayload(payload));

		Environment environment = getEnvironment();

		assertThat(environment.getProperty(SPRING_DATA_REDIS + ".password")).isEqualTo("password");
		assertThat(environment.getProperty(SPRING_DATA_REDIS + ".sentinel.master")).isEqualTo("my-master");
		assertThat(environment.getProperty(SPRING_DATA_REDIS + ".sentinel.nodes")).isEqualTo("sent1:26380,sent2:26381");
		assertThat(environment.getProperty(SPRING_DATA_REDIS + ".sentinel.username")).isEqualTo("");
		assertThat(environment.getProperty(SPRING_DATA_REDIS + ".sentinel.password")).isEqualTo("sentinel-password");
		assertThat(environment.getProperty(SPRING_DATA_REDIS + ".ssl.enabled")).isEqualTo("true");
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

	private class RedisFileSentinelPayloadBuilder {

		private String payload;
		private String serviceName;
		private String name;
		private Integer redisPort;
		private String password;
		private String sentinelPassword;
		private String sentinelMaster;
		private List<SentinelDefinition> sentinels;

		RedisFileSentinelPayloadBuilder(String filename) {
			this.payload = readTestDataFile(filename);
			this.sentinels = new ArrayList<>();
		}

		RedisFileSentinelPayloadBuilder withServiceName(String serviceName) {
			this.serviceName = serviceName;
			return this;
		}

		RedisFileSentinelPayloadBuilder withName(String name) {
			this.name = name;
			return this;
		}

		RedisFileSentinelPayloadBuilder withRedisPort(int redisPort) {
			this.redisPort = redisPort;
			return this;
		}

		RedisFileSentinelPayloadBuilder withPassword(String password) {
			this.password = password;
			return this;
		}

		RedisFileSentinelPayloadBuilder withSentinelPassword(String sentinelPassword) {
			this.sentinelPassword = sentinelPassword;
			return this;
		}

		RedisFileSentinelPayloadBuilder withSentinel(String host, int port) {
			return withSentinelTls(host, port, -1);
		}

		RedisFileSentinelPayloadBuilder withSentinelTls(String host, int port, int tlsPort) {
			this.sentinels.add(new SentinelDefinition(host, port, tlsPort));
			return this;
		}

		RedisFileSentinelPayloadBuilder withSentinelMaster(String sentinelMaster) {
			this.sentinelMaster = sentinelMaster;
			return this;
		}

		String payload() {
			var result = payload.replace("$serviceName", serviceName)
					.replace("$redisPort", String.valueOf(redisPort))
					.replace("$redisPassword", password)
					.replace("$sentinelPassword", sentinelPassword)
					.replace("$sentinelMaster", sentinelMaster)
					.replace("$name", name);
			for (int i = 0; i < this.sentinels.size(); i++) {
				var sentinelDefinition = this.sentinels.get(i);

				var hostKey = "$sentinel" + (i+1) + "-hostname";
				var portKey = "$sentinel" + (i+1) + "-port";
				var tlsPortKey = "$sentinel" + (i+1) + "-tlsPort";
				result = result.replace(hostKey, sentinelDefinition.host);
				result = result.replace(portKey, Integer.toString(sentinelDefinition.port));
				result = result.replace(tlsPortKey, Integer.toString(sentinelDefinition.tlsPort));
			}

			return result;
		}
	}

	record SentinelDefinition(String host, int port, int tlsPort) { }
}
