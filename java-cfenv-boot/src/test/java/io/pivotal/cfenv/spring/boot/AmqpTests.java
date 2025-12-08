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

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import io.pivotal.cfenv.core.CfEnv;
import io.pivotal.cfenv.core.CfService;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author David Turanski
 *
 */
public class AmqpTests extends AmqpTestSupport {
	protected static final String hostname2 = "11.21.31.41";

	@Test
	public void rabbitServiceCreationWithTags() {
		mockVcapServices(getServicesPayload(
				getRabbitServicePayloadWithTags("rabbit-1", hostname, port, username, password, "q-1", "vhost1"),
				getRabbitServicePayloadWithTags("rabbit-2", hostname, port, username, password, "q-2", "vhost2")));

		CfEnv cfEnv = new CfEnv();
		assertThat(cfEnv.findServicesByName("rabbit-1","rabbit-2")).hasSize(2);
	}

	@Test
	public void rabbitServiceCreationWithManagementUri() {
			mockVcapServices(getServicesPayload(
				getRabbitServicePayloadWithTags("rabbit-1", hostname, port, username, password, "q-1", "vhost1")));

		String expectedManagementUri = "https://" + username + ":" + password + "@" + hostname + "/api";
		Map<String, String> credentials = (Map)new CfEnv().findServiceByName("rabbit-1").getMap().get("credentials");
		assertThat(credentials.get("http_api_uri")).isEqualTo(expectedManagementUri);
	}

	@Test
	public void rabbitServiceCreationWithoutManagementUri() {
		mockVcapServices(getServicesPayload(
				getRabbitServicePayloadNoLabelNoTags("rabbit-1", hostname, port, username, password, "q-1", "vhost1")));
		Map<String, ?> credentials = (Map)new CfEnv().findServiceByName("rabbit-1").getMap().get("credentials");
		assertThat(credentials.get("http_api_uri")).isNull();
	}

	@Test
	public void rabbitServiceCreationWithoutTags() {
		mockVcapServices(getServicesPayload(
				getRabbitServicePayloadWithoutTags("rabbit-1", hostname, port, username, password, "q-1", "vhost1"),
				getRabbitServicePayloadWithoutTags("rabbit-2", hostname, port, username, password, "q-2", "vhost2")));
		CfEnv cfEnv = new CfEnv();
		assertThat(cfEnv.findServicesByName("rabbit-1","rabbit-2")).hasSize(2);
	}

	@Test
	public void rabbitServiceCreationMultipleUris() {
		mockVcapServices(getServicesPayload(
				getRabbitServicePayloadMultipleUris("rabbit-1", hostname, hostname2, port, username, password, "q-1",
					"v/host1"),
				getRabbitServicePayloadMultipleUris("rabbit-2", hostname, hostname2, port, username, password, "q-2",
					"v/host2")));

		CfEnv cfEnv = new CfEnv();
		assertThat(cfEnv.findServicesByName("rabbit-1","rabbit-2")).hasSize(2);


		assertThat(cfEnv.findServiceByName("rabbit-1").getCredentials().getUriInfo().getUriString())
			.isEqualTo(String.format("amqp://%s:%s@%s/v/host1",username,password,hostname));

		String expectedManagementUri = "https://" + username + ":" + password + "@" + hostname + "/api";
		Map<String, ?> credentials = (Map)cfEnv.findServiceByName("rabbit-1").getMap().get("credentials");
		assertThat(credentials.get("http_api_uri")).isEqualTo(expectedManagementUri);

		List<String> uris = (List<String>) credentials.get("uris");
		assertThat(uris).hasSize(2);
		assertThat(uris.get(0)).contains(hostname);
		assertThat(uris.get(1)).contains(hostname2);

		List<String> managementUris = (List<String>) credentials.get("http_api_uris");
		assertThat(managementUris).hasSize(2);
		assertThat(managementUris.get(0)).contains(hostname);
		assertThat(managementUris.get(1)).contains(hostname2);
	}

	@Test
	public void rabbitServiceCreationNoLabelNoTags() {
		mockVcapServices(getServicesPayload(
				getRabbitServicePayloadNoLabelNoTags("rabbit-1", hostname, port, username, password, "q-1", "vhost1"),
				getRabbitServicePayloadNoLabelNoTags("rabbit-2", hostname, port, username, password, "q-2", "vhost2")));
		CfEnv cfEnv = new CfEnv();
		assertThat(cfEnv.findServicesByName("rabbit-1","rabbit-2")).hasSize(2);
	}

	@Test
	public void rabbitServiceCreationNoLabelNoTagsSecure() {
		mockVcapServices(getServicesPayload(
				getRabbitServicePayloadNoLabelNoTagsSecure("rabbit-1", hostname, port, username, password, "q-1", "vhost1"),
				getRabbitServicePayloadNoLabelNoTagsSecure("rabbit-2", hostname, port, username, password, "q-2", "vhost2")));
		CfEnv cfEnv = new CfEnv();
		assertThat(cfEnv.findServicesByName("rabbit-1","rabbit-2")).hasSize(2);
	}

	@Test
	public void qpidServiceCreationNoLabelNoTags() throws Exception {
		mockVcapServices(getServicesPayload(
				getQpidServicePayloadNoLabelNoTags("qpid-1", hostname, port, username, password, "q-1", "vhost1"),
				getQpidServicePayloadNoLabelNoTags("qpid-2", hostname, port, username, password, "q-2", "vhost2")));
		CfEnv cfEnv = new CfEnv();
		assertThat(cfEnv.findServicesByName("qpid-1","qpid-2")).hasSize(2);
		CfService cfService = cfEnv.findServiceByName("qpid-1");
		assertThat(cfService.getCredentials().getUsername()).isEqualTo(username);
		assertThat(cfService.getCredentials().getPassword()).isEqualTo(password);
		assertThat(cfService.getCredentials().getUriInfo().getUri().getQuery())
			.contains(String.format("tcp://%s:%s", hostname, port));
	}

	private String getQpidServicePayloadNoLabelNoTags(String serviceName,
		String hostname, int port,
		String user, String password, String name,
		String vHost) {
		return getAmqpServicePayload("test-qpid-info.json", serviceName,
			hostname, port, user, password, name, vHost);
	}
}
