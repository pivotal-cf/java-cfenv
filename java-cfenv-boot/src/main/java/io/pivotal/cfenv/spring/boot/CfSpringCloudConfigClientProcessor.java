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

import java.util.List;
import java.util.Map;

import io.pivotal.cfenv.core.CfCredentials;
import io.pivotal.cfenv.core.CfEnv;
import io.pivotal.cfenv.core.CfService;

/**
 * @author Mark Pollack
 * @author Scott Frederick
 */
public class CfSpringCloudConfigClientProcessor implements CfEnvProcessor {
	private static final String CONFIG_SERVER_SERVICE_TAG_NAME = "configuration";

	private static final String SPRING_CLOUD_CONFIG_URI = "spring.cloud.config.uri";
	private static final String SPRING_CLOUD_CONFIG_OAUTH2_CLIENT_CLIENT_ID = "spring.cloud.config.client.oauth2.clientId";
	private static final String SPRING_CLOUD_CONFIG_OAUTH2_CLIENT_CLIENT_SECRET = "spring.cloud.config.client.oauth2.clientSecret";
	private static final String SPRING_CLOUD_CONFIG_OAUTH2_CLIENT_ACCESS_TOKEN_URI = "spring.cloud.config.client.oauth2.accessTokenUri";

	@Override
	public List<CfService> findServices(CfEnv cfEnv) {
		return cfEnv.findServicesByTag(CONFIG_SERVER_SERVICE_TAG_NAME);
	}

	@Override
	public String getPropertySourceName() {
		return "cfSpringCloudConfigClientProcessor";
	}

	@Override
	public void process(CfCredentials cfCredentials, Map<String, Object> properties) {
		String uri = cfCredentials.getUri();
		String clientId = cfCredentials.getString("client_id");
		String clientSecret = cfCredentials.getString("client_secret");
		String accessTokenUri = cfCredentials.getString("access_token_uri");

		properties.put(SPRING_CLOUD_CONFIG_URI, uri);
		properties.put(SPRING_CLOUD_CONFIG_OAUTH2_CLIENT_CLIENT_ID, clientId);
		properties.put(SPRING_CLOUD_CONFIG_OAUTH2_CLIENT_CLIENT_SECRET,
				clientSecret);
		properties.put(SPRING_CLOUD_CONFIG_OAUTH2_CLIENT_ACCESS_TOKEN_URI,
				accessTokenUri);
	}

	@Override
	public String getPropertyPrefixes() {
		return "spring.cloud.config";
	}
}
