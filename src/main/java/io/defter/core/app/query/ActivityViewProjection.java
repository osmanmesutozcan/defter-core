package io.defter.core.app.query;

import io.defter.core.app.api.ExpenseGroupCreated;
import io.defter.core.app.api.ExpenseGroupInvitationAccepted;
import io.defter.core.app.api.ExpenseGroupInvitationRejected;
import io.defter.core.app.api.SplitAddedToGroup;
import javax.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.XSlf4j;
import org.axonframework.config.ProcessingGroup;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@XSlf4j
@Component
@Profile("query")
@RequiredArgsConstructor
@ProcessingGroup(QueryConstants.ACTIVITY_PROCESSOR)
public class ActivityViewProjection {

  private final EntityManager entityManager;

  // {actor} accepted your invitation to {group name}
  // you joined {group name}
  public void on(ExpenseGroupInvitationAccepted event) {
    //
  }

  // {actor} rejected your invitation to {group name}
  public void on(ExpenseGroupInvitationRejected event) {
    //
  }

  // You created {group name}
  public void on(ExpenseGroupCreated event) {
    //
  }

  // {actor} added a new split(amount) to {group}
  public void on(SplitAddedToGroup event) {
    //
  }
}
