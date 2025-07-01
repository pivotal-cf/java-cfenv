package io.pivotal.cfenv.boot.genai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class GenAILocatorAutoConfiguration {

  @Bean
  public GenAILocator genAILocator(
          @Value("genai.locator.config-url") String configUrl,
          @Value("genai.locator.api-key") String apiKey,
          @Value("genai.locator.api-base") String apiBase
  ) {
    return new DefaultGenAILocator(configUrl, apiKey, apiBase);
  }
}
