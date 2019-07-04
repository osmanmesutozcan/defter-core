package io.defter.core.app.client;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import io.defter.core.app.api.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.XSlf4j;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;

@XSlf4j
@Component
@RequiredArgsConstructor
public class Query implements GraphQLQueryResolver {

  private final QueryGateway queryGateway;

  public List<ExpenseGroupView> groups() {
    // TODO: Get groups of current user
    FetchExpenseGroupViewsQuery query = new FetchExpenseGroupViewsQuery(0, 50, new ExpenseGroupViewFilter(""));
    return queryGateway
        .query(query, ResponseTypes.multipleInstancesOf(ExpenseGroupView.class))
        .join();
  }

  public List<UserView> affiliations() {
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    String currentUserId = ((UserView) principal).getId();

    FetchUserAffiliatesQuery query = new FetchUserAffiliatesQuery(currentUserId);
    return queryGateway
        .query(query, ResponseTypes.multipleInstancesOf(UserView.class))
        .join();
  }

  public List<SplitView> splits(String groupId) {
    FetchExpenseGroupsSplitsQuery query = new FetchExpenseGroupsSplitsQuery(groupId);
    return queryGateway
        .query(query, ResponseTypes.multipleInstancesOf(SplitView.class))
        .join();
  }

  public SettlementResult settlement(String groupId) {
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    String currentUserId = ((UserView) principal).getId();

    FetchExpenseGroupSettlementQuery query = new FetchExpenseGroupSettlementQuery(
        currentUserId, groupId);

    SettlementView settlement = queryGateway
        .query(query, ResponseTypes.instanceOf(SettlementView.class))
        .join();

    return new SettlementResult(0, settlement.getBalances());
  }

  @Getter
  @Setter
  @AllArgsConstructor
  public class SettlementResult {

    public Integer totalSplits;
    public List<SettlementBalance> members;
  }
}
