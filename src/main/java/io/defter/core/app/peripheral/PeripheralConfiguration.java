package io.defter.core.app.peripheral;

import lombok.RequiredArgsConstructor;
import org.axonframework.spring.eventhandling.scheduling.java.SimpleEventSchedulerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

@Component
@Profile("peripheral")
@EnableScheduling
@RequiredArgsConstructor
public class PeripheralConfiguration {

  @Bean
  public SimpleEventSchedulerFactoryBean simpleEventSchedulerFactoryBean() {
    return new SimpleEventSchedulerFactoryBean();
  }
}
