package io.defter.core.app.client;

import io.defter.core.app.saga.MemberInvitationManagement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class Peripheral {

  private final CommandGateway commandGateway;

  /**
   * Answer an invitation to the app... We will redirect the user to a signup page where they can either signup via web
   * or signup in the app.
   */
  @GetMapping("/invitation/reference")
  public void invitationAnswer(
      @RequestParam("iid") String affiliationRequestId,
      @RequestParam("eid") String emailId
  ) {
    //
  }

  @Data
  @NoArgsConstructor
  static public class InvitationAnswer {

    private MemberInvitationManagement.InvitationAnswer answer;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  static public class PeripheralAcknowledge {

    private Boolean status;
  }
}
