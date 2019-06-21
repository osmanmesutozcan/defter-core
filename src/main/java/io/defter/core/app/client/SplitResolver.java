package io.defter.core.app.client;

import com.coxautodev.graphql.tools.GraphQLResolver;
import io.defter.core.app.api.FetchUserViewById;
import io.defter.core.app.api.SplitView;
import io.defter.core.app.api.UserView;
import lombok.RequiredArgsConstructor;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SplitResolver implements GraphQLResolver<SplitView> {
    private final QueryGateway queryGateway;

    public UserView submittedBy(SplitView split) {
        FetchUserViewById query = new FetchUserViewById(split.getSubmittedBy());
        return queryGateway
                .query(query, ResponseTypes.instanceOf(UserView.class))
                .join();
    }

    public UserView payedBy(SplitView split) {
        FetchUserViewById query = new FetchUserViewById(split.getPayedBy());
        return queryGateway
                .query(query, ResponseTypes.instanceOf(UserView.class))
                .join();
    }
}
