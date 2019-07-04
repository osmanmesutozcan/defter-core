package io.defter.core.app.query;

import io.defter.core.app.api.*;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.persistence.TypedQuery;
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

  @EventHandler
  public void on(ExpenseGroupCreated event) {
    log.debug("projecting {}", event);

    List<SettlementView> settlements = event.getMembers()
        .stream()
        .map(member -> createSettlementViewForMember(event.getMembers(), member, event.getId()))
        .collect(Collectors.toList());

    settlements.forEach(entityManager::persist);
  }

  private SettlementView createSettlementViewForMember(List<ExpenseGroupMember> members,
      ExpenseGroupMember currentMember, String groupId) {
    List<SettlementBalance> balances = members
        .stream()
        .filter(m -> !m.getId().equals(currentMember.getId()))
        .map(m -> new SettlementBalance(m.getId(), 0))
        .collect(Collectors.toList());

    String id = UUID.randomUUID().toString();
    return new SettlementView(id, currentMember.getId(), groupId, new Date(), balances);
  }

  @EventHandler
  public void on(SplitAddedToGroup event) {
    log.debug("projecting {}", event);
    TypedQuery<SettlementView> settlementsQuery = entityManager
        .createNamedQuery("SettlementView.fetch", SettlementView.class);
    settlementsQuery.setParameter("groupId", event.getId());

    Map<String, SplitMember> members = event.getMembers()
        .stream()
        .collect(Collectors.toMap(SplitMember::getId, c -> c));

    settlementsQuery.getResultList()
        .forEach(settlement -> {
          List<SettlementBalance> balances = getUpdatedBalancesForView(event, settlement, members, event.getPayedBy());
          settlement.setBalances(balances);
        });
  }

  private List<SettlementBalance> getUpdatedBalancesForView(SplitAddedToGroup event, SettlementView settlement,
      Map<String, SplitMember> members, String payedBy) {

    return settlement.getBalances()
        .stream()
        .map(balance -> updateSettlementBalanceForView(balance, event, settlement, members, payedBy))
        .collect(Collectors.toList());
  }

  private SettlementBalance updateSettlementBalanceForView(SettlementBalance balance, SplitAddedToGroup event,
      SettlementView settlement, Map<String, SplitMember> members, String payedBy) {

    // if this is paying users settlement record
    // add payed amount to each users account on my book
    if (payedBy.equals(settlement.getUserId())) {
      SplitMember member = members.get(settlement.getUserId());
      double amount = event.getAmount() * (member.getShare() / 100);
      balance.setBalance(balance.getBalance() - amount);
    }

    // if this is other users settlement record
    // write debt to paying users account on other users book
    if (payedBy.equals(balance.getUserId())) {
      SplitMember member = members.get(balance.getUserId());
      double amount = event.getAmount() * (member.getShare() / 100);
      balance.setBalance(balance.getBalance() + amount);
    }

    return balance;
  }

  @QueryHandler
  public SettlementView handle(FetchExpenseGroupSettlementQuery query) {
    log.trace("handling {}", query);
    TypedQuery<SettlementView> jpaQuery = entityManager
        .createNamedQuery("SettlementView.fetchForUser", SettlementView.class);
    jpaQuery.setParameter("userId", query.getUserId());
    jpaQuery.setParameter("groupId", query.getGroupId());
    return log.exit(jpaQuery.getSingleResult());
  }
}
