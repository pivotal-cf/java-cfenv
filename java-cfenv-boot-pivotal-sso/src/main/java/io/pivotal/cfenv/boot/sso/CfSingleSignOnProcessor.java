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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.pivotal.cfenv.core.CfCredentials;
import io.pivotal.cfenv.core.CfService;
import io.pivotal.cfenv.spring.boot.CfEnvProcessor;
import io.pivotal.cfenv.spring.boot.CfEnvProcessorProperties;

/**
 * @author Pivotal Application Single Sign-On
 * Mapping values for // https://docs.spring.io/spring-security/site/docs/current/reference/html5/#oauth2login-boot-property-mappings
 */
public class CfSingleSignOnProcessor implements CfEnvProcessor {
    private static final String PIVOTAL_SSO_LABEL = "p-identity";
    private static final String SPRING_SECURITY_CLIENT = "spring.security.oauth2.client";
    private static final String PROVIDER_ID = "sso";
    private static final String BASE_CLIENT_REGISTRATION_ID = "sso";
    private static final String AUTHCODE_CLIENT_REGISTRATION_ID = BASE_CLIENT_REGISTRATION_ID + "authorizationcode";
    private static final String CLIENTCRED_CLIENT_REGISTRATION_ID = BASE_CLIENT_REGISTRATION_ID + "clientcredentials";
    private static final String SSO_SERVICE = "ssoServiceUrl";
    private static final String AUTHORIZATION_CODE = "authorization_code";
    private static final String CLIENT_CREDENTIALS = "client_credentials";
    private static final Set<String> AUTH_CODE_AND_CLIENT_CREDS = new HashSet<>(Arrays.asList(AUTHORIZATION_CODE, CLIENT_CREDENTIALS));

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
        properties.put(SPRING_SECURITY_CLIENT + ".provider." + PROVIDER_ID + ".issuer-uri", issuer + "/oauth/token");
        properties.put(SPRING_SECURITY_CLIENT + ".provider." + PROVIDER_ID + ".authorization-uri", authDomain + "/oauth/authorize");
        if(SpringSecurityDetector.isSpringResourceServerPresent()) {
            properties.put("spring.security.oauth2.resourceserver.jwt.issuer-uri", issuer + "/oauth/token");
        }

        ArrayList<String> grantTypes = (ArrayList<String>) cfCredentials.getMap().get("grant_types");
        if (grantTypes != null && isAuthCodeAndClientCreds(grantTypes)) {
            mapBasicClientProperties(properties, AUTHCODE_CLIENT_REGISTRATION_ID, clientId, clientSecret);
            properties.put(SPRING_SECURITY_CLIENT + ".registration." + AUTHCODE_CLIENT_REGISTRATION_ID + ".authorization-grant-type", AUTHORIZATION_CODE);

            mapBasicClientProperties(properties, CLIENTCRED_CLIENT_REGISTRATION_ID, clientId, clientSecret);
            properties.put(SPRING_SECURITY_CLIENT + ".registration." + CLIENTCRED_CLIENT_REGISTRATION_ID + ".authorization-grant-type", CLIENT_CREDENTIALS);
        } else if (grantTypes != null && grantTypes.size() == 1) { // if one grant type
            mapBasicClientProperties(properties, BASE_CLIENT_REGISTRATION_ID, clientId, clientSecret);
            String grantType = grantTypes.get(0);
            properties.put(SPRING_SECURITY_CLIENT + ".registration." + BASE_CLIENT_REGISTRATION_ID + ".authorization-grant-type", grantType);
        } else { // if grant type is empty, invalid combo, or more than 2 grant types
            mapBasicClientProperties(properties, BASE_CLIENT_REGISTRATION_ID, clientId, clientSecret);
        }
    }

    private boolean isAuthCodeAndClientCreds(ArrayList<String> grantTypes) {
        return (new HashSet<>(grantTypes)).equals(AUTH_CODE_AND_CLIENT_CREDS);
    }

    private void mapBasicClientProperties(Map<String, Object> properties, String clientRegistrationId, String clientId, String clientSecret) {
        String registrationPrefix = SPRING_SECURITY_CLIENT + ".registration." + clientRegistrationId;
        properties.put(registrationPrefix + ".client-id", clientId);
        properties.put(registrationPrefix + ".client-secret", clientSecret);
        properties.put(registrationPrefix + ".client-name", clientRegistrationId);
        properties.put(registrationPrefix + ".redirect-uri", "{baseUrl}/login/oauth2/code/{registrationId}");
        properties.put(registrationPrefix + ".provider", PROVIDER_ID);
    }

    @Override
    public CfEnvProcessorProperties getProperties() {
        return CfEnvProcessorProperties.builder()
                .propertyPrefixes(String.join(",", SSO_SERVICE, SPRING_SECURITY_CLIENT))
                .serviceName("Single Sign On").build();
    }

    String fromAuthDomain(String authUri) {
        URI uri = URI.create(authUri);

        String host = uri.getHost();

        if (host == null) {
            throw new IllegalArgumentException("Unable to parse URI host from VCAP_SERVICES with label: \"" + PIVOTAL_SSO_LABEL + "\" and auth_domain: \"" + authUri + "\"");
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
