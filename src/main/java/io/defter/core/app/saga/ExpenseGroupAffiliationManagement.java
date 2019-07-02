package io.defter.core.app.saga;

import io.defter.core.app.api.SendAffiliationRequest;
import io.defter.core.app.api.ExpenseGroupCreated;
import io.defter.core.app.api.ExpenseGroupMember;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.XSlf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.SagaLifecycle;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.spring.stereotype.Saga;

@Saga
@XSlf4j
@RequiredArgsConstructor
public class ExpenseGroupAffiliationManagement {

  @Inject
  private transient CommandGateway commandGateway;

  @StartSaga
  @SagaEventHandler(associationProperty = "id")
  public void handle(ExpenseGroupCreated event) {
    log.debug("saga handling {}", event);
    List<ExpenseGroupMember> members = event.getMembers();
    members.forEach(member -> {
      List<String> friends = members
          .stream()
          .filter(m -> !member.getId().equals(m.getId()))
          .map(ExpenseGroupMember::getId)
          .collect(Collectors.toList());

      friends.forEach(friend -> sendInvitation(member.getId(), friend));
    });

    // Because we will delegate tracking of each individual request to
    // another saga.
    SagaLifecycle.end();
  }

  private void sendInvitation(String member, String friend) {
    log.debug("{} {}", member, friend);
    String affiliationRequestId = member + ":" + friend;
    commandGateway.send(new SendAffiliationRequest(member, friend, affiliationRequestId));
  }
}
