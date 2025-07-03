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

import io.pivotal.cfenv.core.CfCredentials;
import io.pivotal.cfenv.core.CfService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Retrieve GenAI on Tanzu Platform properties from {@link CfCredentials} and
 * set {@literal spring.ai.openai.chat}
 * Boot properties.
 *
 * @author Stuart Charlton
 * @author Ed King
 * @author Corby Page
 **/
public class GenAIChatCfEnvProcessor implements CfEnvProcessor {

	private static final String PROPERTY_PREFIX = "spring.ai.openai.chat";
	private static final String OPENAI_PATH_SUFFIX = "/openai";

	private final GenAIModelDiscoveryService discoveryService;
	private final GenAIModelSelector modelSelector;

	public GenAIChatCfEnvProcessor() {
		this(new GenAIModelDiscoveryService(), GenAIModelSelector.forChat());
	}

	// Constructor for testing
	GenAIChatCfEnvProcessor(GenAIModelDiscoveryService discoveryService, GenAIModelSelector modelSelector) {
		this.discoveryService = discoveryService;
		this.modelSelector = modelSelector;
	}

	@Override
	public boolean accept(CfService service) {
		return service.existsByTagIgnoreCase("genai") || service.existsByLabelStartsWith("genai");
	}

	@Override
	public void process(CfCredentials cfCredentials, Map<String, Object> properties) {
		try {
			Map<String, Object> credentialsMap = cfCredentials.getMap();

			if (GenAICredentialFormatDetector.isMultiModelFormat(credentialsMap)) {
				processMultiModelFormat(credentialsMap, properties, "genai-chat");
			} else {
				processLegacyFormat(credentialsMap, properties);
			}

		} catch (Exception e) {
			// Don't add properties on error, but don't throw either
		}
	}

	@Override
	public CfEnvProcessorProperties getProperties() {
		return CfEnvProcessorProperties.builder()
				.propertyPrefixes("spring.ai.openai.chat")
				.serviceName("GenAI")
				.build();
	}

	private void processMultiModelFormat(Map<String, Object> credentialsMap,
										 Map<String, Object> properties,
										 String serviceName) {
		try {
			// Extract endpoint information
			String apiBase = GenAICredentialFormatDetector.extractApiBase(credentialsMap);
			String apiKey = GenAICredentialFormatDetector.extractApiKey(credentialsMap);
			String configUrl = GenAICredentialFormatDetector.extractConfigUrl(credentialsMap);

			// Discover available models
			List<GenAIModelInfo> models = discoveryService.discoverModels(configUrl, apiKey);

			if (models.isEmpty()) {
				// Fall back to basic configuration without model selection
				configureBasicProperties(properties, apiBase, apiKey);
				return;
			}

			// Select appropriate chat model
			Optional<GenAIModelInfo> selectedModel = modelSelector.selectModel(models, GenAIModelInfo.Capability.CHAT);

			if (selectedModel.isPresent()) {
				String modelName = selectedModel.get().getName();

				// Configure Spring AI OpenAI properties
				properties.put(PROPERTY_PREFIX + ".base-url", apiBase + OPENAI_PATH_SUFFIX);
				properties.put(PROPERTY_PREFIX + ".api-key", apiKey);
				properties.put(PROPERTY_PREFIX + ".options.model", modelName);

			} else {
				// Configure without model - let Spring AI use its default
				configureBasicProperties(properties, apiBase, apiKey);
			}

		} catch (Exception e) {
		}
	}

	private void processLegacyFormat(Map<String, Object> credentialsMap, Map<String, Object> properties) {
		// Legacy format processing - maintain backward compatibility
		// Expected structure varies, but typically includes api_key and api_base

		String apiKey = (String) credentialsMap.get("api_key");
		String apiBase = (String) credentialsMap.get("api_base");

		// Check for model_name as per documentation
		String model = (String) credentialsMap.get("model_name");

		if (apiKey != null && apiBase != null) {
			properties.put(PROPERTY_PREFIX + ".base-url", apiBase);
			properties.put(PROPERTY_PREFIX + ".api-key", apiKey);

			if (model != null) {
				properties.put(PROPERTY_PREFIX + ".options.model", model);
			}
		}
	}

	private void configureBasicProperties(Map<String, Object> properties, String baseUrl, String apiKey) {
		properties.put(PROPERTY_PREFIX + ".base-url", baseUrl);
		properties.put(PROPERTY_PREFIX + ".api-key", apiKey);
		// Let Spring AI use its default model
	}
}