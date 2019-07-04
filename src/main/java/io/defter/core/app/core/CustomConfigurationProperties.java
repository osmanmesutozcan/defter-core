package io.defter.core.app.core;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Data
@Configuration
@PropertySource("classpath:custom.properties")
@ConfigurationProperties(prefix = "core")
public class CustomConfigurationProperties {

  private String currency_rates_api_url;

}
