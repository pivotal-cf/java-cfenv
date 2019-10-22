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
package io.pivotal.cfenv.boot.scs.serviceregistry;

import java.util.Map;

import io.pivotal.cfenv.core.CfCredentials;
import io.pivotal.cfenv.core.CfService;
import io.pivotal.cfenv.spring.boot.CfEnvProcessor;
import io.pivotal.cfenv.spring.boot.CfEnvProcessorProperties;

/**
 * Sets eureka-client properties with values found in service bindings of a
 * spring-cloud-services service-registry service instance.
 *
 * @author Will Tran
 * @author Dylan Roberts
 */
public class CfEurekaClientProcessor implements CfEnvProcessor {

    @Override
    public boolean accept(CfService service) {
        return service.existsByTagIgnoreCase("eureka");
    }

    @Override
    public void process(CfCredentials cfCredentials, Map<String, Object> properties) {
        String uri = cfCredentials.getUri();
        String clientId = cfCredentials.getString("client_id");
        String clientSecret = cfCredentials.getString("client_secret");
        String accessTokenUri = cfCredentials.getString("access_token_uri");

        properties.put("eureka.client.serviceUrl.defaultZone", uri + "/eureka/");
        properties.put("eureka.client.region", "default");
        properties.put("eureka.client.oauth2.client-id", clientId);
        properties.put("eureka.client.oauth2.client-secret", clientSecret);
        properties.put("eureka.client.oauth2.access-token-uri", accessTokenUri);
    }

    @Override
    public CfEnvProcessorProperties getProperties() {
        return CfEnvProcessorProperties.builder()
                .propertyPrefixes("eureka.client")
                .serviceName("Spring Cloud Netflix Eureka").build();
    }

}
