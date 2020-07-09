/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.pivotal.cfenv.jdbc;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.pivotal.cfenv.core.CfCredentials;
import io.pivotal.cfenv.core.CfService;
import io.pivotal.cfenv.core.UriInfo;

/**
 * @author Mark Pollack
 */
public class SqlServerJdbcUrlCreator extends AbstractJdbcUrlCreator {

	public static final String SQLSERVER_SCHEME = "sqlserver";

	public static final String SQLSERVER_LABEL = "sqlserver";

	@Override
	public String buildJdbcUrlFromUriField(CfCredentials cfCredentials) {
		UriInfo uriInfo = cfCredentials.getUriInfo(SQLSERVER_SCHEME);
		Map<String, String> uriParameters = parseSqlServerUriParameters(uriInfo.getUriString());
		String databaseName = getDatabaseName(uriParameters);
		try {
			URI uri = new URI(uriInfo.getUriString().substring(0, uriInfo.getUriString().indexOf(";")));
			return String.format("jdbc:%s://%s%s%s%s%s%s",
					SQLSERVER_SCHEME,
					uri.getHost(),
					uri.getPort() > 0 ? ":" + uri.getPort() : "",
					uri.getPath() != null && !uri.getPath().isEmpty() ? "/" + uri.getPath() : "",
					databaseName != null ? ";database=" + UriInfo.urlEncode(databaseName) : "",
					uriParameters.containsKey("user") ? ";user=" +  UriInfo.urlEncode(uriParameters.get("user")) : "",
					uriParameters.containsKey("password") ? ";password=" +  UriInfo.urlEncode(uriParameters.get("password")) : ""
			);
		}
		catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	private String getDatabaseName(Map<String, String> uriParameters) {
		String databaseName = null;
		if (uriParameters.containsKey("databaseName")) {
			databaseName = uriParameters.get("databaseName");
		} else if (uriParameters.containsKey("database")) {
			databaseName = uriParameters.get("database");
		}
		return databaseName;
	}

	private Map<String, String> parseSqlServerUriParameters(String uriString) {
		return Arrays.stream(uriString.split(";"))
					.filter(s -> s.contains("="))
					.map(s -> s.split("="))
					.filter(t -> t.length == 2)
					.collect(Collectors.toMap(parameterName(), parameterValue(), takeFirst()));
	}

	private BinaryOperator<String> takeFirst() {
		return (s, s2) -> s;
	}

	private Function<String[], String> parameterValue() {
		return t -> UriInfo.urlDecode(t[1]);
	}

	private Function<String[], String> parameterName() {
		return t -> UriInfo.urlDecode(t[0]);
	}

	@Override
	public boolean isDatabaseService(CfService cfService) {
		// Match tags
		return jdbcUrlMatchesScheme(cfService, SQLSERVER_SCHEME)
				|| cfService.existsByLabelStartsWith(SQLSERVER_LABEL)
				|| cfService.existsByUriSchemeStartsWith(SQLSERVER_SCHEME)
				|| cfService.existsByCredentialsContainsUriField(SQLSERVER_SCHEME);
	}

	@Override
	public String getDriverClassName() {
		return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
	}
}
