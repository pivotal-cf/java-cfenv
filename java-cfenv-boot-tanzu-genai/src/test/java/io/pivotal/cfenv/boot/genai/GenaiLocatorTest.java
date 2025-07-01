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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

public class GenaiLocatorTest {

  private MockRestServiceServer server;

  private GenaiLocator locator;

  String configDetails = """
                {
                   "name": "test",
                   "description": "test",
                   "wireFormat": "OPENAI",
                   "advertisedModels": [
                      {
                         "name": "chat-1",
                         "description": "",
                         "capabilities": ["CHAT"]
                      },
                      {
                         "name": "chat-2",
                         "description": "",
                         "capabilities": ["CHAT", "TOOLS"]
                      },
                      {
                         "name": "embedding-1",
                         "description": "",
                         "capabilities": ["EMBEDDING"]
                      }
                   ],
                   "advertisedMcpServers": [
                      {
                         "url":"http://localhost:1234/"
                      },
                      {
                         "url":"http://localhost:1235/"
                      },
                      {
                         "url":"http://localhost:1236/"
                      }
                   ]
                }
                """;

  @BeforeEach
  public void setup() {
    RestClient.Builder restClientBuilder = RestClient.builder();
    server = MockRestServiceServer.bindTo(restClientBuilder).build();

    server.expect(MockRestRequestMatchers.requestTo("http://ai-server/test/config/v1/endpoint"))
            .andRespond(MockRestResponseCreators.withSuccess(configDetails, MediaType.APPLICATION_JSON));

    locator =
        new DefaultGenaiLocator(restClientBuilder, "http://ai-server/test/config/v1/endpoint", "fake-bearer-token", "http://ai-server/test");
  }

  @AfterEach
  public void after() {
    server.verify();
  }

  @Test
  public void canListModels() {
    List<String> models = locator.getModelNames();
    assertThat(models).isNotNull();
    assertThat(models).hasSize(3);
    assertThat(models).contains("chat-1", "chat-2", "embedding-1");
  }

  @Test
  public void canListModelsByChatCapability() {
    List<String> chatModels = locator.getModelNamesByCapability("CHAT");
    assertThat(chatModels).isNotNull();
    assertThat(chatModels).hasSize(2);
    assertThat(chatModels).contains("chat-1", "chat-2");
  }

  @Test
  public void canListModelsByToolsCapability() {
    List<String> toolModels = locator.getModelNamesByCapability("TOOLS");
    assertThat(toolModels).isNotNull();
    assertThat(toolModels).hasSize(1);
    assertThat(toolModels).contains("chat-2");
  }

  @Test
  public void canGetModelByName() {
    ChatModel chatModel = locator.getChatModelByName("chat-1");
    assertThat(chatModel).isNotNull();
  }

  @Test
  public void canGetFirstAvailableChatModel() {
    ChatModel firstAvailableChatModel = locator.getFirstAvailableChatModel();
    assertThat(firstAvailableChatModel).isNotNull();
  }

  @Test
  public void canGetFirstAvailableToolModel() {
    ChatModel firstAvailableToolModel = locator.getFirstAvailableToolModel();
    assertThat(firstAvailableToolModel).isNotNull();
  }

  @Test
  public void canGetEmbeddingModelByName() {
    EmbeddingModel embeddingModel = locator.getEmbeddingModelByName("embedding-1");
    assertThat(embeddingModel).isNotNull();
  }

  @Test
  public void canGetFirstAvailableEmbeddingModel() {
    EmbeddingModel firstAvailableEmbeddingModel = locator.getFirstAvailableEmbeddingModel();
    assertThat(firstAvailableEmbeddingModel).isNotNull();
  }

  @Test
  public void canListMcpServers() {
    List<GenaiLocator.McpConnectivity> mcpServers = locator.getMcpServers();
    assertThat(mcpServers).isNotNull();
    assertThat(mcpServers).hasSize(3);
  }
}
