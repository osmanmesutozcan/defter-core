package io.defter.core.app.saga;

import io.defter.core.app.api.AcceptExpenseGroupInvitation;
import io.defter.core.app.api.EmailDispatched;
import io.defter.core.app.api.ExpenseGroupInvitationAnswered;
import io.defter.core.app.api.PushNotificationDispatched;
import io.defter.core.app.api.RejectExpenseGroupInvitation;
import io.defter.core.app.api.SendExpenseGroupInvitation;
import io.defter.core.app.api.ExpenseGroupCreated;
import io.defter.core.app.api.ExpenseGroupMember;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.XSlf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.scheduling.java.SimpleEventScheduler;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.SagaLifecycle;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.spring.stereotype.Saga;

@Saga
@XSlf4j
@RequiredArgsConstructor
public class ExpenseGroupInvitationManagement {

  @Inject
  private transient CommandGateway commandGateway;

  @Inject
  private transient SimpleEventScheduler eventScheduler;

  private String groupId;
  private InvitationRequestState requestState = InvitationRequestState.NOT_SENT;
  private Map<String, String> invitedUsers = new HashMap<>();

  @StartSaga
  @SagaEventHandler(associationProperty = "id")
  public void handle(ExpenseGroupCreated event) {
    log.debug("saga handling {}", event);

    // This might be a double trigger. Just ignore it.
    if (requestState.equals(InvitationRequestState.SENT)) {
      return;
    }

    this.groupId = event.getId();

    event.getMembers()
        .stream()
        // TODO: Check who created the group and automatically accept group invitation for that user.
        //      Projection should also do something similar where it does not actually persist that invitation
        //      for users own create group invitations.
        //      ...
        //      That's a very leaky logic though. There must be an easier way
        .filter(member -> !member.getId().equals(event.getCreatedBy()))
        .forEach(member -> sendInvitation(member.getId(), event.getId(), member.getEmail()));

    requestState = InvitationRequestState.SENT;
  }

  private void sendInvitation(String member, String group, String email) {
    String invitationRequestId = groupId + ":" + member;
    SagaLifecycle.associateWith("invitationRequestId", invitationRequestId);
    this.invitedUsers.put(invitationRequestId, member);

    commandGateway.sendAndWait(new SendExpenseGroupInvitation(invitationRequestId, member, email, group));
    eventScheduler.schedule(Instant.now(), new PushNotificationDispatched(member, "title", "body"));
    eventScheduler.schedule(Instant.now(), new EmailDispatched(member, "title", "body"));
  }

  @SagaEventHandler(associationProperty = "invitationRequestId")
  public void handle(ExpenseGroupInvitationAnswered event) {
    log.debug("Invitation answered {}", event);

    if (!requestState.equals(InvitationRequestState.SENT)) {
      // Not sure how to handle this...
      return;
    }

    String invitationRequestId = event.getInvitationRequestId();
    String invitedUserId = this.invitedUsers.get(invitationRequestId);

    if (event.getAnswer().equals(InvitationAnswer.ACCEPTED)) {
      commandGateway.send(
          new AcceptExpenseGroupInvitation(
              this.groupId,
              invitedUserId,
              invitationRequestId
          )
      );
    }

    if (event.getAnswer().equals(InvitationAnswer.REJECTED)) {
      commandGateway.send(
          new RejectExpenseGroupInvitation(
              this.groupId,
              invitedUserId,
              invitationRequestId
          )
      );
    }

    SagaLifecycle.end();
  }

  public enum InvitationRequestState {
    NOT_SENT,
    SENT,
  }

  public enum InvitationAnswer {
    ACCEPTED,
    REJECTED,
  }
}
