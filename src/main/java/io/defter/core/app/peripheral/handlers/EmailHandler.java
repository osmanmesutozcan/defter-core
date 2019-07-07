package io.defter.core.app.peripheral.handlers;

import io.defter.core.app.api.PushNotificationDispatched;
import io.defter.core.app.peripheral.PeripheralConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.XSlf4j;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@XSlf4j
@Component
@Profile("peripheral")
@RequiredArgsConstructor
@ProcessingGroup(PeripheralConstants.NOTIFICATION_PROCESSOR)
public class EmailHandler {
  @EventHandler
  public void on(PushNotificationDispatched event) {
    log.debug("sending email {}", event);
  }
}
