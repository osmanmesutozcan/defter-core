package io.defter.core.app.command;

import io.defter.core.app.api.*;
import lombok.extern.slf4j.XSlf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.context.annotation.Profile;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

@XSlf4j
@Aggregate
@Profile("command")
public class ExpenseGroup {

    @AggregateIdentifier
    private String id;
    private String name;
    private Currency currency;

    public ExpenseGroup() {
        //
    }

    @CommandHandler
    public ExpenseGroup(CreateExpenseGroup cmd) {
        log.debug("handling {}", cmd);
        apply(new ExpenseGroupCreated(cmd.getId(), cmd.getName(), cmd.getCurrency()));
    }

    @CommandHandler
    public void handle(AddSplitToGroup cmd) {
        log.debug("handling {}", cmd);
        apply(new SplitAddedToGroup(cmd.getId(), cmd.getAmount(), cmd.getPayedBy(), cmd.getDescription(), cmd.getSubmittedBy()));
    }

    @EventSourcingHandler
    public void on(ExpenseGroupCreated evt) {
        id = evt.getId();
        name = evt.getName();
        currency = evt.getCurrency();
    }
}
