/*
 * Copyright 2011-2023 the original author or authors.
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

package io.pivotal.cfenv.profile;

import java.util.function.Consumer;

import io.pivotal.cfenv.core.CfEnv;
import io.pivotal.cfenv.core.CfEnvSingleton;

/**
 * Originally contributed by Dylan Roberts in
 * https://github.com/cloudfoundry/java-buildpack-auto-reconfiguration/pull/76/files
 */
public class DefaultCfEnvHolder implements CfEnvHolder {

    private final CfEnv cfEnv;

    public DefaultCfEnvHolder() {
        this.cfEnv = CfEnvSingleton.getCfEnvInstance();
    }

    @Override
    public final void withCfEnv(Consumer<CfEnv> ifInCf) {
        ifInCf.accept(this.cfEnv);
    }
}
