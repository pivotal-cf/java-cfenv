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

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.config.client.ConfigServicePropertySourceLocator;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.util.Assert;

/**
 * @author Mike Heath
 * @author Will Tran
 */
@Configuration
@EnableConfigurationProperties(ConfigClientOAuth2ResourceDetails.class)
@ConditionalOnClass({ConfigServicePropertySourceLocator.class, OAuth2RestTemplate.class})
@ConditionalOnProperty("spring.cloud.config.client.oauth2.client-id")
public class ConfigClientOAuth2BootstrapConfiguration {

	@Configuration
	public class ConfigClientOAuth2Configurer {

		private final ConfigServicePropertySourceLocator locator;

		private final ConfigClientOAuth2ResourceDetails configClientOAuth2ResourceDetails;

		@Autowired
		public ConfigClientOAuth2Configurer(ConfigServicePropertySourceLocator locator, ConfigClientOAuth2ResourceDetails configClientOAuth2ResourceDetails) {
			Assert.notNull(locator, "Error injecting ConfigServicePropertySourceLocator, this can occur" +
					"using self signed certificates in Cloud Foundry without setting the TRUST_CERTS environment variable");
			this.locator = locator;
			this.configClientOAuth2ResourceDetails = configClientOAuth2ResourceDetails;
		}

		@PostConstruct
		public void init() {
			 locator.setRestTemplate(new OAuth2RestTemplate(configClientOAuth2ResourceDetails));
		}

	}

}
