/*
 * Copyright 2025 the original author or authors.
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
package io.pivotal.cfenv.boot.genai;

import java.util.Map;

import io.pivotal.cfenv.core.CfCredentials;
import io.pivotal.cfenv.core.CfService;
import io.pivotal.cfenv.spring.boot.CfEnvProcessor;
import io.pivotal.cfenv.spring.boot.CfEnvProcessorProperties;

/**
 * Retrieve GenAI on Tanzu Platform properties from {@link CfCredentials} and
 * set {@literal genai.locator.*} Boot properties.
 *
 * @author Gareth Evans
 **/
public class CfGenaiProcessor implements CfEnvProcessor {

    @Override
    public boolean accept(CfService service) {
        boolean isGenAIService = service.existsByTagIgnoreCase("genai") || service.existsByLabelStartsWith("genai");
        // we only want to process service instances that are generated from Tanzu Platform 10.2 or later
        return (isGenAIService && service.getCredentials().getMap().containsKey("endpoint"));
    }

    @Override
    public void process(CfCredentials cfCredentials, Map<String, Object> properties) {
        Map<String, Object> endpoint = (Map<String, Object>)cfCredentials.getMap().get("endpoint");

        properties.put("genai.locator.config-url", endpoint.get("config_url"));
        properties.put("genai.locator.api-key", endpoint.get("api_key"));
        properties.put("genai.locator.api-base", endpoint.get( "api_base"));
    }

    @Override
    public CfEnvProcessorProperties getProperties() {
        return CfEnvProcessorProperties.builder()
                .propertyPrefixes("genai.locator")
                .serviceName("Tanzu GenAI Locator").build();
    }
}
