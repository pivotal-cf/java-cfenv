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
package io.pivotal.cfenv.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import org.springframework.core.io.ClassPathResource;

import io.pivotal.cfenv.core.test.CfEnvMock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Mark Pollack
 * @author Paul Warren
 * @author David Turanski
 */
public abstract class CfEnvTests {

	public static final String DEFAULT_VCAP_SERVICES = "vcap-services.json";
	public static final String DEFAULT_VCAP_APPLICATION = "test/vcap-application.json";
	protected CfEnv cfEnv;

	@Before
	public void beforeEach() {
		setupTest(DEFAULT_VCAP_SERVICES, DEFAULT_VCAP_APPLICATION);
	}

	protected abstract void setupTest(String vcapServicesResource, String vcapApplicationResource);

	public static class CfEnvWithParametersTests extends CfEnvTests {
		@Override
		protected void setupTest(String vcapServicesResource, String vcapApplicationResource) {
			String vcapServicesJson = readResource(vcapServicesResource);
			String vcapApplicationJson = readResource(vcapApplicationResource);
			cfEnv = new CfEnv(vcapApplicationJson, vcapServicesJson);
		}
	}

	public static class CfEnvDefaultConstructorTests extends CfEnvTests {
		@Override
		protected void setupTest(String vcapServicesResource, String vcapApplicationResource) {
			CfEnvMock.configure()
					.vcapServicesResource(vcapServicesResource)
					.vcapApplicationResource(vcapApplicationResource)
					.mock();
			cfEnv = new CfEnv();
		}
	}

	@Test
	public void testCfApplicationValues() {
		CfApplication cfApplication = cfEnv.getApp();
		assertThat(cfApplication.getApplicationId())
				.isEqualTo("fa05c1a9-0fc1-4fbd-bae1-139850dec7a3");
		assertThat(cfApplication.getInstanceId())
				.isEqualTo("fe98dc76ba549876543210abcd1234");
		assertThat(cfApplication.getInstanceIndex()).isEqualTo(0);
		assertThat(cfApplication.getHost()).isEqualTo("0.0.0.0");
		assertThat(cfApplication.getPort()).isEqualTo(61857);
		assertThat(cfApplication.getApplicationVersion())
				.isEqualTo("ab12cd34-5678-abcd-0123-abcdef987654");
		assertThat(cfApplication.getApplicationName()).isEqualTo("styx-james");
		assertThat(cfApplication.getApplicationUris()).contains("my-app.example.com");
		assertThat(cfApplication.getVersion())
				.isEqualTo("ab12cd34-5678-abcd-0123-abcdef987654");
		assertThat(cfApplication.getName()).isEqualTo("my-app");
		assertThat(cfApplication.getUris()).contains("my-app.example.com");
		assertThat(cfApplication.getCfApi()).isEqualTo("https://api.example.com");
		assertThat(cfApplication.getSpaceId()).isEqualTo("06450c72-4669-4dc6-8096-45f9777db68a");
		assertThat(cfApplication.getSpaceName()).isEqualTo("my-space");

	}

	@Test
	public void testCfService() {
		List<CfService> cfServices = cfEnv.findAllServices();
		assertThat(cfServices.size()).isEqualTo(3);

		CfService cfService = cfEnv.findServiceByTag("mysql");
		assertThat(cfService.getString("blah")).isNull();

		assertThat(cfService.getTags()).containsExactly("mysql", "relational");
		assertThat(cfService.getMap()).containsEntry("syslog_drain_url", null)
				.containsEntry("volume_mounts", new ArrayList<String>())
				.containsEntry("label", "p-mysql").containsEntry("provider", null)
				.containsEntry("plan", "100mb").containsEntry("name", "mysql")
				.containsKey("credentials");

		CfCredentials cfCredentials = cfService.getCredentials();
		assertMySqlCredentials(cfCredentials);

		// Query methods

		assertThat(cfService.existsByTagIgnoreCase()).isFalse();
		assertThat(cfService.existsByTagIgnoreCase((String[]) null)).isFalse();
		assertThat(cfService.existsByTagIgnoreCase("relational")).isTrue();
		assertThat(cfService.existsByTagIgnoreCase("ReLaTiOnAl")).isTrue();
		assertThat(cfService.existsByTagIgnoreCase("blah")).isFalse();
		assertThat(cfService.existsByTagIgnoreCase("blah", "relational")).isTrue();

		assertThat(cfService.existsByUriSchemeStartsWith()).isFalse();
		assertThat(cfService.existsByUriSchemeStartsWith((String[]) null)).isFalse();
		assertThat(cfService.existsByUriSchemeStartsWith("mysql")).isTrue();
		assertThat(cfService.existsByUriSchemeStartsWith("MYSQL")).isFalse();
		assertThat(cfService.existsByUriSchemeStartsWith("blah")).isFalse();
		assertThat(cfService.existsByUriSchemeStartsWith("blah", "mysql")).isTrue();

		assertThat(cfService.existsByCredentialsContainsUriField()).isFalse();
		assertThat(cfService.existsByCredentialsContainsUriField((String[]) null))
				.isFalse();
		assertThat(cfService.existsByCredentialsContainsUriField("mysql")).isFalse();
		// TODO sample data does not support testing for .isTrue

		// Test Redis Entries

		cfService = cfEnv.findServiceByTag("redis");
		assertThat(cfService.getTags()).containsExactly("pivotal", "redis");
		cfCredentials = cfService.getCredentials();
		Map<String, Object> credentialMap = cfCredentials.getMap();
		credentialMap = cfCredentials.getMap();
		assertThat(credentialMap).containsEntry("host", "10.0.4.30");
		assertThat(cfCredentials.getHost()).isEqualTo("10.0.4.30");
		assertThat(cfService.getName()).isEqualTo("redis-binding");

		// Test Volume Services

		cfService = cfEnv.findServiceByName("nfs1");
		assertThat(cfService.getTags()).containsExactly("nfs");
		List<CfVolume> cfVolumes = cfService.getVolumes();
		assertNfsVolumes(cfVolumes);
	}

	private void assertMySqlCredentials(CfCredentials cfCredentials) {
		Map<String, Object> credentialMap = cfCredentials.getMap();
		assertThat(credentialMap).containsEntry("hostname", "10.0.4.35")
				.containsEntry("port", 3306).containsEntry("name", "mysql_name")
				.containsEntry("username", "mysql_username")
				.containsEntry("password", "mysql_password")
				.containsEntry("uri",
						"mysql://mysql_username:mysql_password@10.0.4.35:3306/cf_2e23d10a_8738_8c3c_66cf_13e44422698c?reconnect=true")
				.containsEntry("jdbcUrl",
						"jdbc:mysql://10.0.4.35:3306/cf_2e23d10a_8738_8c3c_66cf_13e44422698c?user=mysql_username&password=mysql_password");

		assertThat(cfCredentials.getUsername()).isEqualTo("mysql_username");
		assertThat(cfCredentials.getPassword()).isEqualTo("mysql_password");
		assertThat(cfCredentials.getHost()).isEqualTo("10.0.4.35");
		assertThat(cfCredentials.getPort()).isEqualTo("3306");
		assertThat(cfCredentials.getUri()).isEqualTo(
				"mysql://mysql_username:mysql_password@10.0.4.35:3306/cf_2e23d10a_8738_8c3c_66cf_13e44422698c?reconnect=true");

		UriInfo uriInfo = cfCredentials.getUriInfo("mysql");
		assertUriInfo(uriInfo);
		uriInfo = cfCredentials.getUriInfo();
		assertUriInfo(uriInfo);

	}

	private void assertUriInfo(UriInfo uriInfo) {
		assertThat(uriInfo.getUsername()).isEqualTo("mysql_username");
		assertThat(uriInfo.getPassword()).isEqualTo("mysql_password");
		assertThat(uriInfo.getHost()).isEqualTo("10.0.4.35");
		assertThat(uriInfo.getPort()).isEqualTo(3306);
	}

	@Test
	public void testFindServiceByName() {
		CfService cfService = cfEnv.findServiceByTag("redis");
		assertThat(cfService.getLabel()).isEqualTo("p-redis");
		assertThat(cfService.getPlan()).isEqualTo("shared-vm");
		assertThat(cfService.getName()).isEqualTo("redis-binding");

		cfService = cfEnv.findServiceByTag("blah", "redis");
		assertThat(cfService.getLabel()).isEqualTo("p-redis");

		cfService = cfEnv.findServiceByName(".*sql");
		assertThat(cfService.getName()).isEqualTo("mysql");

		assertThatThrownBy(() -> {
			cfEnv.findServiceByName("blah");
		}).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("No service with name [blah]");

		assertThatThrownBy(() -> {
			cfEnv.findServiceByName("");
		}).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("No service with name []");

		assertThatThrownBy(() -> {
			cfEnv.findServiceByName((String[]) null);
		}).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("No service with name [null]");

	}

	@Test
	public void testFindServiceByLabel() {
		CfService cfService = cfEnv.findServiceByLabel("p-redis");
		assertThat(cfService.getLabel()).isEqualTo("p-redis");
		assertThat(cfService.getPlan()).isEqualTo("shared-vm");
		assertThat(cfService.getName()).isEqualTo("redis-binding");

		cfService = cfEnv.findServiceByLabel("blah", "p-redis");
		assertThat(cfService.getLabel()).isEqualTo("p-redis");
		assertThat(cfService.getName()).isEqualTo("redis-binding");

		cfService = cfEnv.findServiceByLabel(".*redis");
		assertThat(cfService.getLabel()).isEqualTo("p-redis");
		assertThat(cfService.getName()).isEqualTo("redis-binding");

		assertThatThrownBy(() -> {
			cfEnv.findServiceByLabel("blah");
		}).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("No service with label [blah]");

		assertThatThrownBy(() -> {
			cfEnv.findServiceByLabel("");
		}).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("No service with label []");

		assertThatThrownBy(() -> {
			cfEnv.findServiceByLabel((String[]) null);
		}).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("No service with label [null]");

	}

	@Test
	public void testFindServiceByTag() {
		CfService cfService = cfEnv.findServiceByTag("redis");
		assertThat(cfService.getLabel()).isEqualTo("p-redis");
		assertThat(cfService.getPlan()).isEqualTo("shared-vm");
		assertThat(cfService.getName()).isEqualTo("redis-binding");

		cfService = cfEnv.findServiceByTag("blah", "redis");
		assertThat(cfService.getLabel()).isEqualTo("p-redis");

		cfService = cfEnv.findServiceByTag(".*sql");
		assertThat(cfService.getName()).isEqualTo("mysql");

		assertThatThrownBy(() -> {
			cfEnv.findServiceByTag("blah");
		}).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("No service with tag [blah]");

		assertThatThrownBy(() -> {
			cfEnv.findServiceByTag("");
		}).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("No service with tag []");

		assertThatThrownBy(() -> {
			cfEnv.findServiceByTag((String[]) null);
		}).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("No service with tag [null]");
	}

	@Test
	public void testFindCredentialsByName() {
		CfCredentials cfCredentials = cfEnv.findCredentialsByName("mysql");
		assertMySqlCredentials(cfCredentials);

		cfCredentials = cfEnv.findCredentialsByName("blah", "mysql");
		assertMySqlCredentials(cfCredentials);

		cfCredentials = cfEnv.findCredentialsByName(".*sql");
		assertMySqlCredentials(cfCredentials);

		assertThatThrownBy(() -> {
			cfEnv.findCredentialsByName("blah");
		}).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("No service with name [blah]");

		assertThatThrownBy(() -> {
			cfEnv.findCredentialsByName("");
		}).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("No service with name []");

		assertThatThrownBy(() -> {
			cfEnv.findCredentialsByName((String[]) null);
		}).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("No service with name [null]");

	}

	@Test
	public void testFindCredentialsByLabel() {
		CfCredentials cfCredentials = cfEnv.findCredentialsByLabel("p-mysql");
		assertMySqlCredentials(cfCredentials);

		cfCredentials = cfEnv.findCredentialsByLabel("blah", "p-mysql");
		assertMySqlCredentials(cfCredentials);

		cfCredentials = cfEnv.findCredentialsByLabel(".*mysql");
		assertMySqlCredentials(cfCredentials);

		assertThatThrownBy(() -> {
			cfEnv.findCredentialsByLabel("blah");
		}).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("No service with label [blah]");

		assertThatThrownBy(() -> {
			cfEnv.findCredentialsByLabel("");
		}).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("No service with label []");

		assertThatThrownBy(() -> {
			cfEnv.findCredentialsByLabel((String[]) null);
		}).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("No service with label [null]");
	}

	@Test
	public void testFindCredentialsByTag() {
		CfCredentials cfCredentials = cfEnv.findCredentialsByTag("mysql");
		assertMySqlCredentials(cfCredentials);

		cfCredentials = cfEnv.findCredentialsByTag("blah", "mysql");
		assertMySqlCredentials(cfCredentials);

		cfCredentials = cfEnv.findCredentialsByTag(".*sql");
		assertMySqlCredentials(cfCredentials);

		assertThatThrownBy(() -> {
			cfEnv.findCredentialsByTag("blah");
		}).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("No service with tag [blah]");

		assertThatThrownBy(() -> {
			cfEnv.findCredentialsByTag("");
		}).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("No service with tag []");

		assertThatThrownBy(() -> {
			cfEnv.findCredentialsByTag((String[]) null);
		}).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("No service with tag [null]");

	}

	@Test
	public void testNullCredentials() {
		setupTest("vcap-null-credentials.json", DEFAULT_VCAP_APPLICATION);

		CfService cfService = cfEnv.findServiceByTag("efs");
		// should not throw exception
		cfService.existsByCredentialsContainsUriField("foo");
	}

	private void assertNfsVolumes(List<CfVolume> cfVolumes) {
		assertThat(cfVolumes.size()).isEqualTo(1);

		String expectedVolumePath = "/var/vcap/data/78525ee7-196c-4ed4-8ac6-857d15334631";

		Map<String, String> cfVolumeMap = cfVolumes.get(0).getMap();
		assertThat(cfVolumeMap)
				.containsEntry("container_dir", expectedVolumePath)
				.containsEntry("device_type", "shared")
				.containsEntry("mode", "rw");

		CfVolume cfVolume = cfVolumes.get(0);

		String normalizedVolumePath = expectedVolumePath.replace('/', File.separatorChar);

		assertThat(cfVolume.getPath().toString()).isEqualTo(normalizedVolumePath);
		assertThat(cfVolume.getMode()).isEqualTo(CfVolume.Mode.READ_WRITE);
	}

	@Test
	public void testMultipleMatchingServices() {
		setupTest("vcap-services-multiple-mysql.json", DEFAULT_VCAP_APPLICATION);

		List<CfService> services = cfEnv.findAllServices();
		assertThat(services.size()).isEqualTo(3);

		assertThatThrownBy(() -> {
			CfService service = cfEnv.findServiceByName("mysql.*");
		}).isInstanceOf(IllegalArgumentException.class).hasMessageContaining(
				"No unique service matching by name [mysql.*] was found.  Matching service names are [mysql, mysql2]");

		assertThatThrownBy(() -> {
			CfService service = cfEnv.findServiceByLabel("p-mysql");
		}).isInstanceOf(IllegalArgumentException.class).hasMessageContaining(
				"No unique service matching by label [p-mysql] was found.  Matching service names are [mysql, mysql2]");

		assertThatThrownBy(() -> {
			CfService service = cfEnv.findServiceByTag("mysql");
		}).isInstanceOf(IllegalArgumentException.class).hasMessageContaining(
				"No unique service matching by tag [mysql] was found.  Matching service names are [mysql, mysql2]");

		List<CfService> servicesByName = cfEnv.findServicesByName("mysql.*");
		assertThat(servicesByName.size()).isEqualTo(2);

		List<CfService> servicesByLabel = cfEnv.findServicesByLabel("p-mysql");
		assertThat(servicesByLabel.size()).isEqualTo(2);

		List<CfService> servicesByTag = cfEnv.findServicesByTag("relational");
		assertThat(servicesByTag.size()).isEqualTo(2);

	}

	protected String readResource(String resource) {
		ClassPathResource classPathResource = new ClassPathResource(resource);
		try (FileSystem ignored = FileSystems.newFileSystem(
				classPathResource.getURI(),
				Collections.emptyMap(),
				ClassLoader.getSystemClassLoader())) {
			byte[] resourceBytes = Files.readAllBytes(Paths.get(classPathResource.getURI()));
			return new String(resourceBytes);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
