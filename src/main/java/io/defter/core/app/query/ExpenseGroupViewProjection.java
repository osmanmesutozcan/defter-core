package io.defter.core.app.query;

import io.defter.core.app.api.*;
import io.defter.core.app.api.ExpenseGroupCreated;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.XSlf4j;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
@XSlf4j
@RequiredArgsConstructor
@Profile("query")
@ProcessingGroup("query")
public class ExpenseGroupViewProjection {
    private final EntityManager entityManager;

    @EventHandler
    public void on(ExpenseGroupCreated event) {
        log.debug("projecting {}", event);
        entityManager.persist(new ExpenseGroupView(
                event.getId(),
                event.getName(),
                event.getCurrency(),
                .0,
                0,
                new ArrayList<>()
        ));
    }

    @EventHandler
    public void on(MemberAddedToGroup event) {
        log.debug("projecting {}", event);
        ExpenseGroupView group = entityManager.find(ExpenseGroupView.class, event.getId());
        List<String> members = group.getMembers();
        members.add(event.getMemberId());
        group.setMembers(members);
    }

    @EventHandler
    public void on(SplitAddedToGroup event) {
        log.debug("projecting {}", event);
        ExpenseGroupView group = entityManager.find(ExpenseGroupView.class, event.getId());
        group.setBalance(group.getBalance() + event.getAmount());
        group.setNumberOfSplits(group.getNumberOfSplits() + 1);
    }

    @QueryHandler
    public List<ExpenseGroupView> handle(FetchExpenseGroupViewsQuery query) {
        log.trace("handling {}", query);
        TypedQuery<ExpenseGroupView> jpaQuery = entityManager.createNamedQuery("ExpenseGroupView.fetch", ExpenseGroupView.class);
        jpaQuery.setParameter("idStartsWith", query.getFilter().getIdStartsWith());
        jpaQuery.setFirstResult(query.getOffset());
        jpaQuery.setMaxResults(query.getLimit());
        return log.exit(jpaQuery.getResultList());
    }

    @QueryHandler
    public CountExpenseGroupViewsResponse handle(CountExpenseGroupViewsQuery query) {
        log.trace("handling {}", query);
        TypedQuery<Long> jpaQuery = entityManager.createNamedQuery("ExpenseGroupView.count", Long.class);
        jpaQuery.setParameter("idStartsWith", query.getFilter().getIdStartsWith());
        return log.exit(new CountExpenseGroupViewsResponse(jpaQuery.getSingleResult().intValue(), Instant.now().toEpochMilli()));
    }
}