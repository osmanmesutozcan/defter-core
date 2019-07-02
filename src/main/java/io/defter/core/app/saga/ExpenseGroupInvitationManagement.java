package io.defter.core.app.saga;

import io.defter.core.app.api.AcceptExpenseGroupInvitation;
import io.defter.core.app.api.ExpenseGroupInvitationAnswered;
import io.defter.core.app.api.RejectExpenseGroupInvitation;
import io.defter.core.app.api.SendExpenseGroupInvitation;
import io.defter.core.app.api.ExpenseGroupCreated;
import io.defter.core.app.api.ExpenseGroupMember;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public class ExpenseGroupInvitationManagement {

  @Inject
  private transient CommandGateway commandGateway;

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

    groupId = event.getId();

    List<ExpenseGroupMember> members = event.getMembers();
    // TODO: Check who created the group and automatically accept group invitation for that user.
    members.forEach(member -> sendInvitation(member.getId(), event.getId()));
    requestState = InvitationRequestState.SENT;
  }

  private void sendInvitation(String member, String group) {
    String invitationRequestId = groupId + ":" + member;
    SagaLifecycle.associateWith("invitationRequestId", invitationRequestId);
    this.invitedUsers.put(invitationRequestId, member);

    commandGateway.send(new SendExpenseGroupInvitation(invitationRequestId, member, group));
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
