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

import java.util.Map;

import io.pivotal.cfenv.core.CfCredentials;
import io.pivotal.cfenv.core.CfService;
import io.pivotal.cfenv.core.UriInfo;


/**
 * Retrieve Redis properties from {@see CfCredentials} and set {@literal spring.redis} Boot properties.
 *
 * @author Mark Pollack
 * @author Scott Frederick
 */
public class RedisCfEnvProcessor implements CfEnvProcessor {

	private static String[] redisSchemes = new String[]{"redis", "rediss"};

	@Override
	public boolean accept(CfService service) {
		return service.existsByTagIgnoreCase("redis") ||
				service.existsByLabelStartsWith("rediscloud") ||
				service.existsByUriSchemeStartsWith(redisSchemes) ||
				service.existsByCredentialsContainsUriField(redisSchemes);
	}

	@Override
	public void process(CfCredentials cfCredentials, Map<String, Object> properties) {
		String uri = cfCredentials.getUri(redisSchemes);

		if (uri == null) {
			properties.put("spring.redis.host", cfCredentials.getHost());
			properties.put("spring.redis.port", cfCredentials.getPort());
			properties.put("spring.redis.password", cfCredentials.getPassword());
		} else {
			UriInfo uriInfo = new UriInfo(uri);
			properties.put("spring.redis.host", uriInfo.getHost());
			properties.put("spring.redis.port", uriInfo.getPort());
			properties.put("spring.redis.password", uriInfo.getPassword());
			if (uriInfo.getScheme().equals("rediss")) {
				properties.put("spring.redis.ssl", "true");
			}
		}
	}

	@Override
	public CfEnvProcessorProperties getProperties() {
		return CfEnvProcessorProperties.builder()
				.propertyPrefixes("spring.redis")
				.serviceName("Redis")
				.build();
	}

}
