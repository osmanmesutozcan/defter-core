package io.defter.core.app.command;

import lombok.RequiredArgsConstructor;
import org.axonframework.spring.eventhandling.scheduling.java.SimpleEventSchedulerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

@Component
@Profile("command")
@RequiredArgsConstructor
public class DefterCommandConfiguration {
  //
}
