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

package io.pivotal.cfenv.test;

import java.util.Map;

import mockit.MockUp;

import org.springframework.core.io.Resource;

/**
 * @author David Turanski
 **/
public class CfEnvTestUtils {

	public static void mockVcapServicesFromString(String serviceJson) {
		Map<String, String> env = System.getenv();
		new MockUp<System>() {
			@mockit.Mock
			public String getenv(String name) {
				if (name.equalsIgnoreCase("VCAP_SERVICES")) {
					return serviceJson;
				}
				return env.get(name);
			}
		};

	}

	public static void mockVcapServicesFromResource(Resource vcapServicesResource) {
		try {
			java.util.Scanner s = new java.util.Scanner(vcapServicesResource.getInputStream()).useDelimiter("\\A");
			String serviceJson = s.hasNext() ? s.next() : "";
			mockVcapServicesFromString(serviceJson);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}

	}

}
