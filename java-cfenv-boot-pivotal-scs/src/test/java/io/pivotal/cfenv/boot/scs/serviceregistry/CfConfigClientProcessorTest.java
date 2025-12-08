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
package io.pivotal.cfenv.boot.scs.serviceregistry;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import io.pivotal.cfenv.boot.scs.CfConfigClientProcessor;
import io.pivotal.cfenv.core.CfCredentials;
import io.pivotal.cfenv.core.CfService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class CfConfigClientProcessorTest {
    private static final String URI = "uri";
    private static final String CLIENT_ID = "clientId";
    private static final String CLIENT_SECRET = "clientSecret";
    private static final String ACCESS_TOKEN_URI = "accessTokenUri";

    @Mock
    private CfService configService;
    @Mock
    private CfService otherService;
    @Mock
    private CfCredentials cfCredentials;

    @InjectMocks
    private CfConfigClientProcessor configClientProcessor;

    @BeforeEach
    public void setUp() {
        when(configService.existsByTagIgnoreCase("configuration")).thenReturn(true);

        when(cfCredentials.getUri()).thenReturn(URI);
        when(cfCredentials.getString("client_id")).thenReturn(CLIENT_ID);
        when(cfCredentials.getString("client_secret")).thenReturn(CLIENT_SECRET);
        when(cfCredentials.getString("access_token_uri")).thenReturn(ACCESS_TOKEN_URI);
    }

    @Test
    public void shouldAcceptConfigService() {
        boolean actual = configClientProcessor.accept(configService);

        assertThat(actual).isTrue();
    }

    @Test
    public void shouldNotAcceptOtherService() {
        boolean actual = configClientProcessor.accept(otherService);

        assertThat(actual).isFalse();
    }

    @Test
    public void shouldProcessCfCredentials() {
        Map<String, Object> properties = new HashMap<>();

        configClientProcessor.process(cfCredentials, properties);

        assertThat(properties.get("spring.cloud.config.uri")).isEqualTo( URI);
        assertThat(properties.get("spring.cloud.config.client.oauth2.client-id")).isEqualTo(CLIENT_ID);
        assertThat(properties.get("spring.cloud.config.client.oauth2.client-secret")).isEqualTo(CLIENT_SECRET);
        assertThat(properties.get("spring.cloud.config.client.oauth2.access-token-uri")).isEqualTo(ACCESS_TOKEN_URI);
        assertThat(properties.get("spring.cloud.config.client.oauth2.scope")).isEqualTo("");
    }

    @Test
    public void shouldRetainThePropertiesDuringRefresh() {
        Map<String, Object> properties = new HashMap<>();

        configClientProcessor.process(cfCredentials, properties);

        assertThat(properties.get("spring.cloud.refresh.additional-property-sources-to-retain"))
                .isEqualTo("CfConfigClientProcessor");
    }
}
