package io.defter.core.app.client;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import io.defter.core.app.api.ExpenseGroupView;
import io.defter.core.app.api.ExpenseGroupViewFilter;
import io.defter.core.app.api.FetchExpenseGroupViewsQuery;
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
}
