/*
 * Copyright 2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
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
 * Retrieve GenAI on Tanzu Platform properties from {@link CfCredentials} and
 * set {@literal spring.ai}
 * Boot properties.
 *
 * @author Stuart Charlton
 **/
public class GenAICfEnvProcessor implements CfEnvProcessor {

	@Override
	public boolean accept(CfService service) {
		return service.existsByTagIgnoreCase("genai") ||
				service.existsByLabelStartsWith("genai");
	}

	@Override
	public void process(CfCredentials cfCredentials, Map<String, Object> properties) {
		properties.put("spring.ai.openai.base-url", cfCredentials.getString("api_base"));
		properties.put("spring.ai.openai.api-key", cfCredentials.getString("api_key"));
	}

	@Override
	public CfEnvProcessorProperties getProperties() {
		return CfEnvProcessorProperties.builder()
				.propertyPrefixes("spring.ai.openai")
				.serviceName("GenAI on Tanzu Platform")
				.build();
	}
}
