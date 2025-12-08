/*
 * Copyright 2020 the original author or authors.
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
import org.junit.jupiter.api.Test;

import io.pivotal.cfenv.test.AbstractCfEnvTests;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Dylan Roberts
 */
public class VaultCfEnvProcessorTests extends AbstractCfEnvTests {

	private static final String EXPECTED_TOKEN_FROM_JSON_FILE = "s.qgVrPa3eKawwDDkeOSXUaWZq";
	private static final String EXPECTED_URI_FROM_JSON_FILE = "https://ae585ec6.ngrok.io/";
	private static final String TEST_VAULT_INFO_JSON = "test-vault-info.json";

	@Test
	public void vaultServiceCreation() {
		mockConnectorLibrary(false);
		mockVcapServices(getServicesPayload(readTestDataFile(TEST_VAULT_INFO_JSON)));
		assertThat(getEnvironment().getProperty("spring.cloud.vault.uri")).isEqualTo(EXPECTED_URI_FROM_JSON_FILE);
		assertThat(getEnvironment().getProperty("spring.cloud.vault.token")).isEqualTo(EXPECTED_TOKEN_FROM_JSON_FILE);
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
