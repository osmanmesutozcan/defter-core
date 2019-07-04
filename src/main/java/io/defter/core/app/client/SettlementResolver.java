package io.defter.core.app.client;

import com.coxautodev.graphql.tools.GraphQLResolver;
import io.defter.core.app.client.Query.SettlementResult;
import lombok.RequiredArgsConstructor;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SettlementResolver implements GraphQLResolver<SettlementResult> {
    private final QueryGateway queryGateway;

    public Integer totalSplits(SettlementResult settlement) {
      return 42;
    }
}
