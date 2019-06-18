package io.defter.core.app.gui;

import com.vaadin.flow.data.provider.AbstractBackEndDataProvider;
import com.vaadin.flow.data.provider.DataChangeEvent;
import com.vaadin.flow.data.provider.Query;
import io.defter.core.app.api.*;
import lombok.*;
import lombok.extern.slf4j.XSlf4j;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.queryhandling.SubscriptionQueryResult;

import java.util.List;
import java.util.stream.Stream;

@XSlf4j
@RequiredArgsConstructor
public class ExpenseGroupSummaryDataProvider extends AbstractBackEndDataProvider<ExpenseGroupView, Void> {

    private final QueryGateway queryGateway;

    /**
     * We need to keep track of our current subscriptions. To avoid subscriptions being modified while
     * we are processing query updates, the methods on these class are synchronized.
     */

    private SubscriptionQueryResult<List<ExpenseGroupView>, ExpenseGroupView> fetchQueryResult;

    @Getter
    @Setter
    @NonNull
    private ExpenseGroupViewFilter filter = new ExpenseGroupViewFilter("");

    @Override
    @Synchronized
    protected Stream<ExpenseGroupView> fetchFromBackEnd(Query<ExpenseGroupView, Void> query) {
        /*
         * If we are already doing a query (and are subscribed to it), cancel are subscription
         * and forget about the query.
         */
        if (fetchQueryResult != null) {
            fetchQueryResult.cancel();
            fetchQueryResult = null;
        }
        FetchExpenseGroupViewsQuery fetchExpenseGroupViewsQuery =
                new FetchExpenseGroupViewsQuery(query.getOffset(), query.getLimit(), filter);
        log.trace("submitting {}", fetchExpenseGroupViewsQuery);
        /*
         * Submitting our query as a subscriptionquery, specifying both the initially expected
         * response type (multiple CardSummaries) as wel as the expected type of the updates
         * (single CardSummary object). The result is a SubscriptionQueryResult which contains
         * a project reactor Mono for the initial response, and a Flux for the updates.
         */
        fetchQueryResult = queryGateway.subscriptionQuery(
                fetchExpenseGroupViewsQuery,
                ResponseTypes.multipleInstancesOf(ExpenseGroupView.class),
                ResponseTypes.instanceOf(ExpenseGroupView.class));
        /*
         * Subscribing to the updates before we get the initial results.
         */
        fetchQueryResult
                .updates()
                .subscribe(expenseGroupView -> {
                    log.trace("processing query update for {}: {}", fetchExpenseGroupViewsQuery, expenseGroupView);
                    /* This is a Vaadin-specific call to update the UI as a result of data changes. */
                    fireEvent(new DataChangeEvent.DataRefreshEvent<>(this, expenseGroupView));
                });
        /*
         * Returning the initial result.
         */
        return fetchQueryResult.initialResult().block().stream();
    }

    @Override
    @Synchronized
    protected int sizeInBackEnd(Query<ExpenseGroupView, Void> query) {
        CountExpenseGroupViewsQuery countExpenseGroupViewsQuery = new CountExpenseGroupViewsQuery(filter);
        log.trace("submitting {}", countExpenseGroupViewsQuery);
        return queryGateway
                .query(countExpenseGroupViewsQuery, ResponseTypes.instanceOf(CountExpenseGroupViewsResponse.class))
                .exceptionally(exception -> new CountExpenseGroupViewsResponse(0, 0))
                .join().getCount();
    }

    @Synchronized
    void shutDown() {
        if (fetchQueryResult != null) {
            fetchQueryResult.cancel();
            fetchQueryResult = null;
        }
    }
}
