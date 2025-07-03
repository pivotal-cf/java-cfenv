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
    void testSelectModel_WithMultipleMatchingModels_FirstStrategy() {
        GenAIModelSelector selector = new GenAIModelSelector(GenAIModelSelector.SelectionStrategy.FIRST);
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
    void testSelectModel_WithMultipleMatchingModels_LastStrategy() {
        GenAIModelSelector selector = new GenAIModelSelector(GenAIModelSelector.SelectionStrategy.LAST);
        List<GenAIModelInfo> models = Arrays.asList(
                new GenAIModelInfo("gpt-3.5-turbo", List.of(GenAIModelInfo.Capability.CHAT)),
                new GenAIModelInfo("gpt-4", List.of(GenAIModelInfo.Capability.CHAT)),
                new GenAIModelInfo("claude-3", List.of(GenAIModelInfo.Capability.CHAT))
        );

        Optional<GenAIModelInfo> selected = selector.selectModel(models, GenAIModelInfo.Capability.CHAT);

        assertThat(selected).isPresent();
        assertThat(selected.get().getName()).isEqualTo("claude-3");
    }

    @Test
    void testSelectModel_WithPreferredModels() {
        GenAIModelSelector selector = GenAIModelSelector.withPreferredModels("gpt-4", "claude-3");
        List<GenAIModelInfo> models = Arrays.asList(
                new GenAIModelInfo("llama2", List.of(GenAIModelInfo.Capability.CHAT)),
                new GenAIModelInfo("gpt-3.5-turbo", List.of(GenAIModelInfo.Capability.CHAT)),
                new GenAIModelInfo("gpt-4", List.of(GenAIModelInfo.Capability.CHAT))
        );

        Optional<GenAIModelInfo> selected = selector.selectModel(models, GenAIModelInfo.Capability.CHAT);

        assertThat(selected).isPresent();
        assertThat(selected.get().getName()).isEqualTo("gpt-4");
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
    void testSelectModelWithAllCapabilities() {
        GenAIModelSelector selector = new GenAIModelSelector();
        List<GenAIModelInfo> models = Arrays.asList(
                new GenAIModelInfo("basic-model", List.of(GenAIModelInfo.Capability.CHAT)),
                new GenAIModelInfo("advanced-model", Arrays.asList(
                        GenAIModelInfo.Capability.CHAT,
                        GenAIModelInfo.Capability.TOOLS
                )),
                new GenAIModelInfo("vision-model", Arrays.asList(
                        GenAIModelInfo.Capability.CHAT,
                        GenAIModelInfo.Capability.VISION
                ))
        );

        Optional<GenAIModelInfo> selected = selector.selectModelWithAllCapabilities(
                models,
                GenAIModelInfo.Capability.CHAT,
                GenAIModelInfo.Capability.TOOLS
        );

        assertThat(selected).isPresent();
        assertThat(selected.get().getName()).isEqualTo("advanced-model");
    }

    @Test
    void testForChat_Selector() {
        GenAIModelSelector selector = GenAIModelSelector.forChat();
        List<GenAIModelInfo> models = Arrays.asList(
                new GenAIModelInfo("llama3.2", List.of(GenAIModelInfo.Capability.CHAT)),
                new GenAIModelInfo("custom-model", List.of(GenAIModelInfo.Capability.CHAT)),
                new GenAIModelInfo("gpt-3.5-turbo", List.of(GenAIModelInfo.Capability.CHAT))
        );

        Optional<GenAIModelInfo> selected = selector.selectModel(models, GenAIModelInfo.Capability.CHAT);

        assertThat(selected).isPresent();
        assertThat(selected.get().getName()).isEqualTo("gpt-3.5-turbo"); // Preferred model
    }

    @Test
    void testForEmbedding_Selector() {
        GenAIModelSelector selector = GenAIModelSelector.forEmbedding();
        List<GenAIModelInfo> models = Arrays.asList(
                new GenAIModelInfo("custom-embed", List.of(GenAIModelInfo.Capability.EMBEDDING)),
                new GenAIModelInfo("mxbai-embed-large", List.of(GenAIModelInfo.Capability.EMBEDDING)),
                new GenAIModelInfo("text-embedding-ada-002", List.of(GenAIModelInfo.Capability.EMBEDDING))
        );

        Optional<GenAIModelInfo> selected = selector.selectModel(models, GenAIModelInfo.Capability.EMBEDDING);

        assertThat(selected).isPresent();
        assertThat(selected.get().getName()).isEqualTo("text-embedding-ada-002"); // Preferred model
    }
}