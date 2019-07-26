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


import io.pivotal.cfenv.test.AbstractCfEnvTests;
import mockit.Mock;
import mockit.MockUp;

/**
 * @author Pivotal Application Single Sign-On
 */
public class CfSingleSignOnTestSupport extends AbstractCfEnvTests {

    String getSsoServicePayload(String authDomain) {
        return readTestDataFile("test-identity.json")
                .replace("$authDomain", authDomain);
    }

    String getSsoServicePayload(String authDomain, String grantType) {
        return readTestDataFile("test-identity-with-one-grant-types.json")
                .replace("$authDomain", authDomain)
                .replace("$grantType", grantType);
    }

    String getSsoServicePayload(String authDomain, String grantType1, String grantType2) {
        return readTestDataFile("test-identity-with-two-grant-types.json")
                .replace("$authDomain", authDomain)
                .replace("$grantType-1", grantType1)
                .replace("$grantType-2", grantType2);
    }

    public static MockUp<?> mockSpringSecurityDetector(boolean isPresent, boolean isLegacyPresent) {
        return new MockUp<SpringSecurityDetector>() {
            @Mock
            public boolean isSpringSecurityPresent() {
                return isPresent;
            }

            @Mock
            public boolean isLegacySpringSecurityPresent() {
                return isLegacyPresent;
            }
        };
    }

}
