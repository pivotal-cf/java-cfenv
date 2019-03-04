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
package io.pivotal.cfenv.boot.scs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.cloud.config.client.ConfigClientProperties;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit4.SpringRunner;


@Ignore
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ConfigServerTestApplication.class, webEnvironment = WebEnvironment.RANDOM_PORT, properties = {
		"spring.profiles.active=plaintext,native,integration-test",
		"spring.cloud.config.enabled=true",
		"eureka.client.enabled=false"
})
public class PlainTextDefaultLabelTests {

	// @formatter:off
	private static final String nginxConfig = "server {\n"
			+ "    listen              80;\n"
			+ "    server_name         default-label.example.com;\n"
			+ "}";
	// @formatter:on
	private PlainTextConfigClient configClient;

	@Autowired
	private ConfigClientProperties configClientProperties;
	@LocalServerPort
	private int port;
	@Autowired
	private ConfigClientOAuth2ResourceDetails resource;

	public String read(Resource resource) {
		try (BufferedReader buffer = new BufferedReader(
				new InputStreamReader(resource.getInputStream()))) {
			return buffer.lines().collect(Collectors.joining("\n"));
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Before
	public void setup() {
		resource.setAccessTokenUri("http://localhost:" + port + "/oauth/token");
		configClientProperties.setName("app");
		configClientProperties.setProfile(null);
		configClientProperties.setUri(new String[] {"http://localhost:" + port});
		configClient = new PlainTextConfigClientAutoConfiguration()
				.plainTextConfigClient(resource, configClientProperties);
	}

	@Test
	public void shouldFindFileWithDefaultLabel() {
		Assert.assertEquals(nginxConfig,
				read(configClient.getConfigFile("default-label-nginx.conf")));
	}
}
