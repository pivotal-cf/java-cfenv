package io.pivotal.cfenv.spring.boot;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Selects appropriate GenAI models based on required capabilities.
 *
 * @author Corby Page
 */
public class GenAIModelSelector {

    public enum SelectionStrategy {
        FIRST,          // Select the first matching model
        LAST,           // Select the last matching model
        PREFER_NAMED    // Prefer models with specific names
    }

    private final SelectionStrategy strategy;
    private final List<String> preferredModelNames;

    public GenAIModelSelector() {
        this(SelectionStrategy.FIRST, Collections.emptyList());
    }

    public GenAIModelSelector(SelectionStrategy strategy) {
        this(strategy, Collections.emptyList());
    }

    public GenAIModelSelector(SelectionStrategy strategy, List<String> preferredModelNames) {
        this.strategy = strategy;
        this.preferredModelNames = preferredModelNames != null ?
                new ArrayList<>(preferredModelNames) :
                Collections.emptyList();
    }

    public Optional<GenAIModelInfo> selectModel(List<GenAIModelInfo> models,
                                                GenAIModelInfo.Capability requiredCapability) {
        if (models == null || models.isEmpty()) {
            return Optional.empty();
        }

        List<GenAIModelInfo> matchingModels = models.stream()
                .filter(model -> model.hasCapability(requiredCapability))
                .collect(Collectors.toList());

        if (matchingModels.isEmpty()) {
            return Optional.empty();
        }

        GenAIModelInfo selected = applySelectionStrategy(matchingModels);

        return Optional.of(selected);
    }

    public Optional<GenAIModelInfo> selectModelWithAllCapabilities(List<GenAIModelInfo> models,
                                                                   GenAIModelInfo.Capability... requiredCapabilities) {
        if (models == null || models.isEmpty() || requiredCapabilities.length == 0) {
            return Optional.empty();
        }

        List<GenAIModelInfo.Capability> capabilityList = Arrays.asList(requiredCapabilities);

        List<GenAIModelInfo> matchingModels = models.stream()
                .filter(model -> capabilityList.stream().allMatch(model::hasCapability))
                .collect(Collectors.toList());

        if (matchingModels.isEmpty()) {
            return Optional.empty();
        }

        GenAIModelInfo selected = applySelectionStrategy(matchingModels);

        return Optional.of(selected);
    }

    private GenAIModelInfo applySelectionStrategy(List<GenAIModelInfo> matchingModels) {
        switch (strategy) {
            case LAST:
                return matchingModels.get(matchingModels.size() - 1);

            case PREFER_NAMED:
                // Try to find a preferred model
                for (String preferredName : preferredModelNames) {
                    Optional<GenAIModelInfo> preferred = matchingModels.stream()
                            .filter(model -> model.getName().equals(preferredName))
                            .findFirst();
                    if (preferred.isPresent()) {
                        return preferred.get();
                    }
                }
                return matchingModels.get(0);

            case FIRST:
            default:
                return matchingModels.get(0);
        }
    }

    public static GenAIModelSelector withPreferredModels(String... modelNames) {
        return new GenAIModelSelector(SelectionStrategy.PREFER_NAMED, Arrays.asList(modelNames));
    }

    public static GenAIModelSelector forChat() {
        return withPreferredModels(
                "gpt-4",
                "gpt-3.5-turbo",
                "claude-3-opus",
                "claude-3-sonnet",
                "llama3.2"
        );
    }

    public static GenAIModelSelector forEmbedding() {
        return withPreferredModels(
                "text-embedding-ada-002",
                "text-embedding-3-large",
                "text-embedding-3-small",
                "mxbai-embed-large"
        );
    }
}