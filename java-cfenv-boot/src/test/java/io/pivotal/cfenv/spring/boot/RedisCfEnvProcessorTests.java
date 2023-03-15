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

import java.util.Arrays;
import java.util.Collection;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import org.springframework.core.env.Environment;

import io.pivotal.cfenv.test.AbstractCfEnvTests;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Mark Pollack
 * @author David Turanski
 */
@RunWith(Parameterized.class)
public class RedisCfEnvProcessorTests extends AbstractCfEnvTests {
	private final static int TLS_PORT = 11111;
	public static final String SPRING_REDIS = "spring.redis";
	public static final String SPRING_DATA_REDIS = "spring.data.redis";

	public static void commonAssertions(Environment environment, String configurationPrefix) {
		assertThat(environment.getProperty(configurationPrefix + ".host")).isEqualTo(hostname);
		assertThat(environment.getProperty(configurationPrefix + ".port")).isEqualTo(String.valueOf(port));
		assertThat(environment.getProperty(configurationPrefix + ".password")).isEqualTo(password);
	}

	@Parameters(name = "SpringBoot{0}")
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] {
				{ 2, SPRING_REDIS, new RedisCfEnvProcessor.Boot2()}, { 3, SPRING_DATA_REDIS, new RedisCfEnvProcessor.Boot3()}
		});
	}

	@Parameter
	public int springBootVersion;

	@Parameter(1)
	public String configurationPrefix;

	@Parameter(2)
	public CfEnvProcessor versionSpecificRedisCfEnvProcessor;

    @After
    public void resetSpringBootVersion() {
        SpringBootVersionResolver.forcedVersion = -1;
    }

	@Test
	public void testRedisBootPropertiesWithoutUriInCredentials() {
		SpringBootVersionResolver.forcedVersion = springBootVersion;
		String payload = payloadBuilder("test-redis-info.json").payload();
		mockVcapServices(getServicesPayload(payload));

        commonAssertions(getEnvironment(), configurationPrefix);
	}

	@Test
	public void testRedisBootPropertiesWithTLSEnabledInCredentials() {
		SpringBootVersionResolver.forcedVersion = springBootVersion;
		String payload = payloadBuilder("test-redis-info-with-tls-port.json")
				.withTLSPort(TLS_PORT)
				.payload();
		mockVcapServices(getServicesPayload(payload));

		Environment environment = getEnvironment();

		assertThat(environment.getProperty(configurationPrefix+".host")).isEqualTo(hostname);
		assertThat(environment.getProperty(configurationPrefix+".port")).isEqualTo(String.valueOf(TLS_PORT));
		assertThat(environment.getProperty(configurationPrefix+".password")).isEqualTo(password);
		assertThat(environment.getProperty(configurationPrefix+".ssl")).isEqualTo("true");
	}

	@Test
	public void testRedisSSlBootPropertiesWithUriInCredentials() {
		SpringBootVersionResolver.forcedVersion = springBootVersion;
		String payload = payloadBuilder("test-redis-info-no-label-no-tags-secure.json").payload();
		mockVcapServices(getServicesPayload(payload));

		Environment environment = getEnvironment();
		commonAssertions(getEnvironment(), configurationPrefix);
		assertThat(environment.getProperty(configurationPrefix+".ssl")).isEqualTo("true");
	}

	@Test
	public void testNoCredentials() {
		SpringBootVersionResolver.forcedVersion = springBootVersion;
		String payload = "{}";
		mockVcapServices(getServicesPayload(payload));

		Environment environment = getEnvironment();

		assertThat(environment.getProperty(configurationPrefix+".host")).isNull();
		assertThat(environment.getProperty(configurationPrefix+".port")).isNull();
		assertThat(environment.getProperty(configurationPrefix+".password")).isNull();
		assertThat(environment.getProperty(configurationPrefix+".ssl")).isNull();
	}

	@Test
	public void testGetProperties() {
		assertThat(versionSpecificRedisCfEnvProcessor.getProperties().getPropertyPrefixes()).isEqualTo(configurationPrefix);
		assertThat(versionSpecificRedisCfEnvProcessor.getProperties().getServiceName()).isEqualTo("Redis");

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
