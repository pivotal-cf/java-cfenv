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
        LAST            // Select the last matching model
    }

    private final SelectionStrategy strategy;

    public GenAIModelSelector() {
        this(SelectionStrategy.FIRST);
    }

    public GenAIModelSelector(SelectionStrategy strategy) {
        this.strategy = strategy;
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
            case FIRST:
            default:
                return matchingModels.get(0);
        }
    }
}