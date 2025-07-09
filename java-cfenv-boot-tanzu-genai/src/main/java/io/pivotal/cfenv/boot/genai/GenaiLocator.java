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
package io.pivotal.cfenv.boot.genai;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;

/**
 * Locates available models and mcp servers from ai-servers config endpoint
 *
 * @author Gareth Evans
 **/
public interface GenaiLocator {

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
