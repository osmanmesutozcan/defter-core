package io.defter.core.app.query;

import io.defter.core.app.api.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.XSlf4j;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;

@XSlf4j
@Component
@Profile("query")
@RequiredArgsConstructor
@ProcessingGroup(QueryConstants.SETTLEMENT)
public class SettlementViewProjection {
    private final EntityManager entityManager;

//    @EventHandler
//    public void on(ExpenseGroupCreated event) {
//        //
//    }
//
//    @QueryHandler
//    public void handle(FetchExpenseGroupsSplitsQuery query) {
//        //
//    }
}
