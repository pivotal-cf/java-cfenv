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

	public static final String MYSQL_TAG = "mysql";

	public static final String MYSQL_LABEL = "mysql";

	@Override
	public boolean isDatabaseService(CfService cfService) {
		if (jdbcUrlMatchesScheme(cfService, MYSQL_SCHEME)
				|| cfService.existsByTagIgnoreCase(MYSQL_TAG)
				|| cfService.existsByLabelStartsWith(MYSQL_LABEL)
				|| cfService.existsByUriSchemeStartsWith(MYSQL_SCHEME)
				|| cfService.existsByCredentialsContainsUriField(MYSQL_SCHEME)) {
			return true;
		}
		return false;
	}

	@Override
	public String getDriverClassName() {
		String driverClassNameToUse = null;
		try {
			driverClassNameToUse = "org.mariadb.jdbc.Driver";
			Class.forName(driverClassNameToUse, false, getClass().getClassLoader());
		} catch (ClassNotFoundException e) {
			try {
				driverClassNameToUse = "com.mysql.cj.jdbc.Driver";
				Class.forName(driverClassNameToUse, false, getClass().getClassLoader());
			} catch (ClassNotFoundException e2) {
				return null;
			}
		}
		return driverClassNameToUse;
	}

	@Override
	public String buildJdbcUrlFromUriField(CfCredentials cfCredentials) {
		UriInfo uriInfo = cfCredentials.getUriInfo(MYSQL_SCHEME);
		return String.format("%s%s://%s%s/%s%s%s", JDBC_PREFIX, MYSQL_SCHEME,
				uriInfo.getHost(), uriInfo.formatPort(), uriInfo.getPath(),
				uriInfo.formatUserNameAndPasswordQuery(), uriInfo.formatQuery());
	}

}
