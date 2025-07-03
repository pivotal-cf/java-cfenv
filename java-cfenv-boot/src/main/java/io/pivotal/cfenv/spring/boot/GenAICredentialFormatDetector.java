package io.pivotal.cfenv.spring.boot;

import java.util.Map;

/**
 * Utility class to detect the format of GenAI service credentials.
 * Supports both the legacy single-model format and the new multi-model format
 * introduced in GenAI on Tanzu Platform for Cloud Foundry 10.2.
 *
 * @author Corby Page
 */
public class GenAICredentialFormatDetector {

    public static boolean isMultiModelFormat(Map<String, Object> credentials) {
        if (credentials == null || credentials.isEmpty()) {
            return false;
        }

        Object endpoint = credentials.get("endpoint");
        if (!(endpoint instanceof Map)) {
            return false;
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> endpointMap = (Map<String, Object>) endpoint;

        return endpointMap.containsKey("api_base") &&
                endpointMap.containsKey("api_key") &&
                endpointMap.containsKey("config_url");
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> extractEndpoint(Map<String, Object> credentials) {
        if (!isMultiModelFormat(credentials)) {
            throw new IllegalArgumentException("Credentials are not in multi-model format");
        }

        return (Map<String, Object>) credentials.get("endpoint");
    }

    public static String extractApiBase(Map<String, Object> credentials) {
        Map<String, Object> endpoint = extractEndpoint(credentials);
        return (String) endpoint.get("api_base");
    }

    public static String extractApiKey(Map<String, Object> credentials) {
        Map<String, Object> endpoint = extractEndpoint(credentials);
        return (String) endpoint.get("api_key");
    }

    public static String extractConfigUrl(Map<String, Object> credentials) {
        Map<String, Object> endpoint = extractEndpoint(credentials);
        return (String) endpoint.get("config_url");
    }

    public static String extractEndpointName(Map<String, Object> credentials) {
        Map<String, Object> endpoint = extractEndpoint(credentials);
        return (String) endpoint.get("name");
    }
}