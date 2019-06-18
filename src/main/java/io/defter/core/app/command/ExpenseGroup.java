package io.defter.core.app.command;

import io.defter.core.app.api.*;
import lombok.extern.slf4j.XSlf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.context.annotation.Profile;

import java.util.stream.Collectors;

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
    public ExpenseGroup(CreateExpenseGroup command) {
        log.debug("handling {}", command);
        apply(new ExpenseGroupCreated(command.getId(), command.getName(), command.getCurrency(), command.getMembers()));
        command.getMembers().forEach(member -> apply(new MemberAddedToGroup(command.getId(), member)));
    }

    @CommandHandler
    public void handle(AddSplitToGroup cmd) {
        log.debug("handling {}", cmd);
        apply(new SplitAddedToGroup(cmd.getId(), cmd.getAmount(), cmd.getPayedBy(), cmd.getDescription(), cmd.getSubmittedBy()));
    }

    @EventSourcingHandler
    public void on(ExpenseGroupCreated event) {
        id = event.getId();
        name = event.getName();
        currency = event.getCurrency();
    }
}
