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
package io.pivotal.cfenv.jdbc;

import java.util.List;

import org.junit.Test;

import io.pivotal.cfenv.core.UriInfo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Mark Pollack
 * @author David Turanski
 */
public class MySqlJdbcTests extends AbstractJdbcTests {

	@Test
	public void mysqlServiceCreation() {
		String name1 = "database-1";
		String name2 = "database-2";

		mockVcapServices(getServicesPayload(
				getMysqlServicePayload("mysql-1", hostname, port, username, password,
						name1),
				getMysqlServicePayload("mysql-2", hostname, port, username, password,
						name2)));

		assertJdbcServiceValues(name1, name2);

		CfJdbcEnv cfJdbcEnv = new CfJdbcEnv();
		CfJdbcService cfJdbcService = cfJdbcEnv.findJdbcServiceByName("mysql-1");
		assertThat(cfJdbcService.getUsername()).isEqualTo(username);
		assertThat(cfJdbcService.getPassword()).isEqualTo(password);
		assertThat(cfJdbcService.getDriverClassName())
				.isEqualTo(MySqlJdbcUrlCreator.MARIADB_DRIVER_CLASS_NAME);

		assertThatThrownBy(() -> {
			cfJdbcEnv.findJdbcServiceByName("mysql.*");
		}).isInstanceOf(IllegalArgumentException.class).hasMessage(
				"No unique database service matching by name [mysql.*] was found.  Matching service names are [mysql-1, mysql-2]");
	}

	@Test
	public void mysqlServiceCreationWithLabelNoTags() {
		String name1 = "database-1";
		String name2 = "database-2";
		mockVcapServices(getServicesPayload(
				getMysqlServicePayloadWithLabelNoTags("mysql-1", hostname, port, username,
						password, name1),
				getMysqlServicePayloadWithLabelNoTags("mysql-2", hostname, port, username,
						password, name2)));

		assertJdbcServiceValues(name1, name2);

	}

	@Test
	public void mysqlServiceCreationNoLabelNoTags() {
		String name1 = "database-1";
		String name2 = "database-2";
		mockVcapServices(getServicesPayload(
				getMysqlServicePayloadNoLabelNoTags("mysql-1", hostname, port, username,
						password, name1),
				getMysqlServicePayloadNoLabelNoTags("mysql-2", hostname, port, username,
						password, name2)));

		assertJdbcServiceValues(name1, name2);

	}

	@Test
	public void mysqlServiceCreationNoLabelNoTagsWithSpecialChars() {
		String name = "database";
		String userWithSpecialChars = "u%u:u+";
		String passwordWithSpecialChars = "p%p:p+";

		mockVcapServices(getServicesPayload(getMysqlServicePayloadNoLabelNoTags("mysql",
				hostname, port, userWithSpecialChars, passwordWithSpecialChars, name)));

		CfJdbcEnv cfJdbcEnv = new CfJdbcEnv();
		CfJdbcService cfJdbcService = cfJdbcEnv.findJdbcServiceByName("mysql");
		String jdbcUrlMysql = cfJdbcService.getJdbcUrl();

		assertThat(getExpectedMysqlJdbcUrl(hostname, port, name, userWithSpecialChars,
				passwordWithSpecialChars)).isEqualTo(jdbcUrlMysql);
	}

	@Test
	public void mysqlServiceCreationWithLabelNoUri() {
		String name1 = "database-1";
		String name2 = "database-2";
		mockVcapServices(getServicesPayload(
				getMysqlServicePayloadWithLabelNoUri("mysql-1", hostname, port, username,
						password, name1),
				getMysqlServicePayloadWithLabelNoUri("mysql-2", hostname, port, username,
						password, name2)));

		assertJdbcServiceValues(name1, name2);
	}

	@Test
	public void mysqlServiceCreationWithJdbcUrl() {
		String name1 = "database-1";
		String name2 = "database-2";
		mockVcapServices(getServicesPayload(
				getMysqlServicePayloadWithJdbcUrl("mysql-1", hostname, port, username,
						password, name1),
				getMysqlServicePayloadWithJdbcUrl("mysql-2", hostname, port, username,
						password, name2)));

		assertJdbcServiceValues(name1, name2);
	}

	@Test
	public void mysqlServiceCreationWithJdbcUrlAndSpecialChars() {
		String userWithSpecialChars = "u%u:u+";
		String passwordWithSpecialChars = "p%p:p+";
		String name = "database";
		mockVcapServices(getServicesPayload(getMysqlServicePayloadWithJdbcUrl("mysql",
				hostname, port, userWithSpecialChars, passwordWithSpecialChars, name)));

		CfJdbcEnv cfJdbcEnv = new CfJdbcEnv();
		CfJdbcService cfJdbcService = cfJdbcEnv.findJdbcServiceByName("mysql");
		String jdbcUrlMysql = cfJdbcService.getJdbcUrl();

		assertThat(getExpectedMysqlJdbcUrl(hostname, port, name, userWithSpecialChars,
				passwordWithSpecialChars)).isEqualTo(jdbcUrlMysql);
	}

	@Test
	public void mysqlServiceCreationWithJdbcUrlOnly() {
		String name1 = "database-1";
		String name2 = "database-2";
		mockVcapServices(getServicesPayload(
				getMysqlServicePayloadWithJdbcUrlOnly("mysql-1", hostname, port, username,
						password, name1),
				getMysqlServicePayloadWithJdbcUrlOnly("mysql-2", hostname, port, username,
						password, name2)));

		assertJdbcServiceValues(name1, name2);

		CfJdbcEnv cfJdbcEnv = new CfJdbcEnv();
		CfJdbcService cfJdbcService = cfJdbcEnv.findJdbcServiceByName("mysql-1");
		assertThat(cfJdbcService.getUsername()).isNull();
		assertThat(cfJdbcService.getPassword()).isNull();
		assertThat(cfJdbcService.getDriverClassName())
				.isEqualTo(MySqlJdbcUrlCreator.MARIADB_DRIVER_CLASS_NAME);
	}

	@Test
	public void mysqlServiceCreationWithJdbcUrlOnlyWithSpecialChars() {
		String name = "database";
		String userWithSpecialChars = "u%u:u+";
		String passwordWithSpecialChars = "p%p:p+";
		mockVcapServices(getServicesPayload(getMysqlServicePayloadWithJdbcUrlOnly("mysql",
				hostname, port, userWithSpecialChars, passwordWithSpecialChars, name)));

		CfJdbcEnv cfJdbcEnv = new CfJdbcEnv();
		CfJdbcService cfJdbcService = cfJdbcEnv.findJdbcServiceByName("mysql");
		String jdbcUrlMysql = cfJdbcService.getJdbcUrl();
		assertThat(getExpectedMysqlJdbcUrl(hostname, port, name, userWithSpecialChars,
				passwordWithSpecialChars)).isEqualTo(jdbcUrlMysql);
	}


	@Test
	public void mariaDbServiceCreation() {
		String name1 = "database-1";
		String name2 = "database-2";

		mockVcapServices(getServicesPayload(
				getMariaDbServicePayload("mariadb-1", hostname, port, username, password,
						name1),
				getMariaDbServicePayload("mariadb-2", hostname, port, username, password,
						name2)));

		assertJdbcServiceValuesMariaDb(name1, name2);

		CfJdbcEnv cfJdbcEnv = new CfJdbcEnv();
		CfJdbcService cfJdbcService = cfJdbcEnv.findJdbcServiceByName("mariadb-1");
		assertThat(cfJdbcService.getUsername()).isEqualTo(username);
		assertThat(cfJdbcService.getPassword()).isEqualTo(password);
		assertThat(cfJdbcService.getDriverClassName())
				.isEqualTo(MySqlJdbcUrlCreator.MARIADB_DRIVER_CLASS_NAME);
	}
	@Test
	public void mariaDbServiceCreationWithJdbcUrl() {
		String name1 = "database-1";
		String name2 = "database-2";
		mockVcapServices(getServicesPayload(
				getMariaDbServicePayloadWithJdbcUrl("mariadb-1", hostname, port, username,
						password, name1),
				getMariaDbServicePayloadWithJdbcUrl("mariadb-2", hostname, port, username,
						password, name2)));

		assertJdbcServiceValuesMariaDb(name1, name2);
	}

	@Test
	public void mariaDbServiceCreationWithLabelNoTags() {
		String name1 = "database-1";
		String name2 = "database-2";
		mockVcapServices(getServicesPayload(
				getMariaDbServicePayloadWithLabelNoTags("mariadb-1", hostname, port, username,
						password, name1),
				getMariaDbServicePayloadWithLabelNoTags("mariadb-2", hostname, port, username,
						password, name2)));
		assertJdbcServiceValuesMariaDb(name1, name2);

	}

	@Test
	public void mariaDbServiceCreationWithUriOnly() {
		String name1 = "database-1";
		String name2 = "database-2";
		mockVcapServices(getServicesPayload(
				getMariaDbServicePayloadUri("mariadb-1", hostname, port, username,
						password, name1),
				getMariaDbServicePayloadUri("mariadb-2", hostname, port, username,
						password, name2)));

		assertJdbcServiceValuesMariaDb(name1, name2);

		CfJdbcEnv cfJdbcEnv = new CfJdbcEnv();
		CfJdbcService cfJdbcService = cfJdbcEnv.findJdbcServiceByName("mariadb-1");
		assertThat(cfJdbcService.getDriverClassName())
				.isEqualTo(MySqlJdbcUrlCreator.MARIADB_DRIVER_CLASS_NAME);
	}

	@Test
	public void mariaDbServiceCreationWithJdbcUrlOnly() {
		String name1 = "database-1";
		String name2 = "database-2";
		mockVcapServices(getServicesPayload(
				getMariaDbServicePayloadWithJdbcUrlOnly("mariadb-1", hostname, port, username,
						password, name1),
				getMariaDbServicePayloadWithJdbcUrlOnly("mariadb-2", hostname, port, username,
						password, name2)));

		assertJdbcServiceValuesMariaDb(name1, name2);

		CfJdbcEnv cfJdbcEnv = new CfJdbcEnv();
		CfJdbcService cfJdbcService = cfJdbcEnv.findJdbcServiceByName("mariadb-1");
		assertThat(cfJdbcService.getUsername()).isNull();
		assertThat(cfJdbcService.getPassword()).isNull();
		assertThat(cfJdbcService.getDriverClassName())
				.isEqualTo(MySqlJdbcUrlCreator.MARIADB_DRIVER_CLASS_NAME);
	}

	// Utility methods

	private void assertJdbcServiceValues(String name1, String name2) {
		CfJdbcEnv cfJdbcEnv = new CfJdbcEnv();
		String jdbcUrlMysql1 = cfJdbcEnv.findJdbcServiceByName("mysql-1").getJdbcUrl();
		String jdbcUrlMysql2 = cfJdbcEnv.findJdbcServiceByName("mysql-2").getJdbcUrl();

		assertThat(getExpectedJdbcUrl(MySqlJdbcUrlCreator.MYSQL_SCHEME, name1))
				.isEqualTo(jdbcUrlMysql1);
		assertThat(getExpectedJdbcUrl(MySqlJdbcUrlCreator.MYSQL_SCHEME, name2))
				.isEqualTo(jdbcUrlMysql2);

		CfJdbcService cfJdbcService1 = cfJdbcEnv.findJdbcServiceByName("mysql-1");
		CfJdbcService cfJdbcService2 = cfJdbcEnv.findJdbcServiceByName("mysql-2");
		jdbcUrlMysql1 = cfJdbcService1.getJdbcUrl();
		jdbcUrlMysql2 = cfJdbcService2.getJdbcUrl();
		assertThat(getExpectedJdbcUrl(MySqlJdbcUrlCreator.MYSQL_SCHEME, name1))
				.isEqualTo(jdbcUrlMysql1);
		assertThat(getExpectedJdbcUrl(MySqlJdbcUrlCreator.MYSQL_SCHEME, name2))
				.isEqualTo(jdbcUrlMysql2);

		List<CfJdbcService> cfJdbcServices = cfJdbcEnv.findJdbcServices();
		assertThat(cfJdbcServices.size()).isEqualTo(2);

		assertThatThrownBy(() -> {
			cfJdbcEnv.findJdbcService().getJdbcUrl();
		}).isInstanceOf(IllegalArgumentException.class).hasMessage(
				"No unique database service found. Found database service names [mysql-1, mysql-2]");

		CfJdbcService cfJdbcService = cfJdbcEnv.findJdbcServiceByName("mysql-1");
		assertThat(cfJdbcService.getDriverClassName())
				.isEqualTo(MySqlJdbcUrlCreator.MARIADB_DRIVER_CLASS_NAME);

	}

	private void assertJdbcServiceValuesMariaDb(String name1, String name2) {
		CfJdbcEnv cfJdbcEnv = new CfJdbcEnv();
		String jdbcUrlMysql1 = cfJdbcEnv.findJdbcServiceByName("mariadb-1").getJdbcUrl();
		String jdbcUrlMysql2 = cfJdbcEnv.findJdbcServiceByName("mariadb-2").getJdbcUrl();

		assertThat(getExpectedJdbcUrl(MySqlJdbcUrlCreator.MARIADB_SCHEME, name1))
				.isEqualTo(jdbcUrlMysql1);
		assertThat(getExpectedJdbcUrl(MySqlJdbcUrlCreator.MARIADB_SCHEME, name2))
				.isEqualTo(jdbcUrlMysql2);

		CfJdbcService cfJdbcService1 = cfJdbcEnv.findJdbcServiceByName("mariadb-1");
		CfJdbcService cfJdbcService2 = cfJdbcEnv.findJdbcServiceByName("mariadb-2");
		jdbcUrlMysql1 = cfJdbcService1.getJdbcUrl();
		jdbcUrlMysql2 = cfJdbcService2.getJdbcUrl();
		assertThat(getExpectedJdbcUrl(MySqlJdbcUrlCreator.MARIADB_SCHEME, name1))
				.isEqualTo(jdbcUrlMysql1);
		assertThat(getExpectedJdbcUrl(MySqlJdbcUrlCreator.MARIADB_SCHEME, name2))
				.isEqualTo(jdbcUrlMysql2);

		List<CfJdbcService> cfJdbcServices = cfJdbcEnv.findJdbcServices();
		assertThat(cfJdbcServices.size()).isEqualTo(2);

		assertThatThrownBy(() -> {
			cfJdbcEnv.findJdbcService().getJdbcUrl();
		}).isInstanceOf(IllegalArgumentException.class).hasMessage(
				"No unique database service found. Found database service names [mariadb-1, mariadb-2]");

		CfJdbcService cfJdbcService = cfJdbcEnv.findJdbcServiceByName("mariadb-1");
		assertThat(cfJdbcService.getDriverClassName())
				.isEqualTo(MySqlJdbcUrlCreator.MARIADB_DRIVER_CLASS_NAME);

	}

	private String getExpectedMysqlJdbcUrl(String hostname, int port, String name,
			String user, String password) {
		return String.format("jdbc:mysql://%s:%d/%s?user=%s&password=%s", hostname, port,
				name, UriInfo.urlEncode(user), UriInfo.urlEncode(password));
	}

	private String getMysqlServicePayload(String serviceName, String hostname, int port,
			String user, String password, String name) {
		return getTemplatedPayload("test-mysql-info.json", serviceName, hostname, port,
				user, password, name);
	}

	private String getMysqlServicePayloadWithLabelNoTags(String serviceName,
			String hostname, int port, String user, String password, String name) {
		return getTemplatedPayload("test-mysql-info-with-label-no-tags.json",
				serviceName, hostname, port, user, password, name);
	}

	private String getMysqlServicePayloadNoLabelNoTags(String serviceName,
			String hostname, int port, String user, String password, String name) {
		return getTemplatedPayload("test-mysql-info-no-label-no-tags.json", serviceName,
				hostname, port, user, password, name);
	}

	private String getMysqlServicePayloadWithLabelNoUri(String serviceName,
			String hostname, int port, String user, String password, String name) {
		return getTemplatedPayload("test-mysql-info-with-label-no-uri.json", serviceName,
				hostname, port, user, password, name);
	}

	private String getMysqlServicePayloadWithJdbcUrl(String serviceName, String hostname,
			int port, String user, String password, String name) {
		return getTemplatedPayload("test-mysql-info-jdbc-url.json", serviceName,
				hostname, port, user, password, name);
	}

	private String getMysqlServicePayloadWithJdbcUrlOnly(String serviceName,
			String hostname, int port, String user, String password, String name) {
		return getTemplatedPayload("test-mysql-info-jdbc-url-only.json", serviceName,
				hostname, port, user, password, name);
	}

	private String getMariaDbServicePayloadWithJdbcUrl(String serviceName, String hostname, int port, String user, String password, String name) {
		//Test by MariaDB JDBC url with DB uri
		return getTemplatedPayload("test-mariadb-info-jdbc-url.json", serviceName, hostname, port, user, password, name);
	}

	private String getMariaDbServicePayloadWithLabelNoTags(String serviceName, String hostname, int port, String user, String password, String name) {
		//Test by presence of mariadb string in beginning of service label
		return getTemplatedPayload("test-mariadb-info-with-label-no-tags.json", serviceName, hostname, port, user, password, name);
	}

	private String getMariaDbServicePayload(String serviceName, String hostname, int port, String user, String password, String name) {
		//Test by MariaDB tag (alongside mysql)
		return getTemplatedPayload("test-mariadb-info.json", serviceName, hostname, port, user, password, name);
	}

	private String getMariaDbServicePayloadUri(String serviceName, String hostname, int port, String user, String password, String name) {
		//Test by MariaDB uri
		return getTemplatedPayload("test-mariadb-info-uri.json", serviceName, hostname, port, user, password, name);
	}

	private String getMariaDbServicePayloadWithJdbcUrlOnly(String serviceName, String hostname, int port, String user, String password, String name) {
		//Test by MariaDB jdbc url
		return getTemplatedPayload("test-mariadb-info-jdbc-url-only.json", serviceName,
				hostname, port, user, password, name);
	}

	@Override
	protected String getExpectedJdbcUrl(String databaseType, String name) {
		return "jdbc:" + databaseType + "://" + hostname + ":" + port + "/" + name
				+ "?user=" + username + "&password=" + password;
	}

}
