package io.pivotal.cfenv.boot.genai;

import io.pivotal.cfenv.core.CfCredentials;
import io.pivotal.cfenv.core.CfService;
import io.pivotal.cfenv.spring.boot.CfEnvProcessor;
import io.pivotal.cfenv.spring.boot.CfEnvProcessorProperties;

import java.util.Map;

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
