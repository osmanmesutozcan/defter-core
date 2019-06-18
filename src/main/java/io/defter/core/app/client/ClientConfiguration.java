package io.defter.core.app.client;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("client")
public class ClientConfiguration {
    @Bean
    public Mutation mutation() {
        return new Mutation();
    }
}
