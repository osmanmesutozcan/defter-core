package io.defter.core.app.query.group;

import io.defter.core.app.api.*;
import io.defter.core.app.api.ExpenseGroupCreated;
import io.defter.core.app.command.ExpenseGroup;
import io.defter.core.app.query.QueryConstants;
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

    friends.forEach(friend -> saveAffiliation(friend, event.getInvitedUserId()));
  }

  /**
   * Adds `user` as a friend to `friend` so now `friend` can see `user`s information.
   */
  private void saveAffiliation(String member, String friend) {
    // Here we cannot use class level entityManager because we want to catch the
    // unique constraint error.
    String id = UUID.randomUUID().toString();

    TypedQuery<Long> jpaQuery = entityManager
        .createNamedQuery("UserAffiliateView.exists", Long.class)
        .setParameter("userId", member)
        .setParameter("friendId", friend);

    if (jpaQuery.getSingleResult().intValue() > 0) {
      log.warn("skipped affiliate relation from {} to {}", member, friend);
      return;
    }

    entityManager.persist(new UserAffiliateView(id, member, friend));
  }

  @EventHandler
  public void on(SplitAddedToGroup event) {
    log.debug("projecting {}", event);

    // TODO: Maybe move this to a rate calculator.
    CurrencyExchangeRate rate = entityManager
        .createNamedQuery("CurrencyExchangeRate.getLatestBySymbol", CurrencyExchangeRate.class)
        .setParameter("symbol", event.getCurrency())
        .setMaxResults(1)
        .getSingleResult();

    ExpenseGroupView group = entityManager.find(ExpenseGroupView.class, event.getId());
    group.setBalance(group.getBalance() + event.getAmount());
    group.setNumberOfSplits(group.getNumberOfSplits() + 1);

    // TODO: Use client side generated id!.
    //  Id should be generated in mutation and pass via commands/events to keep this method idempotent.
    String id = UUID.randomUUID()
        .toString();

    entityManager.persist(
        new SplitView(
            id,
            event.getAmount(), event.getId(), event.getDescription(),
            event.getPayedBy(), event.getSubmittedBy(), event.getCreatedAt(),
            event.getCurrency(), rate.getRate(), event.getMembers()));
  }

  @QueryHandler
  public List<ExpenseGroupView> handle(FetchExpenseGroupViewsQuery query) {
    log.trace("handling {}", query);
    TypedQuery<ExpenseGroupView> jpaQuery = entityManager
        .createNamedQuery("ExpenseGroupView.fetch", ExpenseGroupView.class)
        .setParameter("idStartsWith", query.getFilter().getIdStartsWith())
        .setFirstResult(query.getOffset())
        .setMaxResults(query.getLimit());

    return log.exit(jpaQuery.getResultList());
  }

  @QueryHandler
  public ExpenseGroupView handle(FetchExpenseGroupViewQuery query) {
    log.trace("handling {}", query);
    ExpenseGroupView group = entityManager.find(ExpenseGroupView.class, query.getId());
    return log.exit(group);
  }

  @QueryHandler
  public List<SplitView> handle(FetchExpenseGroupsSplitsQuery query) {
    log.trace("handling {}", query);
    TypedQuery<SplitView> jpaQuery = entityManager
        .createNamedQuery("SplitView.fetch", SplitView.class)
        .setParameter("groupId", query.getGroupId());

    return log.exit(jpaQuery.getResultList());
  }

  @QueryHandler
  public CountExpenseGroupViewsResponse handle(CountExpenseGroupViewsQuery query) {
    log.trace("handling {}", query);
    TypedQuery<Long> jpaQuery = entityManager
        .createNamedQuery("ExpenseGroupView.count", Long.class)
        .setParameter("idStartsWith", query.getFilter().getIdStartsWith());

    return log.exit(new CountExpenseGroupViewsResponse(jpaQuery.getSingleResult().intValue(),
        Instant.now().toEpochMilli()));
  }
}
