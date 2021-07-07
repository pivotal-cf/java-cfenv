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
package io.pivotal.cfenv.boot.sso;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

public class FromAuthDomainTests {

    private CfSingleSignOnProcessor cfSingleSignOnProcessor;

    @Before
    public void setUp() {
        cfSingleSignOnProcessor = new CfSingleSignOnProcessor();
    }

    @Test
    public void testGetIssuerWithNonSystemZoneIssuer() {
        String authURI = "https://my-plan.login.run.pivotal.io";

        String issuerUri = cfSingleSignOnProcessor.fromAuthDomain(authURI);

        assertThat(issuerUri).isEqualTo("https://my-plan.uaa.run.pivotal.io");
    }

    @Test
    public void testGetIssuerWithSystemZoneIssuer() {
        String authURI = "https://login.run.pivotal.io";

        String issuerUri = cfSingleSignOnProcessor.fromAuthDomain(authURI);

        assertThat(issuerUri).isEqualTo("https://uaa.run.pivotal.io");
    }

    @Test
    public void testGetIssuerWithLoginBeforeSubdomainForSystemZone() {
        String authURIWithLoginBeforeSubdomain = "https://login:password@login.run.pivotal.io";

        String issuerUri = cfSingleSignOnProcessor.fromAuthDomain(authURIWithLoginBeforeSubdomain);

        assertThat(issuerUri).isEqualTo("https://login:password@uaa.run.pivotal.io");
    }

    @Test
    public void testGetIssuerWithLoginWithDotBeforeSubdomainForSystemZone() {
        String authURIWithLoginBeforeSubdomain = "https://login.lastname:password@login.run.pivotal.io";

        String issuerUri = cfSingleSignOnProcessor.fromAuthDomain(authURIWithLoginBeforeSubdomain);

        assertThat(issuerUri).isEqualTo("https://login.lastname:password@uaa.run.pivotal.io");
    }

    @Test
    public void testGetIssueWithLoginDuplicatedForNonSystemZone() {
        String authURIWithLoginBeforeSubdomain = "https://logincorp.login.run.pivotal.io";

        String issuerUri = cfSingleSignOnProcessor.fromAuthDomain(authURIWithLoginBeforeSubdomain);

        assertThat(issuerUri).isEqualTo("https://logincorp.uaa.run.pivotal.io");
    }

    @Test
    public void testMissingHostInUri() {
        String authUriMissingHost = "asdf";

        try {
            cfSingleSignOnProcessor.fromAuthDomain(authUriMissingHost);
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).isEqualTo("Unable to parse URI host from VCAP_SERVICES with label: \"p-identity\" and auth_domain: \"" + authUriMissingHost + "\"");
            return;
        }
        fail();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidAuthUri() {
        String invalidAuthUri = "asdf^";

        cfSingleSignOnProcessor.fromAuthDomain(invalidAuthUri);
    }
}
