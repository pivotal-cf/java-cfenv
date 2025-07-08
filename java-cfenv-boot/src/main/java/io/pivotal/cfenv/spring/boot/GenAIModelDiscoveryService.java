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
package io.pivotal.cfenv.spring.boot;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Service for discovering available GenAI models by querying the config URL.
 *
 * @author Corby Page
 */
public class GenAIModelDiscoveryService {

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration CACHE_TTL = Duration.ofMinutes(5);

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final Map<String, CachedModels> cache;

    /**
     * Internal class to hold cached model information with timestamp.
     */
    private static class CachedModels {
        private final List<GenAIModelInfo> models;
        private final long timestamp;

        CachedModels(List<GenAIModelInfo> models) {
            this.models = models;
            this.timestamp = System.currentTimeMillis();
        }

        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_TTL.toMillis();
        }
    }

    public GenAIModelDiscoveryService() {
        this(HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(5))
                        .build(),
                new ObjectMapper());
    }

    GenAIModelDiscoveryService(HttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.cache = new ConcurrentHashMap<>();
    }

    public List<GenAIModelInfo> discoverModels(String configUrl, String apiKey) {
        if (configUrl == null || apiKey == null) {
            return Collections.emptyList();
        }

        CachedModels cached = cache.get(configUrl);
        if (cached != null && !cached.isExpired()) {
            return cached.models;
        }

        try {
            List<GenAIModelInfo> models = fetchModelsFromConfigUrl(configUrl, apiKey);

            // Cache the results
            cache.put(configUrl, new CachedModels(models));

            return models;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private List<GenAIModelInfo> fetchModelsFromConfigUrl(String configUrl, String apiKey)
            throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(configUrl))
                .timeout(REQUEST_TIMEOUT)
                .header("Authorization", "Bearer " + apiKey)
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Config URL returned status " + response.statusCode() +
                    ": " + response.body());
        }

        GenAIConfigResponse configResponse = objectMapper.readValue(response.body(), GenAIConfigResponse.class);
        return configResponse.getModels();
    }

    public void clearCache() {
        cache.clear();
    }

    public void clearCache(String configUrl) {
        cache.remove(configUrl);
    }
}
