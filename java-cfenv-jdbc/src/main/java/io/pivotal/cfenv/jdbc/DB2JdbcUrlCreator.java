/*
 * Copyright 2019-2023 the original author or authors.
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

import java.util.Map;

import io.pivotal.cfenv.core.CfCredentials;
import io.pivotal.cfenv.core.CfService;
import io.pivotal.cfenv.core.UriInfo;

/**
 * @author Mark Pollack
 */
public class DB2JdbcUrlCreator extends AbstractJdbcUrlCreator  {

	public static final String DB2_SCHEME = "db2";

	public static final String[] DB2_TAGS = new String[]{"sqldb", "dashDB", "db2"};

	public static final String DB2_LABEL = "db2";

	@Override
	public boolean isDatabaseService(CfService cfService) {
		if (jdbcUrlMatchesScheme(cfService, DB2_SCHEME)
				|| cfService.existsByTagIgnoreCase(DB2_TAGS)
				|| cfService.existsByLabelStartsWith(DB2_LABEL)
				|| cfService.existsByUriSchemeStartsWith(DB2_SCHEME)
				|| cfService.existsByCredentialsContainsUriField(DB2_SCHEME)) {
			return true;
		}
		return false;
	}

	@Override
	public String buildJdbcUrlFromUriField(CfCredentials cfCredentials) {
		String jdbcUrl;
		UriInfo uriInfo = cfCredentials.getUriInfo(DB2_SCHEME);
		String jdbcStartingValue = searchFieldWithJdbcStartingValue(cfCredentials);
		//	if we entered this method, we know there was no jdbcUrl in cfCredentials;
		//	but if there's another entry with a value starting with jdbc:db2, that's the second best!
		if (jdbcStartingValue != null) {
			jdbcUrl = jdbcStartingValue;
		} else {
			jdbcUrl = String.format("jdbc:%s://%s:%d/%s:user=%s;password=%s;",
					DB2_SCHEME, uriInfo.getHost(), uriInfo.getPort(), uriInfo.getPath(),
					uriInfo.getUsername(), uriInfo.getPassword());
		}
		// DB2Driver does not support to have additional properties besides its jdbcUrl, see ERRORCODE=-4461
		cfCredentials.getMap().entrySet().removeIf(entry -> entry.getKey().toLowerCase().startsWith("user"));
		cfCredentials.getMap().entrySet().removeIf(entry -> entry.getKey().toLowerCase().startsWith("password"));
		return jdbcUrl;
	}


	@Override
	public String getDriverClassName() {
		return "com.ibm.db2.jcc.DB2Driver";
	}

	String searchFieldWithJdbcStartingValue(CfCredentials cfCredentials) {
		return (String) cfCredentials.getMap().entrySet().stream()
				.filter(entry -> entry.getValue() instanceof String
						&& ((String) entry.getValue()).startsWith("jdbc:db2")
						&& !entry.getKey().toLowerCase().startsWith("masked")
						)
				.map(Map.Entry::getValue)
				.findFirst()
				.orElse(null);
	}
}
