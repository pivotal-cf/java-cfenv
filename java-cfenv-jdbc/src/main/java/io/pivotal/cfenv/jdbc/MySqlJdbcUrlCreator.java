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

import io.pivotal.cfenv.core.CfCredentials;
import io.pivotal.cfenv.core.CfService;
import io.pivotal.cfenv.core.UriInfo;

/**
 * @author Mark Pollack
 */
public class MySqlJdbcUrlCreator extends AbstractJdbcUrlCreator {
	public static final String MYSQL_SCHEME = "mysql";
	public static final String MARIADB_SCHEME = "mariadb";
	public static final String[] MYSQL_SCHEMES = new String[]{MYSQL_SCHEME, MARIADB_SCHEME};
	public static final String MYSQL_TAG = "mysql";
	public static final String MARIADB_TAG = "mariadb";
	public static final String[] MYSQL_TAGS = new String[]{MYSQL_TAG, MARIADB_TAG};
	public static final String MYSQL_LABEL = "mysql";
	public static final String MARIADB_LABEL = "mariadb";
	public static final String[] MYSQL_LABELS = new String[]{MYSQL_LABEL, MARIADB_LABEL};
	public static final String MYSQL_DRIVER_CLASS_NAME = "com.mysql.cj.jdbc.Driver";
	public static final String MARIADB_DRIVER_CLASS_NAME = "org.mariadb.jdbc.Driver";

	@Override
	public boolean isDatabaseService(CfService cfService) {
		return jdbcUrlMatchesScheme(cfService, MYSQL_SCHEMES)
				|| cfService.existsByTagIgnoreCase(MYSQL_TAGS)
				|| cfService.existsByLabelStartsWith(MYSQL_LABELS)
				|| cfService.existsByUriSchemeStartsWith(MYSQL_SCHEMES)
				|| cfService.existsByCredentialsContainsUriField(MYSQL_SCHEMES);
	}

	private boolean isMariaDbService(CfService cfService) {
		return jdbcUrlMatchesScheme(cfService, MARIADB_SCHEME)
				|| cfService.existsByTagIgnoreCase(MARIADB_TAG)
				|| cfService.existsByLabelStartsWith(MARIADB_LABEL)
				|| cfService.existsByUriSchemeStartsWith(MARIADB_SCHEME)
				|| cfService.existsByCredentialsContainsUriField(MARIADB_SCHEME);
	}

	@Override
	public String getDriverClassName() {
		String driverClassNameToUse;
		try {
			driverClassNameToUse = MARIADB_DRIVER_CLASS_NAME;
			Class.forName(driverClassNameToUse, false, getClass().getClassLoader());
		}
		catch (ClassNotFoundException e) {
			try {
				driverClassNameToUse = MYSQL_DRIVER_CLASS_NAME;
				Class.forName(driverClassNameToUse, false, getClass().getClassLoader());
			}
			catch (ClassNotFoundException e2) {
				return null;
			}
		}
		return driverClassNameToUse;
	}

	@Override
	public String createJdbcUrl(CfService cfService) {
		CfCredentials cfCredentials = cfService.getCredentials();
		String jdbcUrl = (String) cfCredentials.getMap().get("jdbcUrl");
		if (jdbcUrl == null) {
			jdbcUrl = buildJdbcUrlFromUriField(cfCredentials);
		}
		if (isMariaDbService(cfService) && getDriverClassName().equals(MARIADB_DRIVER_CLASS_NAME)) {
			jdbcUrl = jdbcUrl.replaceFirst("^(jdbc:mysql)", "jdbc:mariadb");
		}
		return jdbcUrl;
	}

	@Override
	public String buildJdbcUrlFromUriField(CfCredentials cfCredentials) {
		UriInfo uriInfo = cfCredentials.getUriInfo(MYSQL_SCHEME);
		return String.format("%s%s://%s%s/%s%s%s", JDBC_PREFIX, MYSQL_SCHEME,
				uriInfo.getHost(), uriInfo.formatPort(), uriInfo.getPath(),
				uriInfo.formatUserNameAndPasswordQuery(), uriInfo.formatQuery());
	}
}
