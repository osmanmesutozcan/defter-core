package io.defter.core.app.client;

import io.defter.core.app.api.ScheduledElapsed;
import io.defter.core.app.api.ScheduledEventTypes;
import io.defter.core.app.saga.MemberInvitationManagement;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.scheduling.java.SimpleEventScheduler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class Peripheral {

  private final CommandGateway commandGateway;
  private final SimpleEventScheduler eventScheduler;

  /**
   * Shared group link.
   * When a user opens this, they will login be redirected to a read-only
   * expense group page. Preferably a real-time page.
   */
  @GetMapping("/p/shared/group")
  public void sharedGroup(
      @RequestParam("gid") String groupId,
      @RequestParam("astr") String authenticationString
  ) {
    //
  }

  /**
   * Answer an invitation to the app... We will redirect the user to a signup page where they can either signup via web
   * or signup in the app.
   */
  @GetMapping("/p/invitation/reference")
  public void invitationAnswer(
      @RequestParam("iid") String invitationRequestId,
      @RequestParam("eid") String emailId
  ) {
    //
  }

  @GetMapping("/p/scheduled/exrates")
  public void updateExchangeRates() {
    eventScheduler.schedule(
        Instant.now(),
        new ScheduledElapsed(
            "Currency exchange should update",
            ScheduledEventTypes.EXCHANGE_RATES_UPDATES
        )
    );
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
