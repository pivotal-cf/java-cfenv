package io.pivotal.cfenv.boot.genai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.web.client.RestClient;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class DefaultGenAILocator implements GenAILocator {

  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultGenAILocator.class);

  private final String configUrl;
  private final String apiKey;
  private final String apiBase;

  public DefaultGenAILocator(String configUrl, String apiKey, String apiBase) {
    this.configUrl = configUrl;
    this.apiKey = apiKey;
    this.apiBase = apiBase;
  }

  @Override
  public List<String> getModelNames() {
    return getModelNamesByCapability(null);
  }

  @Override
  public List<String> getModelNamesByCapability(String capability) {
    return getModelNamesByCapabilityAndLabels(capability, Map.of());
  }

  @Override
  public List<String> getModelNamesByLabels(Map<String, String> labels) {
    return getModelNamesByCapabilityAndLabels(null, labels);
  }

  @Override
  public List<String> getModelNamesByCapabilityAndLabels(
      String capability, Map<String, String> labels) {
    List<ModelConnectivity> models = getAllModelConnectivityDetails();

    return models.stream()
        .filter(filterModelConnectivityOnCapability(capability))
        .filter(filterModelConnectivityOnLabels(labels))
        .map(a -> a.name)
        .toList();
  }

  @Override
  public ChatModel getChatModelByName(String name) {
    List<ModelConnectivity> models = getAllModelConnectivityDetails();

    return models.stream()
        .filter(filterModelConnectivityOnCapability("CHAT"))
        .filter(c -> c.name().equals(name))
        .map(DefaultGenAILocator::createChatModel)
        .findFirst()
        .orElseThrow(
            () -> new RuntimeException("Unable to find chat model with name '" + name + "'"));
  }

  @Override
  public List<ChatModel> getChatModelsByLabels(Map<String, String> labels) {
    List<ModelConnectivity> models = getAllModelConnectivityDetails();

    return models.stream()
        .filter(filterModelConnectivityOnCapability("CHAT"))
        .filter(filterModelConnectivityOnLabels(labels))
        .map(DefaultGenAILocator::createChatModel)
        .toList();
  }

  @Override
  public ChatModel getFirstAvailableChatModel() {
    List<ModelConnectivity> models = getAllModelConnectivityDetails();

    return models.stream()
        .filter(filterModelConnectivityOnCapability("CHAT"))
        .map(DefaultGenAILocator::createChatModel)
        .findFirst()
        .orElseThrow(() -> new RuntimeException("Unable to find first chat model"));
  }

  @Override
  public ChatModel getFirstAvailableChatModelByLabels(Map<String, String> labels) {
    List<ModelConnectivity> models = getAllModelConnectivityDetails();

    return models.stream()
        .filter(filterModelConnectivityOnCapability("CHAT"))
        .filter(filterModelConnectivityOnLabels(labels))
        .map(DefaultGenAILocator::createChatModel)
        .findFirst()
        .orElseThrow(() -> new RuntimeException("Unable to find first chat model"));
  }

  @Override
  public List<ChatModel> getToolModelsByLabels(Map<String, String> labels) {
    List<ModelConnectivity> models = getAllModelConnectivityDetails();

    return models.stream()
        .filter(filterModelConnectivityOnCapability("TOOLS"))
        .filter(filterModelConnectivityOnLabels(labels))
        .map(DefaultGenAILocator::createChatModel)
        .toList();
  }

  @Override
  public ChatModel getFirstAvailableToolModel() {
    List<ModelConnectivity> models = getAllModelConnectivityDetails();

    return models.stream()
        .filter(filterModelConnectivityOnCapability("TOOLS"))
        .map(DefaultGenAILocator::createChatModel)
        .findFirst()
        .orElseThrow(() -> new RuntimeException("Unable to find first tool model"));
  }

  @Override
  public ChatModel getFirstAvailableToolModelByLabels(Map<String, String> labels) {
    List<ModelConnectivity> models = getAllModelConnectivityDetails();

    return models.stream()
        .filter(filterModelConnectivityOnCapability("TOOLS"))
        .filter(filterModelConnectivityOnLabels(labels))
        .map(DefaultGenAILocator::createChatModel)
        .findFirst()
        .orElseThrow(() -> new RuntimeException("Unable to find first tool model"));
  }

  private static ChatModel createChatModel(ModelConnectivity c) {
    OpenAiApi api = OpenAiApi.builder().apiKey(c.apiKey()).baseUrl(c.apiBase()).build();
    return OpenAiChatModel.builder()
        .defaultOptions(OpenAiChatOptions.builder().model(c.name()).build())
        .openAiApi(api)
        .build();
  }

  @Override
  public EmbeddingModel getEmbeddingModelByName(String name) {
    List<ModelConnectivity> models = getAllModelConnectivityDetails();

    return models.stream()
        .filter(filterModelConnectivityOnCapability("EMBEDDING"))
        .filter(c -> c.name().equals(name))
        .map(DefaultGenAILocator::createEmbeddingModel)
        .findFirst()
        .orElseThrow(
            () -> new RuntimeException("Unable to find embedding model with name '" + name + "'"));
  }

  @Override
  public List<EmbeddingModel> getEmbeddingModelsByLabels(Map<String, String> labels) {
    List<ModelConnectivity> models = getAllModelConnectivityDetails();

    return models.stream()
        .filter(filterModelConnectivityOnCapability("EMBEDDING"))
        .filter(filterModelConnectivityOnLabels(labels))
        .map(DefaultGenAILocator::createEmbeddingModel)
        .toList();
  }

  @Override
  public EmbeddingModel getFirstAvailableEmbeddingModel() {
    List<ModelConnectivity> models = getAllModelConnectivityDetails();

    return models.stream()
        .filter(filterModelConnectivityOnCapability("EMBEDDING"))
        .map(DefaultGenAILocator::createEmbeddingModel)
        .findFirst()
        .orElseThrow(() -> new RuntimeException("Unable to find first embedding model"));
  }

  @Override
  public EmbeddingModel getFirstAvailableEmbeddingModelByLabels(Map<String, String> labels) {
    List<ModelConnectivity> models = getAllModelConnectivityDetails();

    return models.stream()
        .filter(filterModelConnectivityOnCapability("EMBEDDING"))
        .filter(filterModelConnectivityOnLabels(labels))
        .map(DefaultGenAILocator::createEmbeddingModel)
        .findFirst()
        .orElseThrow(() -> new RuntimeException("Unable to find first embedding model"));
  }

  private static EmbeddingModel createEmbeddingModel(ModelConnectivity c) {
    OpenAiApi api = OpenAiApi.builder().apiKey(c.apiKey()).baseUrl(c.apiBase()).build();
    return new OpenAiEmbeddingModel(
        api, MetadataMode.EMBED, OpenAiEmbeddingOptions.builder().model(c.name()).build());
  }

  @Override
  public List<McpConnectivity> getMcpServers() {
    return getAllMcpConnectivityDetails();
  }

  private List<ModelConnectivity> getAllModelConnectivityDetails() {
    ConfigEndpoint e = getEndpointConfig();
    return e.advertisedModels
            .stream()
            .map( a ->
                new ModelConnectivity(
                    a.name(),
                    a.capabilities(),
                    a.labels(),
                    apiKey,
                    apiBase + e.wireFormat().toLowerCase())
            )
            .toList();
  }

  private List<McpConnectivity> getAllMcpConnectivityDetails() {
    return getEndpointConfig()
            .advertisedMcpServers
            .stream()
            .map(m -> new McpConnectivity(m.url()))
            .toList();
  }

  private ConfigEndpoint getEndpointConfig() {
      RestClient client = RestClient.builder().build();
      return client
          .get()
          .uri(configUrl)
          .header("Authorization", "Bearer " + apiKey)
          .retrieve()
          .body(ConfigEndpoint.class);
  }

  private Predicate<ModelConnectivity> filterModelConnectivityOnLabels(Map<String, String> labels) {
    return modelConnectivity -> {
      if (labels == null || labels.isEmpty()) {
        return true;
      }

      if (modelConnectivity.labels() == null) {
        return false;
      }

      return modelConnectivity.labels().entrySet().containsAll(labels.entrySet());
    };
  }

  private Predicate<ModelConnectivity> filterModelConnectivityOnCapability(String capability) {
    return modelConnectivity -> {
      if (capability == null || capability.isEmpty()) {
        return true;
      }

      if (modelConnectivity.capabilities() == null) {
        return false;
      }

      return modelConnectivity.capabilities().contains(capability);
    };
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  private record ModelConnectivity(
      String name,
      List<String> capabilities,
      Map<String, String> labels,
      String apiKey,
      String apiBase) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  private record ConfigEndpoint(
      @JsonProperty("name") String name,
      @JsonProperty("description") String description,
      @JsonProperty("wireFormat") String wireFormat,
      @JsonProperty("advertisedModels") List<ConfigAdvertisedModel> advertisedModels,
      @JsonProperty("advertisedMcpServers") List<ConfigAdvertisedMcpServer> advertisedMcpServers) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  private record ConfigAdvertisedModel(
      @JsonProperty("name") String name,
      @JsonProperty("description") String description,
      @JsonProperty("capabilities") List<String> capabilities,
      @JsonProperty("labels") Map<String, String> labels) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  private record ConfigAdvertisedMcpServer(@JsonProperty("url") String url) {}
}
