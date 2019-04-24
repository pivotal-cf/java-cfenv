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
import java.util.Map;

import io.pivotal.cfenv.core.CfCredentials;
import io.pivotal.cfenv.core.CfService;
import io.pivotal.cfenv.spring.boot.CfEnvProcessor;
import io.pivotal.cfenv.spring.boot.CfEnvProcessorProperties;

/**
 * @author Pivotal Application Single Sign-On
 */
public class CfSingleSignOnProcessor implements CfEnvProcessor {
    private static final String PIVOTAL_SSO_LABEL = "p-identity";
    private static final String SPRING_SECURITY_CLIENT = "spring.security.oauth2.client";
    private static final String SSO_CLIENT_NAME = "sso";
    private static final String SSO_SERVICE = "ssoServiceUrl";

    @Override
    public boolean accept(CfService service) {
        return SpringSecurityDetector.isSpringSecurityPresent() && service.existsByLabelStartsWith(PIVOTAL_SSO_LABEL);
    }

    @Override
    public void process(CfCredentials cfCredentials, Map<String, Object> properties) {
        String clientId = cfCredentials.getString("client_id");
        String clientSecret = cfCredentials.getString("client_secret");
        String authDomain = cfCredentials.getString("auth_domain");
        String issuer = fromAuthDomain(authDomain);

        properties.put(SSO_SERVICE, authDomain);
        properties.put(SPRING_SECURITY_CLIENT + ".registration.sso.client-id", clientId);
        properties.put(SPRING_SECURITY_CLIENT + ".registration.sso.client-secret", clientSecret);
        properties.put(SPRING_SECURITY_CLIENT + ".registration.sso.client-name", SSO_CLIENT_NAME);
        properties.put(SPRING_SECURITY_CLIENT + ".registration.sso.redirect-uri", "{baseUrl}/login/oauth2/code/{registrationId}");
        properties.put(SPRING_SECURITY_CLIENT + ".provider.sso.issuer-uri", issuer + "/oauth/token");
        properties.put(SPRING_SECURITY_CLIENT + ".provider.sso.authorization-uri", authDomain + "/oauth/authorize");
    }

    @Override
    public CfEnvProcessorProperties getProperties() {
        return CfEnvProcessorProperties.builder()
                .propertyPrefixes(String.join(",", SSO_SERVICE, SPRING_SECURITY_CLIENT))
                .serviceName("Single Sign On").build();
    }

    public String fromAuthDomain(String authUri) {
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
