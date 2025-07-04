package io.pivotal.cfenv.spring.boot;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for GenAIModelSelector.
 *
 * @author Corby Page
 */
public class GenAIModelSelectorTest {

    @Test
    void testSelectModel_WithSingleMatchingModel() {
        GenAIModelSelector selector = new GenAIModelSelector();
        List<GenAIModelInfo> models = Arrays.asList(
                new GenAIModelInfo("gpt-4", List.of(GenAIModelInfo.Capability.CHAT)),
                new GenAIModelInfo("text-embedding-ada-002", List.of(GenAIModelInfo.Capability.EMBEDDING))
        );

        Optional<GenAIModelInfo> selected = selector.selectModel(models, GenAIModelInfo.Capability.EMBEDDING);

        assertThat(selected).isPresent();
        assertThat(selected.get().getName()).isEqualTo("text-embedding-ada-002");
    }

    @Test
    void testSelectModel_WithMultipleMatchingModels_SelectsFirst() {
        GenAIModelSelector selector = new GenAIModelSelector();
        List<GenAIModelInfo> models = Arrays.asList(
                new GenAIModelInfo("gpt-3.5-turbo", List.of(GenAIModelInfo.Capability.CHAT)),
                new GenAIModelInfo("gpt-4", List.of(GenAIModelInfo.Capability.CHAT)),
                new GenAIModelInfo("claude-3", List.of(GenAIModelInfo.Capability.CHAT))
        );

        Optional<GenAIModelInfo> selected = selector.selectModel(models, GenAIModelInfo.Capability.CHAT);

        assertThat(selected).isPresent();
        assertThat(selected.get().getName()).isEqualTo("gpt-3.5-turbo");
    }

    @Test
    void testSelectModel_NoMatchingCapability() {
        GenAIModelSelector selector = new GenAIModelSelector();
        List<GenAIModelInfo> models = Arrays.asList(
                new GenAIModelInfo("gpt-4", List.of(GenAIModelInfo.Capability.CHAT)),
                new GenAIModelInfo("claude-3", List.of(GenAIModelInfo.Capability.CHAT))
        );

        Optional<GenAIModelInfo> selected = selector.selectModel(models, GenAIModelInfo.Capability.EMBEDDING);

        assertThat(selected).isEmpty();
    }

    @Test
    void testSelectModel_EmptyModelList() {
        GenAIModelSelector selector = new GenAIModelSelector();

        Optional<GenAIModelInfo> selected = selector.selectModel(Collections.emptyList(), GenAIModelInfo.Capability.CHAT);

        assertThat(selected).isEmpty();
    }

    @Test
    void testSelectModel_NullModelList() {
        GenAIModelSelector selector = new GenAIModelSelector();

        Optional<GenAIModelInfo> selected = selector.selectModel(null, GenAIModelInfo.Capability.CHAT);

        assertThat(selected).isEmpty();
    }
}