/*
 * Copyright 2025 the original author or authors.
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
package io.pivotal.cfenv.boot.genai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

/**
 * Auto configuration for the genai locator.
 *
 * @author Gareth Evans
 **/
@AutoConfiguration
@ConditionalOnProperty("genai.locator.config-url")
public class GenaiLocatorAutoConfiguration {

  @Bean
  public GenaiLocator genaiLocator(
          RestClient.Builder builder,
          @Value("${genai.locator.config-url}") String configUrl,
          @Value("${genai.locator.api-key}") String apiKey,
          @Value("${genai.locator.api-base}") String apiBase
  ) {
    return new DefaultGenaiLocator(builder, configUrl, apiKey, apiBase);
  }
}
