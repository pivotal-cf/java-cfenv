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

import java.util.ArrayList;
import java.util.Map;

import io.pivotal.cfenv.core.CfCredentials;
import io.pivotal.cfenv.core.CfService;

/**
 * Retrieve GenAI on Tanzu Platform properties from {@link CfCredentials} and
 * set {@literal spring.ai.openai.embedding}
 * Boot properties.
 *
 * @author Stuart Charlton
 * @author Ed King
 * @author Gareth Evans
 **/
public class GenAIEmbeddingCfEnvProcessor implements CfEnvProcessor {

	@Override
	public boolean accept(CfService service) {
		boolean isGenAIService = service.existsByTagIgnoreCase("genai") || service.existsByLabelStartsWith("genai");
		if (isGenAIService) {
			ArrayList<String> modelCapabilities = (ArrayList<String>) service.getCredentials().getMap().get("model_capabilities");
			return (modelCapabilities != null && modelCapabilities.contains("embedding"));
		}

		return false;
	}

	@Override
	public void process(CfCredentials cfCredentials, Map<String, Object> properties) {
		properties.put("spring.ai.openai.api-key", "redundant");

		properties.put("spring.ai.openai.embedding.base-url", cfCredentials.getString("api_base"));
		properties.put("spring.ai.openai.embedding.api-key", cfCredentials.getString("api_key"));
		properties.put("spring.ai.openai.embedding.options.model", cfCredentials.getString("model_name"));
	}

	@Override
	public CfEnvProcessorProperties getProperties() {
		return CfEnvProcessorProperties.builder()
				.propertyPrefixes("spring.ai.openai.embedding")
				.serviceName("GenAI on Tanzu Platform (embedding)")
				.build();
	}
}
