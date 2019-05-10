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
package io.pivotal.cfenv.boot.sso;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author Pivotal Application Single Sign-On
 */
class IssuerUtils {
    private static final String PIVOTAL_SSO_LABEL = "p-identity";

    public static String fromAuthDomain(String authUri) {
        URI uri = URI.create(authUri);

        String host = uri.getHost();

        if (host == null) {
            throw new IllegalArgumentException("Unable to parse URI host from VCAP_SERVICES with label: \"" + PIVOTAL_SSO_LABEL + "\" and auth_domain: \"" + authUri +"\"");
        }

        String issuerHost = uri.getHost().replaceFirst("login\\.", "uaa.");

        try {
            return new URI(
                    uri.getScheme(),
                    uri.getUserInfo(),
                    issuerHost,
                    uri.getPort(),
                    uri.getPath(),
                    uri.getQuery(),
                    uri.getFragment()
            ).toString();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }
}
