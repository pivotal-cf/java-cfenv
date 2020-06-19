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


/**
 * Retrieve CredHub secrets from {@link CfCredentials} and set Boot properties.
 *
 * @author Jakob Fels
 **/
public class CredHubCfEnvProcessor implements CfEnvProcessor {

	@Override
	public boolean accept(CfService service) {
		return service.existsByTagIgnoreCase("credhub") ||
				service.existsByLabelStartsWith("credhub");
	}

	@Override
	public void process(CfCredentials cfCredentials, Map<String, Object> properties) {
		Map<String, Object> allCredentials = cfCredentials.getMap();
		for (Map.Entry<String, Object> entry : allCredentials.entrySet()) {
			properties.put(entry.getKey(), entry.getValue());
		}
	}

	@Override
	public CfEnvProcessorProperties getProperties() {
		return CfEnvProcessorProperties.builder()
				.serviceName("CredHub")
				.build();
	}
}
