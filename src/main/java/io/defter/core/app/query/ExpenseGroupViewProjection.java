package io.defter.core.app.query;

import io.defter.core.app.api.*;
import io.defter.core.app.api.ExpenseGroupCreated;
import java.util.Date;
import java.util.stream.Collectors;
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
import java.util.UUID;

@XSlf4j
@Component
@Profile("query")
@RequiredArgsConstructor
@ProcessingGroup(QueryConstants.EXPENSE_GROUP_PROCESSOR)
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
        event.getCreatedBy(),
        new Date(),
        new ArrayList<>()
    ));
  }

  @EventHandler
  public void on(MemberAddedToGroup event) {
    log.debug("projecting {}", event);
    ExpenseGroupView group = entityManager.find(ExpenseGroupView.class, event.getId());
    List<ExpenseGroupMember> members = group.getMembers();
    members.add(event.getMember());
    group.setMembers(members);
  }

  @EventHandler
  public void on(ExpenseGroupInvitationAccepted event) {
    log.debug("projecting {}", event);
    ExpenseGroupView group = entityManager.find(ExpenseGroupView.class, event.getGroupId());

    List<String> friends = group
        .getMembers()
        .stream()
        .map(ExpenseGroupMember::getId)
        .filter(m -> !m.equals(event.getInvitedUserId()))
        .collect(Collectors.toList());

    friends.forEach(friend -> saveAffiliation(event.getInvitedUserId(), friend));
  }

  private void saveAffiliation(String member, String friend) {
    // Here we cannot use class level entityManager because we want to catch the
    // unique contraint error.
    String id = UUID.randomUUID().toString();

    TypedQuery<Long> jpaQuery = entityManager
        .createNamedQuery("UserAffiliateView.exists", Long.class);
    jpaQuery.setParameter("userId", member);
    jpaQuery.setParameter("friendId", friend);

    if (jpaQuery.getSingleResult().intValue() > 0) {
      log.warn("skipped affiliate relation from {} to {}", member, friend);
      return;
    }

    entityManager.persist(new UserAffiliateView(id, member, friend));
  }

  @EventHandler
  public void on(SplitAddedToGroup event) {
    log.debug("projecting {}", event);
    ExpenseGroupView group = entityManager.find(ExpenseGroupView.class, event.getId());
    group.setBalance(group.getBalance() + event.getAmount());
    group.setNumberOfSplits(group.getNumberOfSplits() + 1);

    // TODO: Use client side generated id!.
    //  Id should be generated in mutation and pass via commands/events to keep this method idempotent.
    String id = UUID.randomUUID()
        .toString();

    entityManager.persist(
        new SplitView(id, event.getAmount(), event.getId(), event.getDescription(),
            event.getPayedBy(), event.getSubmittedBy(), event.getCreatedAt(), event.getMembers()));
  }

  @QueryHandler
  public List<ExpenseGroupView> handle(FetchExpenseGroupViewsQuery query) {
    log.trace("handling {}", query);
    TypedQuery<ExpenseGroupView> jpaQuery = entityManager
        .createNamedQuery("ExpenseGroupView.fetch", ExpenseGroupView.class);
    jpaQuery.setParameter("idStartsWith", query.getFilter().getIdStartsWith());
    jpaQuery.setFirstResult(query.getOffset());
    jpaQuery.setMaxResults(query.getLimit());
    return log.exit(jpaQuery.getResultList());
  }

  @QueryHandler
  public List<SplitView> handle(FetchExpenseGroupsSplitsQuery query) {
    log.trace("handling {}", query);
    TypedQuery<SplitView> jpaQuery = entityManager
        .createNamedQuery("SplitView.fetch", SplitView.class);
    jpaQuery.setParameter("groupId", query.getGroupId());
    return log.exit(jpaQuery.getResultList());
  }

  @QueryHandler
  public CountExpenseGroupViewsResponse handle(CountExpenseGroupViewsQuery query) {
    log.trace("handling {}", query);
    TypedQuery<Long> jpaQuery = entityManager
        .createNamedQuery("ExpenseGroupView.count", Long.class);
    jpaQuery.setParameter("idStartsWith", query.getFilter().getIdStartsWith());
    return log.exit(new CountExpenseGroupViewsResponse(jpaQuery.getSingleResult().intValue(),
        Instant.now().toEpochMilli()));
  }
}
