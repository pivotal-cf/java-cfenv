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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import org.springframework.core.env.Environment;

import io.pivotal.cfenv.core.test.CfEnvMock;
import io.pivotal.cfenv.test.AbstractCfEnvTests;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author David Turanski
 **/
public class DisableServicesTests extends AbstractCfEnvTests {

	@Test
	public void defaultEnabled() {
		CfEnvMock.configure().vcapServicesResource("vcap-services.json").mock();
		Environment environment = getEnvironment();
		assertThat(environment.containsProperty("spring.redis.host")).isTrue();
		assertThat(environment.containsProperty("spring.datasource.url")).isTrue();

	}

	@Test
	public void disableRedis() {
		CfEnvMock.configure().vcapServicesResource("vcap-services.json").mock();

		Environment environment = getEnvironment(Collections.singletonMap("cfenv.service.redis.enabled",
			"false"));

		assertThat(environment.containsProperty("spring.redis.host")).isFalse();
		assertThat(environment.containsProperty("spring.datasource.url")).isTrue();

	}
	@Test
	public void disableJdbc() {
		CfEnvMock.configure().vcapServicesResource("vcap-services.json").mock();

		Environment environment = getEnvironment(Collections.singletonMap("cfenv.service.mysql.enabled",
			"false"));

		assertThat(environment.containsProperty("spring.redis.host")).isTrue();
		assertThat(environment.containsProperty("spring.datasource.url")).isFalse();

	}


	@Test
	public void disableAllButOneJdbcs() {
		CfEnvMock.configure().vcapServicesResource("vcap-services-multiple-jdbcs.json").mock();

		Map<String, Object> properties = new HashMap<>();
		properties.put("cfenv.service.mysql.enabled","false");
		properties.put("cfenv.service.oracle.enabled","false");
		Environment environment = getEnvironment(properties);

		assertThat(environment.getProperty("spring.datasource.url").contains("another-host")).isTrue();

	}

	@Test
	public void disableAOneOfTwoRedis() {
		CfEnvMock.configure().vcapServicesResource("vcap-multiple-redis-services.json").mock();

		Map<String, Object> properties = new HashMap<>();
		properties.put("cfenv.service.redis.enabled","false");
		Environment environment = getEnvironment(properties);

		assertThat(environment.getProperty("spring.redis.host")).isEqualTo("another-host");

	}

}
