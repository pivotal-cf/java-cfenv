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
package io.pivotal.cfenv.boot.sso;

import java.util.List;
import java.util.Map;

import io.pivotal.cfenv.core.CfCredentials;
import io.pivotal.cfenv.core.CfEnv;
import io.pivotal.cfenv.core.CfService;
import io.pivotal.cfenv.spring.boot.CfEnvProcessor;
import io.pivotal.cfenv.spring.boot.CfEnvProcessorProperties;

/**
 * @author Mark Pollack
 * @author Scott Frederick
 */
public class CfSingleSignOnProcessor implements CfEnvProcessor {

	private static final String PIVOTAL_SSO_LABEL = "p-identity";

	@Override
	public List<CfService> findServices(CfEnv cfEnv) {
		return cfEnv.findServicesByLabel(PIVOTAL_SSO_LABEL);
	}


	@Override
	public void process(CfCredentials cfCredentials, Map<String, Object> properties) {
		String clientId = cfCredentials.getString("client_id");
		String clientSecret = cfCredentials.getString("client_secret");
		String authDomain = cfCredentials.getString("auth_domain");

		properties.put("security.oauth2.client.clientId", clientId);
		properties.put("security.oauth2.client.clientSecret", clientSecret);
		properties.put("security.oauth2.client.accessTokenUri",
				authDomain + "/oauth/token");
		properties.put("security.oauth2.client.userAuthorizationUri",
				authDomain + "/oauth/authorize");
		properties.put("ssoServiceUrl", authDomain);
		properties.put("security.oauth2.resource.userInfoUri",
				authDomain + "/userinfo");
		properties.put("security.oauth2.resource.tokenInfoUri",
				authDomain + "/check_token");
		properties.put("security.oauth2.resource.jwk.key-set-uri",
				authDomain + "/token_keys");
	}

	@Override
	public CfEnvProcessorProperties getProperties() {
		return CfEnvProcessorProperties.builder()
				.propertyPrefixes("security.oauth2.client, security.oauth2.resource")
				.serviceName("Single Sign On").build();
	}
}
