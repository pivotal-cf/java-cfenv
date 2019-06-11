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

package io.pivotal.cfenv.spring.boot;

import org.springframework.util.ClassUtils;

/**
 * Determine if the Connector Library is on the classpath
 * @author Mark Pollack
 */
public abstract class ConnectorLibraryDetector {

	public static String MESSAGE = "Exiting the application since the Spring Cloud Connector library has been detected on the classpath.  Please remove this dependency from your project and set the environment variable JBP_CONFIG_SPRING_AUTO_RECONFIGURATION '{enabled: false}' in the Cloud Foundry manifest.";

	private static boolean usingConnectorLibrary;

	static {
		ClassLoader classLoader = ConnectorLibraryDetector.class.getClassLoader();
		usingConnectorLibrary = ClassUtils.isPresent("org.springframework.cloud.Cloud", classLoader);
	}

	static boolean isUsingConnectorLibrary() {
		return usingConnectorLibrary;
	}

	static void assertNoConnectorLibrary() {
		if (ConnectorLibraryDetector.isUsingConnectorLibrary()) {
			throw new IllegalStateException(ConnectorLibraryDetector.MESSAGE);
		}
	}
}
