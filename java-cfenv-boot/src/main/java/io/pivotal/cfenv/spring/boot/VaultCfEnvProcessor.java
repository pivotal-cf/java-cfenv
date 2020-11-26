/*
 * Copyright 2020 the original author or authors.
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

import java.util.Collections;
import java.util.Map;

import io.pivotal.cfenv.core.CfCredentials;
import io.pivotal.cfenv.core.CfService;

/**
 * @author Dylan Roberts
 */
public class VaultCfEnvProcessor implements CfEnvProcessor {

	@Override
	public boolean accept(CfService service) {
		return service.existsByLabelStartsWith("hashicorp-vault");
	}

	@Override
	public void process(CfCredentials cfCredentials, Map<String, Object> properties) {
		properties.put("spring.cloud.vault.uri", cfCredentials.getString("address"));
		Map<String, Object> auth = (Map<String, Object>) cfCredentials.getMap().getOrDefault("auth", Collections.emptyMap());
		properties.put("spring.cloud.vault.token", auth.get("token"));
	}

	@Override
	public CfEnvProcessorProperties getProperties() {
		return CfEnvProcessorProperties.builder()
				.propertyPrefixes("spring.cloud.vault")
				.serviceName("HashiCorp Vault")
				.build();
	}

}
