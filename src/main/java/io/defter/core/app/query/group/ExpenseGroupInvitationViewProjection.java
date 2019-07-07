package io.defter.core.app.query.group;

import io.defter.core.app.api.ExpenseGroupInvitationAccepted;
import io.defter.core.app.api.ExpenseGroupInvitationRejected;
import io.defter.core.app.api.ExpenseGroupInvitationSent;
import io.defter.core.app.api.ExpenseGroupInvitationView;
import io.defter.core.app.api.FetchInvitationsOfUser;
import io.defter.core.app.query.QueryConstants;
import io.defter.core.app.saga.ExpenseGroupInvitationManagement.InvitationAnswer;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.XSlf4j;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@XSlf4j
@Component
@Profile("query")
@RequiredArgsConstructor
@ProcessingGroup(QueryConstants.EXPENSE_GROUP_PROCESSOR)
public class ExpenseGroupInvitationViewProjection {

  private final EntityManager entityManager;

  @EventHandler
  public void on(ExpenseGroupInvitationSent event) {
    log.debug("projecting {}", event);
    entityManager.persist(new ExpenseGroupInvitationView(
        event.getInvitationRequestId(),
        event.getInvitedUserId(),
        event.getGroupId(),
        null,
        new Date()
    ));
  }

  @EventHandler
  public void on(ExpenseGroupInvitationAccepted event) {
    log.debug("projecting {}", event);
    ExpenseGroupInvitationView view = entityManager.find(ExpenseGroupInvitationView.class, event.getInvitationRequestId());
    view.setAnswer(InvitationAnswer.ACCEPTED);
  }

  @EventHandler
  public void on(ExpenseGroupInvitationRejected event) {
    log.debug("projecting {}", event);
    ExpenseGroupInvitationView view = entityManager.find(ExpenseGroupInvitationView.class, event.getInvitationRequestId());
    view.setAnswer(InvitationAnswer.REJECTED);
  }

  @QueryHandler
  public List<ExpenseGroupInvitationView> handle(FetchInvitationsOfUser query) {
    log.trace("handling {}", query);
    TypedQuery<ExpenseGroupInvitationView> invitationsQuery = entityManager
        .createNamedQuery("ExpenseGroupInvitationView.fetch", ExpenseGroupInvitationView.class)
        .setParameter("userId", query.getUserId());

    return log.exit(invitationsQuery.getResultList());
  }
}
