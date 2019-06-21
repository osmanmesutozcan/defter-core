package io.defter.core.app.client;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import io.defter.core.app.api.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class Query implements GraphQLQueryResolver {

  private final QueryGateway queryGateway;

  public List<ExpenseGroupView> groups() {
    FetchExpenseGroupViewsQuery query = new FetchExpenseGroupViewsQuery(0, 10, new ExpenseGroupViewFilter(""));
    return queryGateway
        .query(query, ResponseTypes.multipleInstancesOf(ExpenseGroupView.class))
        .join();
  }

  public List<UserView> affiliations() {
    FetchUserAffiliatesQuery query = new FetchUserAffiliatesQuery("a1be090c-b221-4d69-bd1c-3a8ad7914941");
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
    FetchExpenseGroupSettlementQuery query = new FetchExpenseGroupSettlementQuery(
        "11871356-8a09-4f20-9ab6-f9a19ca219ed", groupId);

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
