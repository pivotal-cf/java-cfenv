package io.pivotal.cfenv.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;

class HanaJdbcTests extends AbstractJdbcTests {

	@Test
	public void hanaOnSapBtpServiceCreation() {
		String name1 = "database-1";
		String name2 = "database-2";

		mockVcapServices(
			getServicesPayload(getHanaServicePayload("hanadb-1", hostname, port, username, password, name1),
				getHanaServicePayload("hanadb-2", hostname, port, username, password, name2)));

		assertJdbcServiceValues(name1, name2);

		CfJdbcEnv cfJdbcEnv = new CfJdbcEnv();
		CfJdbcService cfJdbcService = cfJdbcEnv.findJdbcServiceByName("hanadb-1");
		assertThat(cfJdbcService.getUsername()).isEqualTo(username);
		assertThat(cfJdbcService.getPassword()).isEqualTo(password);
		assertThat(cfJdbcService.getDriverClassName()).isEqualTo("com.sap.db.jdbc.Driver");
	}

	// Utility methods

	private void assertJdbcServiceValues(String name1, String name2) {
		CfJdbcEnv cfJdbcEnv = new CfJdbcEnv();

		CfJdbcService hanaService1 = cfJdbcEnv.findJdbcServiceByName("hanadb-1");
		CfJdbcService hanaService2 = cfJdbcEnv.findJdbcServiceByName("hanadb-2");

		assertThat(getExpectedJdbcUrl("hana", name1)).isEqualTo(hanaService1.getJdbcUrl());
		assertThat(getExpectedJdbcUrl("hana", name2)).isEqualTo(hanaService2.getJdbcUrl());

		List<CfJdbcService> cfJdbcServices = cfJdbcEnv.findJdbcServices();
		assertThat(cfJdbcServices.size()).isEqualTo(2);

		assertThatThrownBy(() -> cfJdbcEnv.findJdbcService().getJdbcUrl()).isInstanceOf(IllegalArgumentException.class)
			.hasMessage("No unique database service found. Found database service names [hanadb-1, hanadb-2]");

		assertThat(hanaService1.getDriverClassName()).isEqualTo("com.sap.db.jdbc.Driver");
		assertThat(hanaService2.getDriverClassName()).isEqualTo("com.sap.db.jdbc.Driver");

	}

	private String getHanaServicePayload(String serviceName, String hostname, int port, String user, String password,
		String name) {
		return getTemplatedPayload("test-hana-btp-info.json", serviceName, hostname, port, user, password, name);
	}

	@Override
	protected String getExpectedJdbcUrl(String databaseType, String name) {
		return "jdbc:sap://%s:%s?encrypt=true&validateCertificate=true&currentschema=%s".formatted(hostname, port,
			username);
	}
}
