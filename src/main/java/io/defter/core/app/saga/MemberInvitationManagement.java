package io.defter.core.app.saga;

import io.defter.core.app.api.AcceptMemberInvitation;
import io.defter.core.app.api.MemberInvitationAnswered;
import io.defter.core.app.api.MemberInvitationSent;
import io.defter.core.app.api.RejectMemberInvitation;
import java.util.UUID;
import javax.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.XSlf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.SagaLifecycle;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.spring.stereotype.Saga;

// Setup a deadline for this. if invitation is not
// replied within a certain time, we could ping the user again.
@Saga
@XSlf4j
@RequiredArgsConstructor
public class MemberInvitationManagement {

  private transient CommandGateway commandGateway;
  private transient EntityManager entityManager;
  private InvitationRequestState requestState = InvitationRequestState.NOT_SENT;

  // We will keep track of users info internally in saga so
  // we don't expose any user id to invitation process.
  private String invitedUserId;

  @StartSaga
  @SagaEventHandler(associationProperty = "invitationRequestId")
  public void handle(MemberInvitationSent event) {
    log.debug("saga handling {}", event);

    if (requestState.equals(InvitationRequestState.SENT)) {
      // This might be a retry attempt. We will ignore this
      return;
    }

    // send the email

    requestState = InvitationRequestState.SENT;
    String emailId = UUID.randomUUID().toString();
    SagaLifecycle.associateWith("emailId", emailId);

    this.invitedUserId = event.getInvitedUserId();

    log.debug("Sending an invitation email {} {}", emailId, event);
  }

  @SagaEventHandler(associationProperty = "emailId")
  public void handle(MemberInvitationAnswered event) {
    log.debug("Invitation answered {}", event);

    if (!requestState.equals(InvitationRequestState.SENT)) {
      // Not sure how to handle this...
      return;
    }

    if (event.getAnswer().equals(InvitationAnswer.ACCEPTED)) {
      commandGateway.send(
          new AcceptMemberInvitation(
              this.invitedUserId,
              event.getInvitationRequestId(),
              event.getEmailId()
          )
      );
    }

    if (event.getAnswer().equals(InvitationAnswer.REJECTED)) {
      commandGateway.send(
          new RejectMemberInvitation(
              this.invitedUserId,
              event.getInvitationRequestId(),
              event.getEmailId()
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
