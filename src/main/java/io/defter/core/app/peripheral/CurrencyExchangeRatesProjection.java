package io.defter.core.app.peripheral;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.defter.core.app.api.CurrencyExchangeRate;
import io.defter.core.app.api.ScheduledElapsed;
import io.defter.core.app.core.CustomConfigurationProperties;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.XSlf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
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

  private final EntityManager entityManager;
  private final CustomConfigurationProperties config;
  private final OkHttpClient okHttpClient = new OkHttpClient();

  @Transactional
  @EventHandler
  public void on(ScheduledElapsed event) {
    log.debug("handling {}", event);
    ExchangeRateResponse rates = getLatestExchangeRates();
    rates.getRates().forEach(this::saveExchangeRate);
    log.debug("Finished updating exchange rates");
  }

  private ExchangeRateResponse getLatestExchangeRates() {
    log.debug("pre stuff");
    Request request = new Request.Builder()
        .url(config.getCurrency_rates_api_url())
        .header("Accept", "application/json")
        .build();

    try {
      Response response = okHttpClient.newCall(request).execute();
      if (response.body() != null) {
        return new Gson().fromJson(
            response.body().string(),
            new TypeToken<ExchangeRateResponse>() {
            }.getType()
        );
      }
    } catch (IOException exception) {
      log.warn("failed to update exchange rates");
    }

    // Default empty if errored.
    return new ExchangeRateResponse();
  }

  private Double getProperty(JSONObject obj, String property) {
    log.debug("{} {}", obj, property);
    try {
      return obj.getDouble(property);
    } catch (JSONException exception) {
      return .0;
    }
  }

  private void saveExchangeRate(String symbol, Double rate) {
    entityManager.persist(new CurrencyExchangeRate(
        UUID.randomUUID().toString(),
        symbol,
        rate,
        new Date()
    ));
  }

  @Data
  private class ExchangeRateResponse {

    private String base;
    private String date;
    private HashMap<String, Double> rates;
  }
}
