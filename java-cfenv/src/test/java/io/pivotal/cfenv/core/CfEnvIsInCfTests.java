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
package io.pivotal.cfenv.core;

import org.junit.Test;

import io.pivotal.cfenv.core.test.CfEnvMock;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Mark Pollack
 * @author David Turanski
 */
public class CfEnvIsInCfTests {

	@Test
	public void testIsInCf() {
		CfEnvMock.configure().mock();
		CfEnv cfEnv = new CfEnv();
		assertThat(cfEnv.isInCf()).isTrue();
	}

	@Test
	public void testIsInCfFalse() {
		CfEnvMock.configure().vcapApplication(null).mock();
		CfEnv cfEnv = new CfEnv();
		assertThat(cfEnv.isInCf()).isFalse();
	}
}
