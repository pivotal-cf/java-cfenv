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
import mockit.Mock;
import mockit.MockUp;
import org.junit.Test;

import org.springframework.boot.Banner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

/**
 * @author Mark Pollack
 */
public class MongoCfEnvProcessorTests extends AbstractCfEnvTests {

	@Test
	public void testWithConnectorLibraryOnCP() {
		mockConnectorLibrary(true);
		mockVcapServices(getServicesPayload(
				getMongoServicePayload("mongo-1", hostname, port, username, password, "inventory-1", "db")
		));
		// Note, the exception is actually thrown from the CfDataSourceEnvironmentPostProcessor
		assertThatIllegalStateException().isThrownBy( () ->  getEnvironment().getProperty("spring.data.mongodb.uri"))
				.withMessage(ConnectorLibraryDetector.MESSAGE);
	}

	@Test
	public void mongoServiceCreation() {
		mockConnectorLibrary(false);
		mockVcapServices(getServicesPayload(
				getMongoServicePayload("mongo-1", hostname, port, username, password, "inventory-1", "db")
		));
		assertThat(getEnvironment().getProperty("spring.data.mongodb.uri")).isEqualTo("mongodb://myuser:mypass@10.20.30.40:1234/inventory-1");
	}

	@Test
	public void mongoServiceCreationNoLabelNoTags() {
		mockConnectorLibrary(false);
		mockVcapServices(getServicesPayload(
				getMongoServicePayloadNoLabelNoTags("mongo-1", hostname, port, username, password, "inventory-1", "db")
		));
		assertThat(getEnvironment().getProperty("spring.data.mongodb.uri")).isEqualTo("mongodb://myuser:mypass@10.20.30.40:1234/inventory-1");
	}

	public Environment getEnvironment() {
		SpringApplicationBuilder builder = new SpringApplicationBuilder(TestApp.class)
				.web(WebApplicationType.NONE);
		builder.bannerMode(Banner.Mode.OFF);
		ApplicationContext applicationContext = builder.run();
		return applicationContext.getEnvironment();
	}

	private MockUp<?> mockConnectorLibrary(boolean isUsingConnectorLibrary) {
		return new MockUp<ConnectorLibraryDetector>() {
			@Mock
			public boolean isUsingConnectorLibrary() {
				return isUsingConnectorLibrary;
			}
		};
	}

	@SpringBootApplication
	static class TestApp {
	}

	private String getMongoServicePayload(String serviceName, String hostname, int port,
										  String username, String password, String db, String name) {
		return getMongoServicePayload("test-mongodb-info.json",
				serviceName, hostname, port, username, password, db, name);
	}

	private String getMongoServicePayloadNoLabelNoTags(String serviceName,
													   String hostname, int port,
													   String username, String password, String db, String name) {
		return getMongoServicePayload("test-mongodb-info-no-label-no-tags.json",
				serviceName, hostname, port, username, password, db, name);
	}

	private String getMongoServicePayload(String payloadFile, String serviceName,
										  String hostname, int port,
										  String username, String password, String db, String name) {
		String payload = readTestDataFile(payloadFile);
		payload = payload.replace("$serviceName", serviceName);
		payload = payload.replace("$hostname", hostname);
		payload = payload.replace("$port", Integer.toString(port));
		payload = payload.replace("$username", username);
		payload = payload.replace("$password", password);
		payload = payload.replace("$db", db);
		payload = payload.replace("$name", name);

		return payload;
	}
}
