/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.pivotal.cfenv.boot.sso;

import mockit.MockUp;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.test.context.junit4.SpringRunner;

import io.pivotal.cfenv.spring.boot.CfEnvProcessor;
import io.pivotal.cfenv.spring.boot.ConnectorLibraryDetector;

/**
 * @author Mark Pollack
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = BootIntegrationTest.TestConfig.class)
public class BootIntegrationTest {

	public static MockUp<?> mockConnectors(boolean usingConnector) {
		return new MockUp<ConnectorLibraryDetector>() {
			@mockit.Mock
			public boolean isUsingConnectorLibrary() {
				return usingConnector;
			}
		};
	}

	@Before
	public void setUp() {
		mockConnectors(true);
	}

	@Test
	public void test() {
		// Should not throw exception
		SpringFactoriesLoader.loadFactories(CfEnvProcessor.class, getClass().getClassLoader());
	}

	@Configuration
	static class TestConfig {

	}
}
