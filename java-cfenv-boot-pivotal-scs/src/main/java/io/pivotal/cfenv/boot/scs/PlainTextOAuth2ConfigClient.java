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

import org.springframework.cloud.config.client.ConfigClientProperties;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.util.Assert;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * {@link OAuth2RestTemplate} based implementation of {@link PlainTextConfigClient}.
 * Config Server URI, default application name, profiles and labels are provided by
 * {@link ConfigClientProperties}.
 * 
 * <p>
 * This client requires an {@link OAuth2ProtectedResourceDetails} holding the Config
 * Server credentials configurations.
 * </p>
 * 
 * @author Daniel Lavoie
 */
class PlainTextOAuth2ConfigClient implements PlainTextConfigClient {
	private final ConfigClientProperties configClientProperties;

	private RestTemplate restTemplate;

	protected PlainTextOAuth2ConfigClient(final OAuth2ProtectedResourceDetails resource,
			final ConfigClientProperties configClientProperties) {
		this.restTemplate = new OAuth2RestTemplate(resource);
		this.configClientProperties = configClientProperties;
	}

	/**
	 * Retrieves a config file using the defaults application name, profiles and labels.
	 * 
	 * @throws IllegalArgumentException when application name or Config Server url is
	 * undefined.
	 * @throws HttpClientErrorException when a config file is not found.
	 */
	@Override
	public Resource getConfigFile(String path) {
		return getConfigFile(null, null, path);
	}

	/**
	 * Retrieves a config file.
	 * 
	 * @throws IllegalArgumentException when application name or Config Server url is
	 * undefined.
	 * @throws HttpClientErrorException when a config file is not found.
	 */
	@Override
	public Resource getConfigFile(String profile, String label, String path) {
		Assert.isTrue(
				configClientProperties.getName() != null
						&& !configClientProperties.getName().isEmpty(),
				"Spring application name is undefined.");

		Assert.notEmpty(configClientProperties.getUri(), "Config server URI is undefined");
		Assert.hasText(configClientProperties.getUri()[0], "Config server URI is undefined.");

		if (profile == null) {
			profile = configClientProperties.getProfile();
			if (profile == null || profile.isEmpty()) {
				profile = "default";
			}
		}

		if (label == null) {
			label = configClientProperties.getLabel();
		}

		String url = configClientProperties.getUri()[0] + "/"
				+ configClientProperties.getName() + "/" + profile + "/"
				+ (label == null ? path + "?useDefaultLabel" : label + "/" + path);
		ResponseEntity<Resource> forEntity = restTemplate.getForEntity(url,
				Resource.class);
		return forEntity.getBody();
	}
}
