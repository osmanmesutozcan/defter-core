package io.defter.core.app.peripheral;

import io.defter.core.app.api.ScheduledElapsed;
import io.defter.core.app.api.ScheduledEventTypes;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.XSlf4j;
import org.axonframework.eventhandling.scheduling.java.SimpleEventScheduler;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Dispatch batch jobs.
 */
@XSlf4j
@Component
@Profile("command")
@RequiredArgsConstructor
public class PeripheralEventDispatchers {

  private final SimpleEventScheduler eventScheduler;

  @Scheduled(cron = "0 0 * * * *")
  @Scheduled(cron = "0 12 * * * *")
  public void triggerSyncExchangeRates() {
    eventScheduler.schedule(
        Instant.now(),
        new ScheduledElapsed(
            "Currency exchange should update",
            ScheduledEventTypes.EXCHANGE_RATES_UPDATES
        )
    );
  }
}
