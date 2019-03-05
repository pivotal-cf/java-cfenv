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

import org.junit.Test;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.cloud.config.client.ConfigClientAutoConfiguration;
import org.springframework.cloud.config.client.ConfigClientProperties;
import org.springframework.cloud.config.client.ConfigServiceBootstrapConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

public class PlainTextConfigClientAutoConfigTests {

	private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(PlainTextConfigClientAutoConfiguration.class,
					ConfigClientAutoConfiguration.class, ConfigClientOAuth2BootstrapConfiguration.class,
					ConfigServiceBootstrapConfiguration.class));

	@Test
	public void plainTextConfigClientIsNotCreated() throws Exception {
		this.contextRunner
				.run(context -> {
					assertThat(context).doesNotHaveBean(ConfigClientOAuth2ResourceDetails.class);
					assertThat(context).hasSingleBean(ConfigClientProperties.class);
					assertThat(context).doesNotHaveBean(PlainTextConfigClient.class);
				});
	}

	@Test
	public void plainTextConfigClientIsCreated() throws Exception {
		this.contextRunner
				.withPropertyValues(
						"spring.cloud.config.client.oauth2.client-id=acme",
						"spring.cloud.config.client.oauth2.client-secret=acmesecret")
				.run(context -> {
					assertThat(context).hasSingleBean(ConfigClientOAuth2ResourceDetails.class);
					ConfigClientOAuth2ResourceDetails details = context.getBean(ConfigClientOAuth2ResourceDetails.class);
					assertThat(details.getClientId()).isNotEmpty().isEqualTo("acme");
					assertThat(details.getClientSecret()).isNotEmpty().isEqualTo("acmesecret");
					assertThat(context).hasSingleBean(ConfigClientProperties.class);
					assertThat(context).hasSingleBean(PlainTextConfigClient.class);
				});
	}

}
