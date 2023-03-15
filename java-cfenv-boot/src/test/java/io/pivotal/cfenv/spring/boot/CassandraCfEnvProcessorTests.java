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

import io.pivotal.cfenv.core.test.CfEnvMock;
import io.pivotal.cfenv.test.AbstractCfEnvTests;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Mark Pollack
 * @author David Turanski
 */
@RunWith(Parameterized.class)
public class CassandraCfEnvProcessorTests extends AbstractCfEnvTests {

	public static final String SPRING_CASSANDRA = "spring.cassandra";
	public static final String SPRING_DATA_CASSANDRA = "spring.data.cassandra";

	@Parameters(name = "SpringBoot{0}")
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] {
				{ 2, SPRING_DATA_CASSANDRA, new CassandraCfEnvProcessor.Boot2()}, { 3, SPRING_CASSANDRA, new CassandraCfEnvProcessor.Boot3()}
		});
	}

	@Parameter
	public int springBootVersion;

	@Parameter(1)
	public String configurationPrefix;

	@Parameter(2)
	public CfEnvProcessor versionSpecificCassandraCfEnvProcessor;

	@After
	public void resetSpringBootVersion() {
		SpringBootVersionResolver.forcedVersion = -1;
	}
	
	@Test
	public void simpleService() {
		SpringBootVersionResolver.forcedVersion = springBootVersion;
		mockVcapServicesWithNames("io/pivotal/cfenv/spring/boot/test-cassandra-service.json");
		Environment environment = getEnvironment();
		assertThat(environment.getProperty(configurationPrefix + ".username")).isNull();
		assertThat(environment.getProperty(configurationPrefix + ".password")).isNull();
		assertThat(environment.getProperty(configurationPrefix + ".port")).isEqualTo("9042");
		assertThat(environment.getProperty(configurationPrefix + ".contact-points")).contains("1.2.3.4","5.6.7.8");
	}

	@Test
	public void simpleServiceUserNamePassword() {
		SpringBootVersionResolver.forcedVersion = springBootVersion;
		mockVcapServicesWithNames("io/pivotal/cfenv/spring/boot/test-cassandra-with-credentials.json");
		Environment environment = getEnvironment();
		assertThat(environment.getProperty(configurationPrefix + ".username")).isEqualTo("user");
		assertThat(environment.getProperty(configurationPrefix + ".password")).isEqualTo("pass");
		assertThat(environment.getProperty(configurationPrefix + ".port")).isEqualTo("9042");
		assertThat(environment.getProperty(configurationPrefix + ".contact-points")).contains("1.2.3.4");
	}

	@Test
	public void serviceNotValid() {
		SpringBootVersionResolver.forcedVersion = springBootVersion;
		mockVcapServicesWithNames("io/pivotal/cfenv/spring/boot/test-cassandra-missing-required-fields.json");
		Environment environment = getEnvironment();
		assertThat(environment.getProperty(configurationPrefix + ".username")).isNull();
		assertThat(environment.getProperty(configurationPrefix + ".password")).isNull();
		assertThat(environment.getProperty(configurationPrefix + ".port")).isNull();
		assertThat(environment.getProperty(configurationPrefix + ".contact-points")).isNull();
	}

	@Test
	public void testGetProperties() {
		assertThat(versionSpecificCassandraCfEnvProcessor.getProperties().getPropertyPrefixes()).isEqualTo(configurationPrefix);
		assertThat(versionSpecificCassandraCfEnvProcessor.getProperties().getServiceName()).isEqualTo("Cassandra");

	}

	private void mockVcapServicesWithNames(String fileName) {
		CfEnvMock.configure().vcapServicesResource(fileName).mock();

	}
}
