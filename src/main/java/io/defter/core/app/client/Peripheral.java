package io.defter.core.app.client;

import io.defter.core.app.api.AffiliationRequestAnswered;
import io.defter.core.app.api.AnswerAffiliationRequest;
import io.defter.core.app.saga.MemberAffiliationManagement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class Peripheral {

  private final CommandGateway commandGateway;

  @PostMapping("/invitation/answer")
  public PeripheralAcknowledge invitationAnswer(
      @RequestParam("iid") String affiliationRequestId,
      @RequestParam("eid") String emailId,
      @RequestBody InvitationAnswer answer
  ) {
    commandGateway.sendAndWait(
        new AnswerAffiliationRequest(
            affiliationRequestId,
            emailId,
            answer.answer
        )
    );
    return new PeripheralAcknowledge(true);
  }

  @Data
  private class InvitationAnswer {
    private final MemberAffiliationManagement.AffiliationAnswer answer;
  }

  @Data
  @AllArgsConstructor
  private class PeripheralAcknowledge {
    private final Boolean status;
  }
}
