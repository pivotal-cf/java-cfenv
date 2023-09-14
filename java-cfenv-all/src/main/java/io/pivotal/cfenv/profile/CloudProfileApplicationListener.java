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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.boot.logging.DeferredLog;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;

/**
 * Originally contributed by Dylan Roberts in
 * https://github.com/cloudfoundry/java-buildpack-auto-reconfiguration/pull/76/files
 */
public final class CloudProfileApplicationListener implements ApplicationListener<ApplicationEvent>, Ordered {

    private Log logger = new DeferredLog();
    public static final String CLOUD_PROFILE = "cloud";
    private final CfEnvHolder cfEnvHolder;

    public CloudProfileApplicationListener() {
        this.cfEnvHolder = new DefaultCfEnvHolder();
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 3;  // Before Boot properties file load to enable support for `application-cloud.properties`
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ApplicationEnvironmentPreparedEvent e) {
            ProfileUtils.activateProfile(CLOUD_PROFILE, e.getEnvironment(), this.cfEnvHolder);
            logger.info(String.format("'%s' profile activated", CLOUD_PROFILE));
        } else if (event instanceof ApplicationPreparedEvent) {
            DeferredLog.replay(logger, LogFactory.getLog(getClass()));
        }
    }

}
