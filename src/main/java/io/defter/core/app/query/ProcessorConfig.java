package io.defter.core.app.query;

import lombok.extern.slf4j.XSlf4j;
import org.axonframework.config.EventProcessingConfigurer;
import org.axonframework.eventhandling.TrackingEventProcessorConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@XSlf4j
@Configuration
public class ProcessorConfig {

  @Autowired
  public void config(EventProcessingConfigurer configurer) {
    configurer.registerTrackingEventProcessorConfiguration(
        c -> TrackingEventProcessorConfiguration.forParallelProcessing(2)
    );
  }
}
