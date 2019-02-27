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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.pivotal.cfenv.core.CfCredentials;
import io.pivotal.cfenv.core.CfEnv;
import io.pivotal.cfenv.core.CfService;
import io.pivotal.cfenv.core.UriInfo;


/**
 * Retrieve redis properties from {@see CfCredentials} and set {@literal spring.redis} Boot properties.
 *
 * @author Mark Pollack
 */
public class MongoCfEnvProcessor implements CfEnvProcessor {

	private static String mongoScheme = "mongodb";;

	@Override
	public List<CfService> findServices(CfEnv cfEnv) {
		List<CfService> cfServices = cfEnv.findAllServices();
		List<CfService> cfMongoServices = new ArrayList<>();

		for (CfService service : cfServices) {
			if (service.existsByTagIgnoreCase("mongodb") ||
					service.existsByLabelStartsWith("mongolab") ||
					service.existsByUriSchemeStartsWith(mongoScheme) ||
					service.existsByCredentialsContainsUriField(mongoScheme)) {
				cfMongoServices.add(service);
			}
		}

		return cfMongoServices;
	}

	@Override
	public void process(CfCredentials cfCredentials, Map<String, Object> properties) {
		String uri = cfCredentials.getUri(mongoScheme);
		UriInfo uriInfo = new UriInfo(uri);
		properties.put("spring.data.mongodb.uri", cfCredentials.getUri(mongoScheme));
	}

	@Override
	public CfEnvProcessorProperties getProperties() {
		return CfEnvProcessorProperties.builder().propertyPrefixes("spring.data.mongodb").serviceName("MongoDB").build();
	}

}
