package io.pivotal.cfenv.spring.boot;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represents the response from the GenAI config URL endpoint.
 * This class is designed to be deserialized from the JSON response.
 *
 * @author Corby Page
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GenAIConfigResponse {

    private String name;
    private String description;
    private String wireFormat;
    private List<AdvertisedModel> advertisedModels;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AdvertisedModel {
        private String name;
        private List<String> capabilities;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<String> getCapabilities() {
            return capabilities != null ? capabilities : Collections.emptyList();
        }

        public void setCapabilities(List<String> capabilities) {
            this.capabilities = capabilities;
        }

        public GenAIModelInfo toModelInfo() {
            List<GenAIModelInfo.Capability> modelCapabilities = getCapabilities().stream()
                    .map(cap -> {
                        try {
                            return GenAIModelInfo.Capability.valueOf(cap.toUpperCase());
                        } catch (IllegalArgumentException e) {
                            // Log warning about unknown capability
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            return new GenAIModelInfo(name, modelCapabilities);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getWireFormat() {
        return wireFormat;
    }

    public void setWireFormat(String wireFormat) {
        this.wireFormat = wireFormat;
    }

    public List<AdvertisedModel> getAdvertisedModels() {
        return advertisedModels != null ? advertisedModels : Collections.emptyList();
    }

    public void setAdvertisedModels(List<AdvertisedModel> advertisedModels) {
        this.advertisedModels = advertisedModels;
    }

    /**
     * Converts the advertised models to a list of GenAIModelInfo objects.
     */
    public List<GenAIModelInfo> getModels() {
        return getAdvertisedModels().stream()
                .map(AdvertisedModel::toModelInfo)
                .collect(Collectors.toList());
    }
}