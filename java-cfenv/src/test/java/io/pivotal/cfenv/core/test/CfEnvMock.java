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

package io.pivotal.cfenv.core.test;

import java.util.HashMap;
import java.util.Map;

import mockit.MockUp;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

import io.pivotal.cfenv.core.CfEnv;

/**
 * @author David Turanski
 **/
public class CfEnvMock {
	private MockUp<?> mockUp;

	private CfEnvMock(String vcapServicesJson, String vcapApplicationJson) {

		Map<String, String> env = System.getenv();
		this.mockUp = new MockUp<System>() {
			@mockit.Mock
			public String getenv(String name) {
				if (name.equalsIgnoreCase(CfEnv.VCAP_SERVICES)) {
					return vcapServicesJson;
				}
				else if (name.equalsIgnoreCase(CfEnv.VCAP_APPLICATION)) {
					return vcapApplicationJson;
				}
				return env.get(name);
			}

			@mockit.Mock
			public Map getenv() {
				Map<String, String> finalMap = new HashMap<>();
				finalMap.putAll(env);
				finalMap.put("VCAP_SERVICES", vcapServicesJson);
				return finalMap;
			}
		};
	}

	public MockUp<?> getMockUp() {
		return this.mockUp;
	}

	public static class Configurer {
		private String vcapServices;

		private String vcapApplication;

		private static String DEFAULT_VCAP_APPLICATION_PATH = "test/vcap-application.json";

		private Configurer() {
			this.vcapApplicationResource(DEFAULT_VCAP_APPLICATION_PATH);
		}

		public Configurer vcapServices(String vcapServicesJson) {
			this.vcapServices = vcapServicesJson;
			return this;
		}

		public Configurer vcapServicesResource(String resourcePath) {
			Assert.hasText(resourcePath, "'resourcePath' cannot be empty or null");
			this.vcapServices = resourceToString(new ClassPathResource(resourcePath));
			return this;
		}

		public Configurer vcapApplication(String vcapApplicationJson) {
			this.vcapApplication = vcapApplicationJson;
			return this;
		}

		public Configurer vcapApplicationResource(String resourcePath) {
			Assert.hasText(resourcePath, "'resourcePath' cannot be empty or null");
			this.vcapApplication = resourceToString(new ClassPathResource(resourcePath));
			return this;
		}

		private String resourceToString(Resource resource) {
			String contents;
			try {
				java.util.Scanner s = new java.util.Scanner(resource.getInputStream()).useDelimiter("\\A");
				contents = s.hasNext() ? s.next() : "";
			}
			catch (Exception e) {
				throw new RuntimeException(e.getMessage(), e);
			}
			return contents;
		}

		public CfEnvMock mock() {
			return new CfEnvMock(this.vcapServices, this.vcapApplication);
		}

	}

	public static Configurer configure() {
		return new Configurer();
	}
}
