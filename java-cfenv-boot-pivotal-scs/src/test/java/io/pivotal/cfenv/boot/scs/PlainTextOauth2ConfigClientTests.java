/*
 * Copyright 2017 the original author or authors.
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
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.cloud.config.client.ConfigClientProperties;
import org.springframework.core.io.Resource;
import org.springframework.security.oauth2.client.resource.OAuth2AccessDeniedException;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.HttpClientErrorException;

/**
 * @author Daniel Lavoie
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ConfigServerTestApplication.class, webEnvironment = WebEnvironment.RANDOM_PORT, properties = {
		"spring.profiles.active=plaintext,native", "spring.cloud.config.enabled=true", "eureka.client.enabled=false"})
public class PlainTextOauth2ConfigClientTests {
	// @formatter:off
	private static final String nginxConfig = "server {\n"
			+ "    listen              80;\n"
			+ "    server_name         example.com;\n"
			+ "}";

	private static final String devNginxConfig = "server {\n"
			+ "    listen              80;\n"
			+ "    server_name         dev.example.com;\n" 
			+ "}";

	private static final String testNginxConfig = "server {\n"
			+ "    listen              80;\n"
			+ "    server_name         test.example.com;\n"
			+ "}";
	// @formatter:on 

	@LocalServerPort
	private int port;

	@Autowired
	private ConfigClientOAuth2ResourceDetails resource;

	@Autowired
	private ConfigClientProperties configClientProperties;

	private PlainTextConfigClient configClient;

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
	public void shouldFindSimplePlainFile() {
		Assert.assertEquals(nginxConfig,
				read(configClient.getConfigFile(null, null, "nginx.conf")));

		Assert.assertEquals(devNginxConfig,
				read(configClient.getConfigFile("dev", "master", "nginx.conf")));

		configClientProperties.setProfile("test");
		Assert.assertEquals(testNginxConfig,
				read(configClient.getConfigFile("nginx.conf")));
	}

	@Test(expected = HttpClientErrorException.class)
	public void missingConfigFileShouldReturnHttpError() {
		configClient.getConfigFile("missing-config.xml");
	}

	@Test(expected = IllegalArgumentException.class)
	public void missingApplicationNameShouldCrash() {
		configClientProperties.setName("");
		configClient.getConfigFile("nginx.conf");
	}

	@Test(expected = IllegalArgumentException.class)
	public void missingConfigServerUrlShouldCrash() {
		configClientProperties.setUri(new String[]{""});
		configClient.getConfigFile("nginx.conf");
	}

	@Test(expected = OAuth2AccessDeniedException.class)
	public void shouldBeDenied() {
		ConfigClientOAuth2ResourceDetails invalidCrendentialsResource = new ConfigClientOAuth2ResourceDetails();
		invalidCrendentialsResource.setClientId("wrongClient");
		invalidCrendentialsResource.setAccessTokenUri(resource.getAccessTokenUri());
		invalidCrendentialsResource.setClientSecret("wrongsecret");
		invalidCrendentialsResource.setScope(resource.getScope());
		invalidCrendentialsResource.setGrantType(resource.getGrantType());

		new PlainTextConfigClientAutoConfiguration()
				.plainTextConfigClient(invalidCrendentialsResource,
						configClientProperties)
				.getConfigFile("nginx.conf");
	}

	public String read(Resource resource) {
		try (BufferedReader buffer = new BufferedReader(
				new InputStreamReader(resource.getInputStream()))) {
			return buffer.lines().collect(Collectors.joining("\n"));
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
