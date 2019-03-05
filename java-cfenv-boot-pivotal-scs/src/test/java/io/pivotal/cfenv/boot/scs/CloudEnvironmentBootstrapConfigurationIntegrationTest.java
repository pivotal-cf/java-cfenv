/*
 * Copyright 2015 the original author or authors.
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

package io.pivotal.cfenv.boot.scs;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;

/**
 * @author Craig Walls
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = CloudEnvironmentBootstrapConfigurationIntegrationTest.TestApplication.class,
		properties = {"vcap.application.name=app-name"})
public class CloudEnvironmentBootstrapConfigurationIntegrationTest {

	@Autowired
	private ApplicationContext context;

	@Test
	public void environment() {
		Environment env = context.getEnvironment();
		assertEquals("app-name", env.getProperty("spring.application.name"));
	}

	@SpringBootApplication
	static class TestApplication {}

}
