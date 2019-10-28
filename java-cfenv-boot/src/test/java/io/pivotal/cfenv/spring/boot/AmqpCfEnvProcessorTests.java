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

import org.junit.Test;

import org.springframework.core.env.Environment;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Mark Pollack
 * @author David Turanski
 */
public class AmqpCfEnvProcessorTests extends AmqpTestSupport {

	public static void commonAssertions(Environment environment) {
		assertThat(environment.getProperty("spring.rabbitmq.host")).isEqualTo(hostname);
		assertThat(environment.getProperty("spring.rabbitmq.password")).isEqualTo(password);
		assertThat(environment.getProperty("spring.rabbitmq.username")).isEqualTo(username);
	}

	@Test
	public void testAmqpBootProperties() {
		mockVcapServices(getServicesPayload(
			getRabbitServicePayloadWithTags("rabbit-1", hostname, port, username, password, "q-1", "vhost1")
		));
		Environment env = getEnvironment();
		commonAssertions(env);
	}

	@Test
	public void testAmqpSSlBootProperties() {
		mockVcapServices(getServicesPayload(
			getRabbitServicePayloadNoLabelNoTagsSecure("rabbit-1", hostname, port, username, password, "q-1","vhost1")
		));
		Environment environment = getEnvironment();
		commonAssertions(environment);
		assertThat(environment.getProperty("spring.rabbitmq.ssl.enabled")).isEqualTo("true");
		assertThat(environment.getProperty("spring.rabbitmq.port")).isEqualTo("5671");
	}

	@Test
	public void testAmqpBootMultipleUris() {
		mockVcapServices(getServicesPayload(
			getRabbitServicePayloadMultipleUris("rabbit-1", "host1", "host2", port, username, password, "q-1","vhost1")
		));
		Environment environment = getEnvironment();
		assertThat(environment.getProperty("spring.rabbitmq.addresses"))
			.isEqualTo("amqp://myuser:mypass@host1/vhost1,amqp://myuser:mypass@host2/vhost1");
	}

	@Test
	public void testAmqpBootExplicitProperties() {
		mockVcapServices(getServicesPayload(
			getRabbitServicePayloadWithoutTags("rabbit-1", hostname,  port, username, password, "q-1", "vhost1")
		));
		Environment env = getEnvironment();
		commonAssertions(env);
		assertThat(env.getProperty("spring.rabbitmq.virtualHost")).isEqualTo("vhost1");
	}
}
