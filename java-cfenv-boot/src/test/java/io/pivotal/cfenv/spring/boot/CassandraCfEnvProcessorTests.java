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

import io.pivotal.cfenv.core.test.CfEnvMock;
import io.pivotal.cfenv.test.AbstractCfEnvTests;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Mark Pollack
 * @author David Turanski
 */
public class CassandraCfEnvProcessorTests extends AbstractCfEnvTests {

	private static final String SPRING_CASSANDRA = "spring.cassandra";

	@Test
	public void simpleService() {
		mockVcapServicesWithNames("io/pivotal/cfenv/spring/boot/test-cassandra-service.json");
		Environment environment = getEnvironment();
		assertThat(environment.getProperty(SPRING_CASSANDRA + ".username")).isNull();
		assertThat(environment.getProperty(SPRING_CASSANDRA + ".password")).isNull();
		assertThat(environment.getProperty(SPRING_CASSANDRA + ".port")).isEqualTo("9042");
		assertThat(environment.getProperty(SPRING_CASSANDRA + ".contact-points")).contains("1.2.3.4","5.6.7.8");
	}

	@Test
	public void simpleServiceUserNamePassword() {
		mockVcapServicesWithNames("io/pivotal/cfenv/spring/boot/test-cassandra-with-credentials.json");
		Environment environment = getEnvironment();
		assertThat(environment.getProperty(SPRING_CASSANDRA + ".username")).isEqualTo("user");
		assertThat(environment.getProperty(SPRING_CASSANDRA + ".password")).isEqualTo("pass");
		assertThat(environment.getProperty(SPRING_CASSANDRA + ".port")).isEqualTo("9042");
		assertThat(environment.getProperty(SPRING_CASSANDRA + ".contact-points")).contains("1.2.3.4");
	}

	@Test
	public void serviceNotValid() {
		mockVcapServicesWithNames("io/pivotal/cfenv/spring/boot/test-cassandra-missing-required-fields.json");
		Environment environment = getEnvironment();
		assertThat(environment.getProperty(SPRING_CASSANDRA + ".username")).isNull();
		assertThat(environment.getProperty(SPRING_CASSANDRA + ".password")).isNull();
		assertThat(environment.getProperty(SPRING_CASSANDRA + ".port")).isNull();
		assertThat(environment.getProperty(SPRING_CASSANDRA + ".contact-points")).isNull();
	}

	@Test
	public void testGetProperties() {
		assertThat(new CassandraCfEnvProcessor().getProperties().getPropertyPrefixes()).isEqualTo(SPRING_CASSANDRA);
		assertThat(new CassandraCfEnvProcessor().getProperties().getServiceName()).isEqualTo("Cassandra");
	}

	private void mockVcapServicesWithNames(String fileName) {
		CfEnvMock.configure().vcapServicesResource(fileName).mock();
	}
}
