package io.defter.core.app.command;

import io.defter.core.app.api.CreateUser;
import io.defter.core.app.api.UserCreated;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

@Aggregate
public class User {
    @AggregateIdentifier
    private String id;

    public User() { /* required by axon  */ }

    @CommandHandler
    public User(CreateUser command) {
        apply(new UserCreated(command.getId(), command.getUsername()));
    }

    @EventSourcingHandler
    public void on (UserCreated event) {
        this.id = event.getId();
    }
}
