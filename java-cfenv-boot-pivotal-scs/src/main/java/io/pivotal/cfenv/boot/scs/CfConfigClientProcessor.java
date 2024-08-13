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

import io.pivotal.cfenv.core.CfCredentials;
import io.pivotal.cfenv.core.CfService;
import io.pivotal.cfenv.spring.boot.CfEnvProcessor;
import io.pivotal.cfenv.spring.boot.CfEnvProcessorProperties;

/**
 * Sets config-client properties with values found in service bindings of a
 * spring-cloud-services config-server service instance.
 */
public class CfConfigClientProcessor implements CfEnvProcessor {

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
