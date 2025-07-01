package io.pivotal.cfenv.boot.genai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;

import java.util.List;
import java.util.Map;

public interface GenAILocator {

  List<String> getModelNames();

  List<String> getModelNamesByCapability(String capability);

  List<String> getModelNamesByLabels(Map<String, String> labels);

  List<String> getModelNamesByCapabilityAndLabels(String capability, Map<String, String> labels);

  ChatModel getChatModelByName(String name);

  List<ChatModel> getChatModelsByLabels(Map<String, String> labels);

  ChatModel getFirstAvailableChatModel();

  ChatModel getFirstAvailableChatModelByLabels(Map<String, String> labels);

  List<ChatModel> getToolModelsByLabels(Map<String, String> labels);

  ChatModel getFirstAvailableToolModel();

  ChatModel getFirstAvailableToolModelByLabels(Map<String, String> labels);

  EmbeddingModel getEmbeddingModelByName(String name);

  List<EmbeddingModel> getEmbeddingModelsByLabels(Map<String, String> labels);

  EmbeddingModel getFirstAvailableEmbeddingModel();

  EmbeddingModel getFirstAvailableEmbeddingModelByLabels(Map<String, String> labels);

  List<McpConnectivity> getMcpServers();

  @JsonIgnoreProperties(ignoreUnknown = true)
  record McpConnectivity(String url) {}
}
