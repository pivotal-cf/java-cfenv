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
package io.pivotal.cfenv.boot.scs;

import java.util.Map;

import org.springframework.util.ClassUtils;

import io.pivotal.cfenv.core.CfCredentials;
import io.pivotal.cfenv.core.CfService;
import io.pivotal.cfenv.spring.boot.CfEnvProcessor;
import io.pivotal.cfenv.spring.boot.CfEnvProcessorProperties;

/**
 * Sets config-client properties with values found in service bindings of a
 * spring-cloud-services config-server service instance.
 * <p>
 * The {@code spring.cloud.config.client.oauth2.*} properties are always set. When the
 * OAuth2 support of Spring Cloud Config 5.1 or later is on the classpath, the properties
 * it reads ({@code spring.cloud.config.oauth2.*} and
 * {@code spring.security.oauth2.client.*}) are set as well.
 */
public class CfConfigClientProcessor implements CfEnvProcessor {

    static final String CONFIG_OAUTH2_CLASS = "org.springframework.cloud.config.client.oauth2.ConfigClientOAuth2Properties";

    static final String REGISTRATION_ID = "config-server";

    private static final String REGISTRATION_PREFIX = "spring.security.oauth2.client.registration." + REGISTRATION_ID;

    private static final String PROVIDER_PREFIX = "spring.security.oauth2.client.provider." + REGISTRATION_ID;

    private final ClassLoader classLoader;

    public CfConfigClientProcessor() {
        this(null);
    }

    /**
     * Create a processor that looks for Spring Cloud Config OAuth2 support in the given
     * class loader.
     * @param classLoader the class loader to check, or {@code null} for the default one
     */
    public CfConfigClientProcessor(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public boolean accept(CfService service) {
        return service.existsByTagIgnoreCase("configuration");
    }

    @Override
    public void process(CfCredentials cfCredentials, Map<String, Object> properties) {
        properties.put("spring.cloud.config.uri", cfCredentials.getUri());
        properties.put("spring.cloud.config.client.oauth2.client-id", cfCredentials.getString("client_id"));
        properties.put("spring.cloud.config.client.oauth2.client-secret", cfCredentials.getString("client_secret"));
        properties.put("spring.cloud.config.client.oauth2.access-token-uri", cfCredentials.getString("access_token_uri"));
        properties.put("spring.cloud.config.client.oauth2.scope", "");

        if (ClassUtils.isPresent(CONFIG_OAUTH2_CLASS, this.classLoader)) {
            properties.put("spring.cloud.config.oauth2.enabled", "true");
            properties.put("spring.cloud.config.oauth2.client-registration-id", REGISTRATION_ID);
            properties.put(REGISTRATION_PREFIX + ".client-id", cfCredentials.getString("client_id"));
            properties.put(REGISTRATION_PREFIX + ".client-secret", cfCredentials.getString("client_secret"));
            properties.put(REGISTRATION_PREFIX + ".authorization-grant-type", "client_credentials");
            properties.put(REGISTRATION_PREFIX + ".provider", REGISTRATION_ID);
            properties.put(PROVIDER_PREFIX + ".token-uri", cfCredentials.getString("access_token_uri"));
        }

        properties.put("spring.cloud.refresh.additional-property-sources-to-retain", this.getClass().getSimpleName());
    }

    @Override
    public CfEnvProcessorProperties getProperties() {
        return CfEnvProcessorProperties.builder()
                .propertyPrefixes("spring.cloud.config.client")
                .serviceName("Spring Cloud Config")
                .build();
    }

}
