package io.defter.core.app.command;

import io.defter.core.app.api.*;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;

import java.lang.invoke.MethodHandles;
import java.util.List;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

@Aggregate
@Profile("command")
public class ExpenseGroup {

    private final static Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @AggregateIdentifier
    private String id;
    private String name;
    private String currency;
    private List<String> members;

    public ExpenseGroup() {
        //
    }

    @CommandHandler
    public ExpenseGroup(CreateExpenseGroup cmd) {
        log.debug("handling {}", cmd);
        if (cmd.getMembers().size() <= 0) {
            throw new IllegalArgumentException("members < 2");
        }
        apply(new ExpenseGroupCreated(cmd.getId(), cmd.getName(), cmd.getCurrency(), cmd.getMembers()));
    }

    @EventSourcingHandler
    public void on(ExpenseGroupCreated evt) {
        id = evt.getId();
        name = evt.getName();
        currency = evt.getCurrency();
        members = evt.getMembers();
    }
}
