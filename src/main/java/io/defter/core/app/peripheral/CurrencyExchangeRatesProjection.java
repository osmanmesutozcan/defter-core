package io.defter.core.app.peripheral;

import io.defter.core.app.api.ScheduledElapsed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.XSlf4j;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Responsible of maintaining a currency exchange rates table.
 */
@XSlf4j
@Component
@Profile("query")
@RequiredArgsConstructor
@ProcessingGroup(PeripheralConstants.EXCHANGE_RATES_PROCESSOR)
public class CurrencyExchangeRatesProjection {

  @EventHandler
  private void on(ScheduledElapsed event) {
    log.debug("{}", event);
  }
}
