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

import java.util.List;

import io.pivotal.cfenv.core.CfEnv;
import io.pivotal.cfenv.core.CfService;
import io.pivotal.cfenv.test.AbstractCfEnvTests;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author David Turanski
 **/
public class RedisTests extends AbstractCfEnvTests {
	@Test
	public void redisServiceCreation() {
		mockVcapServices(
				getServicesPayload(
						getRedisServicePayload("redis-1", hostname, port, password, "redis-db"),
						getRedisServicePayload("redis-2", hostname, port, password, "redis-db")));

		CfEnv cfEnv = new CfEnv();
		List<CfService> cfServices = cfEnv.findServicesByName("redis-1", "redis-2");
		assertThat(cfServices).hasSize(2);
		assertThat(cfServices).allMatch(cfService -> cfService.getLabel().equals("rediscloud"));
		assertThat(cfServices).allMatch(
				cfService -> cfService.getCredentials().getUriInfo("redis").getUriString()
						.equals("redis://10.20.30.40:1234"));

	}

	@Test
	public void redisServiceCreationNoLabelNoTags() {
		mockVcapServices(
			getServicesPayload(
				getRedisServicePayloadNoLabelNoTags("redis-1", hostname, port, password, "redis-db"),
				getRedisServicePayloadNoLabelNoTags("redis-2", hostname, port, password, "redis-db")));

		CfEnv cfEnv = new CfEnv();
		List<CfService> cfServices = cfEnv.findServicesByName("redis-1", "redis-2");
		assertThat(cfServices).hasSize(2);
		assertThat(cfServices).allMatch(
			cfService -> cfService.getCredentials().getUri("rediss")
				.equals("rediss://myuser:mypass@10.20.30.40:1234"));
	}

	private String getRedisServicePayload(String serviceName,
			String hostname, int port,
			String password, String name) {
		return getRedisServicePayload("test-redis-info.json", serviceName, hostname, port, password, name, username);
	}

	private String getRedisServicePayloadNoLabelNoTags(String serviceName,
			String hostname, int port,
			String password, String name) {
		return getRedisServicePayload("test-redis-info-no-label-no-tags-secure.json", serviceName, hostname, port,
				password, name, username);
	}

	private String getRedisServicePayload(String payloadFile, String serviceName,
			String hostname, int port,
			String password, String name, String username) {
		String payload = readTestDataFile(payloadFile);
		payload = payload.replace("$serviceName", serviceName);
		payload = payload.replace("$hostname", hostname);
		payload = payload.replace("$port", Integer.toString(port));
		payload = payload.replace("$password", password);
		payload = payload.replace("$name", name);
		payload = payload.replace("$username", username);

		return payload;
	}
}
