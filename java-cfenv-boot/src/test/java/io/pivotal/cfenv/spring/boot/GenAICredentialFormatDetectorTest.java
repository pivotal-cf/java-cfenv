package io.pivotal.cfenv.spring.boot;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for GenAICredentialFormatDetector.
 *
 * @author Corby Page
 */
public class GenAICredentialFormatDetectorTest {

    @Test
    void testIsMultiModelFormat_WithNewFormat() {
        Map<String, Object> credentials = new HashMap<>();
        Map<String, Object> endpoint = new HashMap<>();
        endpoint.put("api_base", "https://genai-proxy.sys.example.com/all-models-9afff1f");
        endpoint.put("api_key", "eyJhbGciOiJIUzI1NiJ9...");
        endpoint.put("config_url", "https://genai-proxy.sys.example.com/all-models-9afff1f/config/v1/endpoint");
        endpoint.put("name", "all-models-9afff1f");
        credentials.put("endpoint", endpoint);

        boolean result = GenAICredentialFormatDetector.isMultiModelFormat(credentials);

        assertThat(result).isTrue();
    }

    @Test
    void testIsMultiModelFormat_WithOldFormat() {
        Map<String, Object> credentials = new HashMap<>();
        credentials.put("api_key", "sk-1234567890");
        credentials.put("api_base", "https://api.openai.com");
        credentials.put("model", "gpt-3.5-turbo");

        boolean result = GenAICredentialFormatDetector.isMultiModelFormat(credentials);

        assertThat(result).isFalse();
    }

    @Test
    void testIsMultiModelFormat_WithNullCredentials() {
        boolean result = GenAICredentialFormatDetector.isMultiModelFormat(null);

        assertThat(result).isFalse();
    }

    @Test
    void testIsMultiModelFormat_WithEmptyCredentials() {
        boolean result = GenAICredentialFormatDetector.isMultiModelFormat(new HashMap<>());

        assertThat(result).isFalse();
    }

    @Test
    void testIsMultiModelFormat_WithIncompleteEndpoint() {
        Map<String, Object> credentials = new HashMap<>();
        Map<String, Object> endpoint = new HashMap<>();
        endpoint.put("api_base", "https://genai-proxy.sys.example.com");
        credentials.put("endpoint", endpoint);

        boolean result = GenAICredentialFormatDetector.isMultiModelFormat(credentials);

        assertThat(result).isFalse();
    }

    @Test
    void testExtractEndpoint_Success() {
        Map<String, Object> credentials = createValidMultiModelCredentials();

        Map<String, Object> endpoint = GenAICredentialFormatDetector.extractEndpoint(credentials);

        assertThat(endpoint).isNotNull();
        assertThat(endpoint.get("api_base")).isEqualTo("https://genai-proxy.sys.example.com/all-models-9afff1f");
        assertThat(endpoint.get("api_key")).isEqualTo("eyJhbGciOiJIUzI1NiJ9...");
        assertThat(endpoint.get("config_url")).isEqualTo("https://genai-proxy.sys.example.com/all-models-9afff1f/config/v1/endpoint");
        assertThat(endpoint.get("name")).isEqualTo("all-models-9afff1f");
    }

    @Test
    void testExtractEndpoint_ThrowsForOldFormat() {
        Map<String, Object> credentials = new HashMap<>();
        credentials.put("api_key", "sk-1234567890");

        assertThatThrownBy(() -> GenAICredentialFormatDetector.extractEndpoint(credentials))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not in multi-model format");
    }

    @Test
    void testExtractApiBase() {
        Map<String, Object> credentials = createValidMultiModelCredentials();

        String apiBase = GenAICredentialFormatDetector.extractApiBase(credentials);

        assertThat(apiBase).isEqualTo("https://genai-proxy.sys.example.com/all-models-9afff1f");
    }

    @Test
    void testExtractApiKey() {
        Map<String, Object> credentials = createValidMultiModelCredentials();

        String apiKey = GenAICredentialFormatDetector.extractApiKey(credentials);

        assertThat(apiKey).isEqualTo("eyJhbGciOiJIUzI1NiJ9...");
    }

    @Test
    void testExtractConfigUrl() {
        Map<String, Object> credentials = createValidMultiModelCredentials();

        String configUrl = GenAICredentialFormatDetector.extractConfigUrl(credentials);

        assertThat(configUrl).isEqualTo("https://genai-proxy.sys.example.com/all-models-9afff1f/config/v1/endpoint");
    }

    @Test
    void testExtractEndpointName() {
        Map<String, Object> credentials = createValidMultiModelCredentials();

        String name = GenAICredentialFormatDetector.extractEndpointName(credentials);

        assertThat(name).isEqualTo("all-models-9afff1f");
    }

    private Map<String, Object> createValidMultiModelCredentials() {
        Map<String, Object> credentials = new HashMap<>();
        Map<String, Object> endpoint = new HashMap<>();
        endpoint.put("api_base", "https://genai-proxy.sys.example.com/all-models-9afff1f");
        endpoint.put("api_key", "eyJhbGciOiJIUzI1NiJ9...");
        endpoint.put("config_url", "https://genai-proxy.sys.example.com/all-models-9afff1f/config/v1/endpoint");
        endpoint.put("name", "all-models-9afff1f");
        credentials.put("endpoint", endpoint);
        return credentials;
    }
}