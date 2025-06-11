/*
 * Copyright 2024 the original author or authors.
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

import org.junit.Test;

import io.pivotal.cfenv.test.AbstractCfEnvTests;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Stuart Charlton
 * @author Ed King
 * @author Gareth Evans
 */
public class GenAIChatCfEnvProcessorTests extends AbstractCfEnvTests {

	@Test
	public void testGenAIBootPropertiesWithChatModelCapability() {
		String TEST_GENAI_JSON_FILE = "test-genai-chat-model.json";

		String EXPECTED_URI_FROM_JSON_FILE = "https://genai-proxy.tpcf.io";
		String EXPECTED_TOKEN_FROM_JSON_FILE = "sk-KW5kiNOKDd_1dFxsAjpVa";
		String EXPECTED_MODEL_FROM_JSON_FILE = "meta-llama/Meta-Llama-3-8B";

		mockVcapServices(getServicesPayload(readTestDataFile(TEST_GENAI_JSON_FILE)));

		assertThat(getEnvironment().getProperty("spring.ai.openai.api-key")).isEqualTo("redundant");

		assertThat(getEnvironment().getProperty("spring.ai.openai.chat.base-url")).isEqualTo(EXPECTED_URI_FROM_JSON_FILE);
		assertThat(getEnvironment().getProperty("spring.ai.openai.chat.api-key")).isEqualTo(EXPECTED_TOKEN_FROM_JSON_FILE);
		assertThat(getEnvironment().getProperty("spring.ai.openai.chat.options.model")).isEqualTo(EXPECTED_MODEL_FROM_JSON_FILE);

		assertThat(getEnvironment().getProperty("spring.ai.openai.embedding.options.model")).isNull();
		assertThat(getEnvironment().getProperty("spring.ai.openai.image.options.model")).isNull();
		assertThat(getEnvironment().getProperty("spring.ai.openai.audio.transcription.options.model")).isNull();
		assertThat(getEnvironment().getProperty("spring.ai.openai.audio.speech.options.model")).isNull();
	}

	@Test
	public void testGenAIBootPropertiesWithChatModelCapabilityEndpointFormat() {
		String TEST_GENAI_JSON_FILE = "test-genai-endpoint-chat-model.json";

		mockVcapServices(getServicesPayload(readTestDataFile(TEST_GENAI_JSON_FILE)));

		assertThat(getEnvironment().getProperty("spring.ai.openai.api-key")).isNull();
		assertThat(getEnvironment().getProperty("spring.ai.openai.chat.base-url")).isNull();
		assertThat(getEnvironment().getProperty("spring.ai.openai.chat.api-key")).isNull();
		assertThat(getEnvironment().getProperty("spring.ai.openai.chat.options.model")).isNull();

		assertThat(getEnvironment().getProperty("spring.ai.openai.embedding.options.model")).isNull();
		assertThat(getEnvironment().getProperty("spring.ai.openai.image.options.model")).isNull();
		assertThat(getEnvironment().getProperty("spring.ai.openai.audio.transcription.options.model")).isNull();
		assertThat(getEnvironment().getProperty("spring.ai.openai.audio.speech.options.model")).isNull();
	}
}
