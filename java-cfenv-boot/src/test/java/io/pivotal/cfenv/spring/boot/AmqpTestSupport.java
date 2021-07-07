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

import io.pivotal.cfenv.test.AbstractCfEnvTests;

/**
 * @author David Turanski
 **/
public class AmqpTestSupport extends AbstractCfEnvTests {
	protected String getRabbitServicePayloadWithoutTags(String serviceName,
		String hostname, int port,
		String user, String password, String name,
		String vHost) {
		return getAmqpServicePayload("test-rabbit-info-with-label-no-tags.json", serviceName,
			hostname, port, user, password, name, vHost);
	}

	protected String getRabbitServicePayloadNoLabelNoTagsSecure(String serviceName,
		String hostname, int port,
		String user, String password, String name,
		String vHost) {
		return getAmqpServicePayload("test-rabbit-info-no-label-no-tags-secure.json", serviceName,
			hostname, port, user, password, name, vHost);
	}

	protected String getRabbitServicePayloadNoLabelNoTags(String serviceName,
		String hostname, int port,
		String user, String password, String name,
		String vHost) {
		return getAmqpServicePayload("test-rabbit-info-no-label-no-tags.json", serviceName,
			hostname, port, user, password, name, vHost);
	}


	protected String getRabbitServicePayloadWithTags(String serviceName,
		String hostname, int port,
		String user, String password, String name,
		String vHost) {
		return getAmqpServicePayload("test-rabbit-info.json", serviceName,
			hostname, port, user, password, name, vHost);
	}

	protected String getRabbitServicePayloadMultipleUris(String serviceName,
		String hostname, String hostname2, int port,
		String user, String password, String name,
		String vHost) {
		String payload = getAmqpServicePayload("test-rabbit-info-multiple-uris.json", serviceName,
			hostname, port, user, password, name, vHost);
		payload = payload.replace("$host1", hostname);
		payload = payload.replace("$host2", hostname2);
		return payload;
	}

	protected String getAmqpServicePayload(String filename, String serviceName,
		String hostname, int port,
		String user, String password, String name,
		String vHost) {
		String payload = readTestDataFile(filename);
		payload = payload.replace("$serviceName", serviceName);
		payload = payload.replace("$hostname", hostname);
		payload = payload.replace("$port", Integer.toString(port));
		payload = payload.replace("$user", user);
		payload = payload.replace("$pass", password);
		payload = payload.replace("$name", name);
		payload = payload.replace("$virtualHost", vHost);

		return payload;
	}
}
