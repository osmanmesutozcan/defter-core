package io.defter.core.app.gui;

import io.defter.core.app.api.CountExpenseGroupViewsQuery;
import io.defter.core.app.api.CountExpenseGroupViewsResponse;

import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Profile("gui")
public class DefterAdminGuiConfiguration {

	@EventListener(ApplicationReadyEvent.class)
	public void helloHub(ApplicationReadyEvent event) {
		QueryGateway queryGateway = event.getApplicationContext().getBean(QueryGateway.class);
		queryGateway.query(new CountExpenseGroupViewsQuery(),
				ResponseTypes.instanceOf(CountExpenseGroupViewsResponse.class));
	}

}
