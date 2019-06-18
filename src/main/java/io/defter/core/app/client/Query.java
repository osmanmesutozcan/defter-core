package io.defter.core.app.client;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import io.defter.core.app.api.*;
import lombok.RequiredArgsConstructor;
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
        FetchUserAffiliatesQuery query = new FetchUserAffiliatesQuery("8a02f8ef-eab4-47a3-98b1-0c05d26af046");
        return queryGateway
                .query(query, ResponseTypes.multipleInstancesOf(UserView.class))
                .join();
    }
}
