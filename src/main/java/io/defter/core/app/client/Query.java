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
  private final String currentUserId =  "6efa72f0-ca93-4484-9c1d-5785dc0aba7a";

  public List<ExpenseGroupView> groups() {
    FetchExpenseGroupViewsQuery query = new FetchExpenseGroupViewsQuery(0, 10, new ExpenseGroupViewFilter(""));
    return queryGateway
        .query(query, ResponseTypes.multipleInstancesOf(ExpenseGroupView.class))
        .join();
  }

  public List<UserView> affiliations() {
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
