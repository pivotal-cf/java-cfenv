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
import java.util.Map;

import io.pivotal.cfenv.core.CfCredentials;
import io.pivotal.cfenv.core.CfService;
import io.pivotal.cfenv.spring.boot.CfEnvProcessor;
import io.pivotal.cfenv.spring.boot.CfEnvProcessorProperties;

import org.springframework.web.util.UriComponentsBuilder;

/**
 * @author Mark Pollack
 * @author Scott Frederick
 * @author Pivotal Application Single Sign-On
 */
public class CfSingleSignOnProcessor implements CfEnvProcessor {
    private static final String PIVOTAL_SSO_LABEL = "p-identity";
    private static final String SSO_SERVICE = "ssoServiceUrl";
    private static final String SPRING_SECURITY_CLIENT = "spring.security.oauth2.client";

    @Override
    public boolean accept(CfService service) {
        return SpringSecurityDetector.isSpringSecurityPresent() && service.existsByLabelStartsWith(PIVOTAL_SSO_LABEL);
    }

    @Override
    public void process(CfCredentials cfCredentials, Map<String, Object> properties) {
        String clientId = cfCredentials.getString("client_id");
        String clientSecret = cfCredentials.getString("client_secret");
        String authDomain = cfCredentials.getString("auth_domain");
        String issuer = getIssuer(authDomain);

        properties.put(SSO_SERVICE, authDomain);
        properties.put(SPRING_SECURITY_CLIENT + ".registration.sso.client-id", clientId);
        properties.put(SPRING_SECURITY_CLIENT + ".registration.sso.client-secret", clientSecret);
        properties.put(SPRING_SECURITY_CLIENT + ".registration.sso.authorization-grant-type", "${GRANT_TYPE}");
        properties.put(SPRING_SECURITY_CLIENT + ".registration.sso.client-name", "sso");
        properties.put(SPRING_SECURITY_CLIENT + ".registration.sso.redirect-uri", "{baseUrl}/login/oauth2/code/{registrationId}");
        properties.put(SPRING_SECURITY_CLIENT + ".registration.sso.scope", "${SSO_SCOPES}");
        properties.put(SPRING_SECURITY_CLIENT + ".provider.sso.issuer-uri", issuer + "/oauth/token");
        properties.put(SPRING_SECURITY_CLIENT + ".provider.sso.authorization-uri", authDomain + "/oauth/authorize");
    }

    private String getIssuer(String authDomain) {
        URI uri = UriComponentsBuilder.fromHttpUrl(authDomain).build().toUri();
        UriComponentsBuilder issueBuilder = UriComponentsBuilder.fromUri(uri);
        String host = uri.getHost();

        // Bound app is using the System Zone
        if (host.startsWith("login")) {
            issueBuilder.host(host.replaceFirst("login", "uaa"));
        } else {
            issueBuilder.host(host.replaceFirst("(.*)\\.login\\.(.*)", "$1.uaa.$2"));
        }

        return issueBuilder.build().toString();
    }

    @Override
    public CfEnvProcessorProperties getProperties() {
        return CfEnvProcessorProperties.builder()
                .propertyPrefixes(String.join(",", SSO_SERVICE, SPRING_SECURITY_CLIENT))
                .serviceName("Single Sign On").build();
    }
}
