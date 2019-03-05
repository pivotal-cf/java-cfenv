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

import org.awaitility.Duration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cloud.config.client.ConfigClientProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;

/**
 * @author Roy Clarkson
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = VaultTokenRenewalAutoConfigurationTest.TestApplication.class, properties = {
		"spring.cloud.config.token=footoken", "vault.token.renew.rate=1000" })
public class VaultTokenRenewalAutoConfigurationTest {

	@MockBean
	private ConfigClientOAuth2ResourceDetails configClientOAuth2ResourceDetails;

	@Autowired
	private ConfigClientProperties configClientProperties;

	@Autowired
	private ApplicationContext context;

	@SpyBean
	private VaultTokenRenewalAutoConfiguration config;

	@Mock
	private RestTemplate rest;

	@Test
	public void contextLoads() {
		assertThat(context.getBeansOfType(ConfigClientProperties.class)).hasSize(1);
	}

	@Test
	public void scheduledVaultTokenRefresh() {
		this.configClientProperties.setUri(new String[] { "https://foobar" });
		given(rest.postForObject(anyString(), any(HttpEntity.class), any())).willReturn("foo");
		ReflectionTestUtils.setField(config, "rest", this.rest);

		await().atMost(Duration.FIVE_SECONDS).untilAsserted(() -> {
			verify(config, atLeast(4)).refreshVaultToken();
			verify(rest, atLeast(4)).postForObject(anyString(), any(HttpEntity.class), any());
		});
	}

	@SpringBootApplication
	static class TestApplication {

	}

}
