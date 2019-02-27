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


import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import io.pivotal.cfenv.test.AbstractCfEnvTests;
import mockit.Mock;
import mockit.MockUp;
import org.junit.Test;

import org.springframework.boot.Banner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.util.ResourceUtils;


import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Mark Pollack
 */
public class CassandraCfEnvProcessorTests extends AbstractCfEnvTests {

	@Test
	public void simpleService() {
		mockVcapServicesWithNames("io/pivotal/cfenv/spring/boot/test-cassandra-service.json");
		Environment environment = getEnvironment();
		assertThat(environment.getProperty("spring.data.cassandra.username")).isNull();
		assertThat(environment.getProperty("spring.data.cassandra.password")).isNull();
		assertThat(environment.getProperty("spring.data.cassandra.port")).isEqualTo("9042");
		assertThat(environment.getProperty("spring.data.cassandra.contact-points")).contains("1.2.3.4","5.6.7.8");
	}

	@Test
	public void simpleServiceUserNamePassword() {
		mockVcapServicesWithNames("io/pivotal/cfenv/spring/boot/test-cassandra-with-credentials.json");
		Environment environment = getEnvironment();
		assertThat(environment.getProperty("spring.data.cassandra.username")).isEqualTo("user");
		assertThat(environment.getProperty("spring.data.cassandra.password")).isEqualTo("pass");
		assertThat(environment.getProperty("spring.data.cassandra.port")).isEqualTo("9042");
		assertThat(environment.getProperty("spring.data.cassandra.contact-points")).contains("1.2.3.4");
	}

	@Test
	public void serviceNotValid() {
		mockVcapServicesWithNames("io/pivotal/cfenv/spring/boot/test-cassandra-missing-required-fields.json");
		Environment environment = getEnvironment();
		assertThat(environment.getProperty("spring.data.cassandra.username")).isNull();
		assertThat(environment.getProperty("spring.data.cassandra.password")).isNull();
		assertThat(environment.getProperty("spring.data.cassandra.port")).isNull();
		assertThat(environment.getProperty("spring.data.cassandra.contact-points")).isNull();
	}


	public Environment getEnvironment() {
		SpringApplicationBuilder builder = new SpringApplicationBuilder(TestApp.class)
				.web(WebApplicationType.NONE);
		builder.bannerMode(Banner.Mode.OFF);
		ApplicationContext applicationContext = builder.run();
		return applicationContext.getEnvironment();
	}

	@SpringBootApplication
	static class TestApp {
	}

	//TODO consilidate mocking
	private void mockVcapServicesWithNames(String fileName) {
		String fileContents;
		try {
			File file = ResourceUtils.getFile("classpath:" + fileName);
			fileContents = new String(Files.readAllBytes(file.toPath()));
		}
		catch (Exception e) {
			throw new IllegalStateException(e);
		}

		Map<String, String> env = System.getenv();
		new MockUp<System>() {
			@Mock
			public String getenv(String name) {
				if (name.equalsIgnoreCase("VCAP_SERVICES")) {
					return fileContents;
				}
				return env.get(name);
			}
			@mockit.Mock
			public Map getenv() {
				Map<String,String> finalMap = new HashMap<>();
				finalMap.putAll(env);
				finalMap.put("VCAP_SERVICES", fileContents);
				return finalMap;
			}
		};

	}
}
