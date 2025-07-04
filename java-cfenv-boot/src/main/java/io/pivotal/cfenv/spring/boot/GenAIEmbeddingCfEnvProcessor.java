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
 * set {@literal spring.ai.openai.embedding}
 * Boot properties.
 *
 * @author Stuart Charlton
 * @author Ed King
 * @author Corby Page
 **/
public class GenAIEmbeddingCfEnvProcessor implements CfEnvProcessor {

    private static final String OPENAI_PATH_SUFFIX = "/openai";

    private final GenAIModelDiscoveryService discoveryService;
    private final GenAIModelSelector modelSelector = new GenAIModelSelector();

    public GenAIEmbeddingCfEnvProcessor() {
        this(new GenAIModelDiscoveryService());
    }

    // Constructor for testing
    GenAIEmbeddingCfEnvProcessor(GenAIModelDiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    @Override
    public boolean accept(CfService service) {
        return service.existsByTagIgnoreCase("genai") || service.existsByLabelStartsWith("genai");
    }

    @Override
    public void process(CfCredentials cfCredentials, Map<String, Object> properties) {
        Map<String, Object> credentialsMap = cfCredentials.getMap();

        if (GenAICredentialFormatDetector.isMultiModelFormat(credentialsMap)) {
            processMultiModelFormat(credentialsMap, properties);
        } else {
            processLegacyFormat(credentialsMap, properties);
        }
    }

    @Override
    public CfEnvProcessorProperties getProperties() {
        return CfEnvProcessorProperties.builder()
                .propertyPrefixes("spring.ai.openai.embedding")
                .serviceName("GenAI")
                .build();
    }

    private void processMultiModelFormat(Map<String, Object> credentialsMap,
                                         Map<String, Object> properties) {
        String apiBase = GenAICredentialFormatDetector.extractApiBase(credentialsMap);
        String apiKey = GenAICredentialFormatDetector.extractApiKey(credentialsMap);
        String configUrl = GenAICredentialFormatDetector.extractConfigUrl(credentialsMap);

        List<GenAIModelInfo> models = discoveryService.discoverModels(configUrl, apiKey);
        if (models.isEmpty()) {
            return;
        }

        Optional<GenAIModelInfo> selectedModel = modelSelector.selectModel(models, GenAIModelInfo.Capability.EMBEDDING);
        if (selectedModel.isPresent()) {
            String modelName = selectedModel.get().getName();

            properties.put("spring.ai.openai.api-key", "redundant");
            properties.put("spring.ai.openai.embedding.base-url", apiBase + OPENAI_PATH_SUFFIX);
            properties.put("spring.ai.openai.embedding.api-key", apiKey);
            properties.put("spring.ai.openai.embedding.options.model", modelName);
        }
    }

    private void processLegacyFormat(Map<String, Object> credentialsMap, Map<String, Object> properties) {
        // Legacy format processing - maintain backward compatibility
        List<String> modelCapabilities = (List<String>) credentialsMap.get("model_capabilities");
        if (modelCapabilities == null || !modelCapabilities.contains("embedding")) {
            return;
        }

        properties.put("spring.ai.openai.api-key", "redundant");
        properties.put("spring.ai.openai.embedding.base-url", credentialsMap.get("api_base"));
        properties.put("spring.ai.openai.embedding.api-key", credentialsMap.get("api_key"));
        properties.put("spring.ai.openai.embedding.options.model", credentialsMap.get("model_name"));
    }
}