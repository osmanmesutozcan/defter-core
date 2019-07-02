package io.defter.core.app.saga;

import io.defter.core.app.api.AcceptAffiliationRequest;
import io.defter.core.app.api.AffiliationRequestAnswered;
import io.defter.core.app.api.AffiliationRequestSent;
import io.defter.core.app.api.RejectAffiliationRequest;
import java.util.UUID;
import javax.persistence.EntityManager;
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
public class MemberAffiliationManagement {

  private transient CommandGateway commandGateway;
  private transient EntityManager entityManager;
  private AffiliationRequestState requestState = AffiliationRequestState.NOT_SENT;

  // We will keep track of users info internally in saga so
  // we don't expose any user id to invitation process.
  private String userId;
  private String affiliatingUserId;

  @StartSaga
  @SagaEventHandler(associationProperty = "affiliationRequestId")
  public void handle(AffiliationRequestSent event) {
    log.debug("saga handling {}", event);

    if (requestState.equals(AffiliationRequestState.SENT)) {
      // This might be a retry attempt. We will ignore this
      return;
    }

    // send the email

    requestState = AffiliationRequestState.SENT;
    String emailId = UUID.randomUUID().toString();
    SagaLifecycle.associateWith("emailId", emailId);

    this.userId = event.getUserId();
    this.affiliatingUserId = event.getAffiliatingUserId();

    log.debug("Sending an invitation email {} {}", emailId, event);
  }

  @SagaEventHandler(associationProperty = "emailId")
  public void handle(AffiliationRequestAnswered event) {
    log.debug("Invitation answered {}", event);

    if (!requestState.equals(AffiliationRequestState.SENT)) {
      // Not sure how to handle this...
      return;
    }

    if (event.getAnswer().equals(AffiliationAnswer.ACCEPTED)) {
      commandGateway.send(
          new AcceptAffiliationRequest(
              this.userId,
              this.affiliatingUserId,
              event.getAffiliationRequestId(),
              event.getEmailId()
          )
      );
    }

    if (event.getAnswer().equals(AffiliationAnswer.REJECTED)) {
      commandGateway.send(
          new RejectAffiliationRequest(
              this.userId,
              this.affiliatingUserId,
              event.getAffiliationRequestId(),
              event.getEmailId()
          )
      );
    }

    SagaLifecycle.end();
  }

  public enum AffiliationRequestState {
    NOT_SENT,
    SENT,
  }

  public enum AffiliationAnswer {
    ACCEPTED,
    REJECTED,
  }
}
