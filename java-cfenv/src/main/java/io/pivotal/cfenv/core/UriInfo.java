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

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * Utility class that allows expressing URIs in alternative forms: individual fields or a
 * URI string
 *
 * @author Ramnivas Laddad
 * @author Scott Frederick
 */
public class UriInfo {

	private String scheme;

	private String host;

	private int port;

	private String userName;

	private String password;

	private String path;

	private String query;

	private String uriString;

	private String hosts;

	public UriInfo(String scheme, String host, int port, String username,
				   String password) {
		this(scheme, host, port, username, password, null, null);
	}

	public UriInfo(String scheme, String host, int port, String username, String password,
				   String path) {
		this(scheme, host, port, username, password, path, null);
	}

	public UriInfo(String scheme, String host, int port, String username, String password,
				   String path, String query) {
		this.scheme = scheme;
		this.host = host;
		this.port = port;
		this.userName = username;
		this.password = password;
		this.path = path;
		this.query = query;

		this.uriString = buildUri().toString();
	}

	public UriInfo(String uriString) {
		this.uriString = uriString;

		URI uri = getUri();
		this.scheme = uri.getScheme();
		this.path = parsePath(uri);
		this.query = uri.getQuery();

		String authority = uri.getAuthority();
		if (uri.getHost() != null) {
			this.host = uri.getHost();
			this.port = uri.getPort();

			String[] userinfo = parseUserinfo(uri);
			this.userName = urlDecode(userinfo[0]);
			this.password = urlDecode(userinfo[1]);
		}
		else if (authority != null && authority.substring(authority.lastIndexOf('@') + 1).contains(",")) {
			// A multi-host authority (e.g. postgresql's
			// host1:port1,host2:port2 failover syntax) isn't valid
			// server-based authority per RFC 3986, so java.net.URI parses
			// it as a registry-based authority: getHost()/getPort() come
			// back null/-1 even though getAuthority() has the full value.
			parseAuthority(authority);
		}
		else {
			// Some non-standard, driver-specific formats (e.g. SQL Server's
			// ;property=value suffix) also fail server-based parsing but
			// aren't a list of hosts either; leave host/port unset as before
			// and let the caller fall back to its own parsing of uriString.
			this.host = uri.getHost();
			this.port = uri.getPort();
		}
	}

	private void parseAuthority(String authority) {
		String hostsPart = authority;

		int at = authority.lastIndexOf('@');
		if (at != -1) {
			String userInfo = authority.substring(0, at);
			hostsPart = authority.substring(at + 1);

			String[] userPass = userInfo.split(":");
			if (userPass.length != 2) {
				throw new IllegalArgumentException("Bad userinfo in URI: " + uriString);
			}
			this.userName = urlDecode(userPass[0]);
			this.password = urlDecode(userPass[1]);
		}

		this.hosts = hostsPart;

		String firstHost = hostsPart.split(",")[0];
		int colon = firstHost.lastIndexOf(':');
		if (colon != -1) {
			this.host = firstHost.substring(0, colon);
			this.port = Integer.parseInt(firstHost.substring(colon + 1));
		}
		else {
			this.host = firstHost;
			this.port = -1;
		}
	}

	public static String urlDecode(String s) {
		if (s == null) {
			return null;
		}

		try {
			// URLDecode decodes '+' to a space, as for
			// form encoding. So protect plus signs.
			return URLDecoder.decode(s.replace("+", "%2B"), "US-ASCII");
		} catch (UnsupportedEncodingException e) {
			// US-ASCII is always supported
			throw new RuntimeException(e);
		}
	}

	public static String urlEncode(String s) {
		try {
			return URLEncoder.encode(s, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public String getScheme() {
		return scheme;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public String getUsername() {
		return userName;
	}

	public String getPassword() {
		return password;
	}

	public String getPath() {
		return path;
	}

	public String getQuery() {
		return query;
	}

	public URI getUri() {
		try {
			return new URI(uriString);
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException("Invalid URI " + uriString, e);
		}
	}

	public String getUriString() {
		return uriString;
	}

	public String formatUserNameAndPasswordQuery() {
		if (userName != null && password != null) {
			return String.format("?user=%s&password=%s", UriInfo.urlEncode(userName),
					UriInfo.urlEncode(password));
		}
		if (userName != null) {
			return String.format("?user=%s", UriInfo.urlEncode(userName));
		}
		return "";
	}

	public String formatPort() {
		if (getPort() != -1) {
			return String.format(":%d", getPort());
		}
		return "";
	}

	/**
	 * Returns the host(s) and port(s) as they should appear in the authority
	 * section of a URI. For a single-host URI this is equivalent to
	 * {@code getHost() + formatPort()}. For a multi-host URI (e.g.
	 * PostgreSQL's {@code host1:port1,host2:port2} failover syntax), the
	 * full, unmodified host list is returned since {@link #getHost()} and
	 * {@link #getPort()} only expose the first host.
	 */
	public String getHostAndPort() {
		if (hosts != null) {
			return hosts;
		}
		return getHost() + formatPort();
	}

	public String formatQuery() {
		if (getQuery() != null) {
			if (formatUserNameAndPasswordQuery().isEmpty()) {
				return String.format("?%s", getQuery());
			} else {
				return String.format("&%s", getQuery());
			}
		}
		return "";
	}

	private URI buildUri() {
		String userInfo = null;

		if (userName != null && password != null) {
			userInfo = userName + ":" + password;
		}

		String cleanedPath = path == null || path.startsWith("/") ? path : "/" + path;

		try {
			return new URI(scheme, userInfo, host, port, cleanedPath, query, null);
		} catch (URISyntaxException e) {
			String details = String.format("Error creating URI with components: "
							+ "scheme=%s, userInfo=%s, host=%s, port=%d, path=%s, query=%s",
					scheme, userInfo, host, port, cleanedPath, query);
			throw new IllegalArgumentException(details, e);
		}
	}

	private String[] parseUserinfo(URI uri) {
		String userInfo = uri.getRawUserInfo();

		if (userInfo != null) {
			String[] userPass = userInfo.split(":");
			if (userPass.length != 2) {
				throw new IllegalArgumentException("Bad userinfo in URI: " + uri);
			}
			return userPass;
		}

		return new String[]{null, null};
	}

	private String parsePath(URI uri) {
		String rawPath = uri.getRawPath();
		if (rawPath != null && rawPath.length() > 1) {
			return rawPath.substring(1);
		}
		return null;
	}

	@Override
	public String toString() {
		return uriString;
	}

}
