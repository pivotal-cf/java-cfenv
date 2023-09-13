/*
 * Copyright 2011-2023 the original author or authors.
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

package io.pivotal.cfenv.profile;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.core.env.ConfigurableEnvironment;

class CloudProfileApplicationListenerTest {

	@Test
	void getOrderTest() {
		CloudProfileApplicationListener cloudProfileApplicationListener =  new CloudProfileApplicationListener();
		Assertions.assertEquals(-2147483645, cloudProfileApplicationListener.getOrder());
	}

	@Test
	void onApplicationEventTest_application_env_prepared_event() {
		CloudProfileApplicationListener cloudProfileApplicationListener = new CloudProfileApplicationListener();

		ApplicationEnvironmentPreparedEvent aepe = Mockito.mock(ApplicationEnvironmentPreparedEvent.class);
		Mockito.when(aepe.getEnvironment()).thenReturn(Mockito.mock(ConfigurableEnvironment.class));

		cloudProfileApplicationListener.onApplicationEvent(aepe);

		Mockito.verify(aepe, Mockito.times(1)).getEnvironment();
	}
}
