package io.pivotal.cfenv.jdbc;

import static io.pivotal.cfenv.jdbc.AbstractJdbcUrlCreator.JDBC_PREFIX;

import io.pivotal.cfenv.core.CfCredentials;
import io.pivotal.cfenv.core.CfService;

public class HanaJdbcUrlCreator implements JdbcUrlCreator {
	@Override
	public boolean isDatabaseService(CfService cfService) {
		return jdbcUrlMatchesScheme(cfService, "sap");
	}

	@Override
	public String createJdbcUrl(CfService cfService) {
		CfCredentials credentials = cfService.getCredentials();
		String host = credentials.getHost();
		String port = credentials.getPort();
		String user = credentials.getUsername();

		return "jdbc:sap://%s:%s?encrypt=true&validateCertificate=true&currentschema=%s".formatted(host, port, user);
	}

	@Override
	public String getDriverClassName() {
		return "com.sap.db.jdbc.Driver";
	}

	@SuppressWarnings("SameParameterValue")
	protected boolean jdbcUrlMatchesScheme(CfService cfService, String... uriSchemes) {
		CfCredentials cfCredentials = cfService.getCredentials();
		String jdbcUrl = (String) cfCredentials.getMap().get("url");

		if (jdbcUrl != null) {
			for (String uriScheme : uriSchemes) {
				if (jdbcUrl.startsWith(JDBC_PREFIX + uriScheme + ":")) {
					return true;
				}
			}
		}

		return false;
	}
}
