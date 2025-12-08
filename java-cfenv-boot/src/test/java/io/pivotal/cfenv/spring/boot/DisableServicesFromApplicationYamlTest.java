/*
 * Copyright 2020 the original author or authors.
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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.pivotal.cfenv.core.test.CfEnvMock;
import io.pivotal.cfenv.spring.boot.DisableServicesFromApplicationYamlTest.TestApplication;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = TestApplication.class)
@ActiveProfiles("disable-services")
public class DisableServicesFromApplicationYamlTest {

	@Autowired
	private Environment environment;

	@BeforeAll
	public static void setUp() {
		CfEnvMock.configure().vcapServicesResource("vcap-services.json").mock();
	}

	@Test
	public void shouldDisableServicesFromApplicationYamlFile() throws Exception {
		assertThat(environment.containsProperty("spring.redis.host"))
				.describedAs("redis service should be disabled").isFalse();
		assertThat(environment.containsProperty("spring.datasource.url"))
				.describedAs("mysql service should be disabled").isFalse();
	}

	@SpringBootApplication
	static class TestApplication {

		public static void main(String[] args) {
			SpringApplication.run(TestApplication.class, args);
		}

	}

}
