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

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.config.client.ConfigClientProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration for a periodic Vault token renewer. Conditionally configured if there
 * is a {@link ConfigClientProperties} bean and if there is a `spring.cloud.config.token`
 * property set.
 * 
 * By default, the token is renewed every 60 seconds and is renewed with a 5 minute time-to-live.
 * The renew rate can be configured by setting `vault.token.renew.rate` to some value that is the 
 * renewal rate in milliseconds. The renewal time-to-live can be specified with by setting
 * `vault.token.ttl` to some value indicating the time-to-live in milliseconds.
 * 
 * @author cwalls
 */
@Configuration
@ConditionalOnBean(ConfigClientProperties.class)
@ConditionalOnProperty(name="spring.cloud.config.token")
@EnableScheduling
public class VaultTokenRenewalAutoConfiguration {
	
	private static final Logger logger = LoggerFactory.getLogger(VaultTokenRenewalAutoConfiguration.class);
	
	private final RestTemplate rest;
	private final String refreshUri;
	private final HttpEntity<Map<String, Long>> request;
	private final String obscuredToken;
	private final long renewTTL;

	@Autowired
	public VaultTokenRenewalAutoConfiguration(
			ConfigClientOAuth2ResourceDetails configClientOAuth2ResourceDetails,
			ConfigClientProperties configClientProps,
			@Value("${spring.cloud.config.token}") String vaultToken,
			@Value("${vault.token.ttl:300000}") long renewTTL) { // <-- Default to a 300 second (5 minute) TTL
		this.rest = new OAuth2RestTemplate(configClientOAuth2ResourceDetails);
		this.refreshUri = configClientProps.getUri()[0] + "/vault/v1/auth/token/renew-self";
		long renewTTLInMS = renewTTL / 1000; // convert to seconds, since that's what Vault wants
		this.request = buildTokenRenewRequest(vaultToken, renewTTLInMS);
		this.obscuredToken = vaultToken.substring(0, 4) + "[*]" + vaultToken.substring(vaultToken.length() - 4);
		this.renewTTL = renewTTL;
	}
	
	@Scheduled(fixedRateString="${vault.token.renew.rate:60000}")  // <-- Default to renew token every 60 seconds
	public void refreshVaultToken() {
		try {
			logger.info("Renewing Vault token " + obscuredToken + " for " + renewTTL + " milliseconds.");
			rest.postForObject(refreshUri, request, String.class);
		} catch (Exception e) {
			logger.error("Unable to renew Vault token " + obscuredToken + ". Is the token invalid or expired?");
		}
	}

	private HttpEntity<Map<String, Long>> buildTokenRenewRequest(String vaultToken, long renewTTL) {
		Map<String, Long> requestBody = new HashMap<>();
		requestBody.put("increment", renewTTL);
		HttpHeaders headers = new HttpHeaders();
		headers.set("X-Vault-Token", vaultToken);
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<Map<String, Long>> request = new HttpEntity<Map<String,Long>>(requestBody, headers);
		return request;
	}
	
}
