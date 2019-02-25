package io.pivotal.cfenv.spring.boot;

import io.pivotal.cfenv.core.CfCredentials;
import io.pivotal.cfenv.core.CfEnv;
import io.pivotal.cfenv.core.CfService;

import java.util.Map;

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
	public CfService findService(CfEnv cfEnv) {
		return cfEnv.findServiceByTag(CONFIG_SERVER_SERVICE_TAG_NAME);
	}

	@Override
	public String getPropertySourceName() {
		return "cfEnvSpringCloudConfigClient";
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
}
