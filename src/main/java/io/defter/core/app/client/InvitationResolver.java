package io.defter.core.app.client;

import com.coxautodev.graphql.tools.GraphQLResolver;
import io.defter.core.app.api.Currency;
import io.defter.core.app.api.ExpenseGroupInvitationView;
import io.defter.core.app.api.ExpenseGroupView;
import io.defter.core.app.api.FetchExpenseGroupViewQuery;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InvitationResolver implements GraphQLResolver<ExpenseGroupInvitationView> {

  private final QueryGateway queryGateway;

  public InvitationGroupPreview group(ExpenseGroupInvitationView invitation) {
    ExpenseGroupView group = queryGateway
        .query(
            new FetchExpenseGroupViewQuery(invitation.getExpenseGroupId()),
            ResponseTypes.instanceOf(ExpenseGroupView.class))
        .join();

    return new InvitationGroupPreview(group.getName(), group.getCurrency());
  }

  @Data
  @AllArgsConstructor
  public class InvitationGroupPreview {

    public String name;
    public Currency currency;
  }
}
