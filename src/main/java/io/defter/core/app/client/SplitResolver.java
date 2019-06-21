package io.defter.core.app.client;

import com.coxautodev.graphql.tools.GraphQLResolver;
import io.defter.core.app.api.FetchUserViewsByIds;
import io.defter.core.app.api.SplitView;
import io.defter.core.app.api.UserView;
import lombok.RequiredArgsConstructor;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SplitResolver implements GraphQLResolver<SplitView> {
    private final QueryGateway queryGateway;

    public UserView submittedBy(SplitView split) {
        List<String> member = Collections.singletonList(split.getSubmittedBy());
        FetchUserViewsByIds query = new FetchUserViewsByIds(member);
        return queryGateway
                .query(query, ResponseTypes.instanceOf(UserView.class))
                .join();
    }

    public UserView payedBy(SplitView split) {
        List<String> member = Collections.singletonList(split.getPayedBy());
        FetchUserViewsByIds query = new FetchUserViewsByIds(member);
        return queryGateway
                .query(query, ResponseTypes.instanceOf(UserView.class))
                .join();
    }
}
