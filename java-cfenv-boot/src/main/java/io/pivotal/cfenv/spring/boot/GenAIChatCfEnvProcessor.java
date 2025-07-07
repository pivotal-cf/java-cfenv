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

import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.pivotal.cfenv.core.CfCredentials;
import io.pivotal.cfenv.core.CfService;

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

    private static final String OPENAI_PATH_SUFFIX = "/openai";

    private final GenAIModelDiscoveryService discoveryService= new GenAIModelDiscoveryService();
    private final GenAIModelSelector modelSelector = new GenAIModelSelector();

    @Override
    public boolean accept(CfService service) {
        boolean isGenAIService = service.existsByTagIgnoreCase("genai") || service.existsByLabelStartsWith("genai");
        return isGenAIService && hasChatModel(service.getCredentials());
    }

    private boolean hasChatModel(CfCredentials cfCredentials) {
        Map<String, Object> credentialsMap = cfCredentials.getMap();

        if (GenAICredentialFormatDetector.isMultiModelFormat(credentialsMap)) {
            String apiKey = GenAICredentialFormatDetector.extractApiKey(credentialsMap);
            String configUrl = GenAICredentialFormatDetector.extractConfigUrl(credentialsMap);

            List<GenAIModelInfo> models = discoveryService.discoverModels(configUrl, apiKey);
            Optional<GenAIModelInfo> selectedModel = modelSelector.selectModel(models, GenAIModelInfo.Capability.CHAT);
            return selectedModel.isPresent();
        } else {
            List<String> modelCapabilities = (List<String>) credentialsMap.get("model_capabilities");
            return (modelCapabilities != null && modelCapabilities.contains("chat"));
        }
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
                .propertyPrefixes("spring.ai.openai.chat")
                .serviceName("GenAI on Tanzu Platform (chat)")
                .build();
    }

    private void processMultiModelFormat(Map<String, Object> credentialsMap,
                                         Map<String, Object> properties) {
        String apiBase = GenAICredentialFormatDetector.extractApiBase(credentialsMap);
        String apiKey = GenAICredentialFormatDetector.extractApiKey(credentialsMap);
        String configUrl = GenAICredentialFormatDetector.extractConfigUrl(credentialsMap);

        List<GenAIModelInfo> models = discoveryService.discoverModels(configUrl, apiKey);
        Optional<GenAIModelInfo> selectedModel = modelSelector.selectModel(models, GenAIModelInfo.Capability.CHAT);
        if (selectedModel.isPresent()) {
            String modelName = selectedModel.get().getName();

            properties.put("spring.ai.openai.api-key", "redundant");
            properties.put("spring.ai.openai.chat.base-url", apiBase + OPENAI_PATH_SUFFIX);
            properties.put("spring.ai.openai.chat.api-key", apiKey);
            properties.put("spring.ai.openai.chat.options.model", modelName);
        }
    }

    private void processLegacyFormat(Map<String, Object> credentialsMap, Map<String, Object> properties) {
        // Legacy format processing - maintain backward compatibility
        properties.put("spring.ai.openai.api-key", "redundant");
        properties.put("spring.ai.openai.chat.base-url", credentialsMap.get("api_base"));
        properties.put("spring.ai.openai.chat.api-key", credentialsMap.get("api_key"));
        properties.put("spring.ai.openai.chat.options.model", credentialsMap.get("model_name"));
    }
}
