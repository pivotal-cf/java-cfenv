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

import mockit.Mock;
import mockit.MockUp;
import org.junit.Test;

import io.pivotal.cfenv.test.AbstractCfEnvTests;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Stuart Charlton
 */
public class GenAICfEnvProcessorTests extends AbstractCfEnvTests {

	private static final String EXPECTED_TOKEN_FROM_JSON_FILE = "sk-KW5kiNOKDd_1dFxsAjpVa";
	private static final String EXPECTED_URI_FROM_JSON_FILE = "https://genai-proxy.tpcf.io";
	private static final String TEST_GENAI_INFO_JSON = "test-genai-info.json";

	@Test
	public void genaiServiceCreation() {
		mockConnectorLibrary(false);
		mockVcapServices(getServicesPayload(readTestDataFile(TEST_GENAI_INFO_JSON)));
		assertThat(getEnvironment().getProperty("spring.ai.openai.base-url")).isEqualTo(EXPECTED_URI_FROM_JSON_FILE);
		assertThat(getEnvironment().getProperty("spring.ai.openai.api-key")).isEqualTo(EXPECTED_TOKEN_FROM_JSON_FILE);
	}

	private MockUp<?> mockConnectorLibrary(boolean isUsingConnectorLibrary) {
		return new MockUp<ConnectorLibraryDetector>() {
			@Mock
			public boolean isUsingConnectorLibrary() {
				return isUsingConnectorLibrary;
			}
		};
	}
}
