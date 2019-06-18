package io.defter.core.app.client;

import com.coxautodev.graphql.tools.GraphQLResolver;
import io.defter.core.app.api.ExpenseGroupView;
import io.defter.core.app.api.FetchUserViewsByIds;
import io.defter.core.app.api.UserView;
import lombok.RequiredArgsConstructor;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GroupResolver implements GraphQLResolver<ExpenseGroupView> {
    private final QueryGateway queryGateway;

    public List<UserView> members(ExpenseGroupView group) {
        FetchUserViewsByIds query = new FetchUserViewsByIds(group.getMembers());
        return queryGateway
                .query(query, ResponseTypes.multipleInstancesOf(UserView.class))
                .join();
    }
}
