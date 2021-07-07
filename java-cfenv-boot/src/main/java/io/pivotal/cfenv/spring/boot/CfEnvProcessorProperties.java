/*
 * Copyright 2019 the original author or authors.
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

/**
 * Properties describing a CfEnvProcessor, mainly used for better logging messages in {@link CfEnvProcessor}.
 *
 * @author Mark Pollack
 */
public class CfEnvProcessorProperties {

	private String propertyPrefixes;

	private String serviceName;

	private CfEnvProcessorProperties() {
	}

	public static Builder builder() {
		return new CfEnvProcessorProperties.Builder();
	}

	/**
	 * A string containing the values of property prefixes that will be set in the {@code process} method.  Used
	 * for logging purposes.
	 * @return property prefix values set in the {@code process} method
	 */
	public String getPropertyPrefixes() {
		return propertyPrefixes;
	}

	/**
	 * A name that describes the service being processed, eg. 'Redis', 'MongoDB'.  Used for logging purposes.
	 * @return name that describes the service being processed
	 */
	public String getServiceName() {
		return serviceName;
	}


	public static class Builder {
		private CfEnvProcessorProperties processorProperties = new CfEnvProcessorProperties();

		public Builder propertyPrefixes(String propertyPrefixes) {
			this.processorProperties.propertyPrefixes = propertyPrefixes;
			return this;
		}

		public Builder serviceName(String serviceName) {
			this.processorProperties.serviceName = serviceName;
			return this;
		}

		public CfEnvProcessorProperties build() {
			return processorProperties;
		}
	}
}
