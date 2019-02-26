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

import io.pivotal.cfenv.test.AbstractCfEnvTests;
import org.junit.Test;

import org.springframework.boot.Banner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Mark Pollack
 */
public class RedisCfEnvProcessorTests extends AbstractCfEnvTests {

	public static void commonAssertions(Environment environment) {
		assertThat(environment.getProperty("spring.redis.host")).isEqualTo(hostname);
		assertThat(environment.getProperty("spring.redis.port")).isEqualTo(String.valueOf(port));
		assertThat(environment.getProperty("spring.redis.password")).isEqualTo(password);
	}

	@Test
	public void testRedisBootProperties() {
		mockVcapServices(getServicesPayload(
				getRedisServicePayload("redis-1", hostname, port, password, "redis-db")
		));
		commonAssertions(getEnvironment());
	}

	@Test
	public void testRedisSSlBootProperties() {
		mockVcapServices(getServicesPayload(
				getRedisServicePayloadNoLabelNoTags("redis-1", hostname, port, password, "redis-db")
		));
		Environment environment = getEnvironment();
		commonAssertions(environment);
		assertThat(environment.getProperty("spring.redis.ssl")).isEqualTo("true");
	}

	public Environment getEnvironment() {
		SpringApplicationBuilder builder = new SpringApplicationBuilder(TestApp.class)
				.web(WebApplicationType.NONE);
		builder.bannerMode(Banner.Mode.OFF);
		ApplicationContext applicationContext = builder.run();
		return applicationContext.getEnvironment();
	}

	private String getRedisServicePayload(String serviceName,
										  String hostname, int port,
										  String password, String name) {
		return getRedisServicePayload("test-redis-info.json", serviceName, hostname, port, password, name);
	}

	private String getRedisServicePayloadNoLabelNoTags(String serviceName,
													   String hostname, int port,
													   String password, String name) {
		return getRedisServicePayload("test-redis-info-no-label-no-tags-secure.json", serviceName, hostname, port, password, name);
	}

	private String getRedisServicePayload(String payloadFile, String serviceName,
										  String hostname, int port,
										  String password, String name) {
		String payload = readTestDataFile(payloadFile);
		payload = payload.replace("$serviceName", serviceName);
		payload = payload.replace("$hostname", hostname);
		payload = payload.replace("$port", Integer.toString(port));
		payload = payload.replace("$password", password);
		payload = payload.replace("$name", name);

		return payload;
	}

	@SpringBootApplication
	static class TestApp {
	}
}
