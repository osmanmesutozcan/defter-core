package io.defter.core.app.command;

import io.defter.core.app.api.AcceptMemberInvitation;
import io.defter.core.app.api.MemberInvitationAccepted;
import io.defter.core.app.api.MemberInvitationRejected;
import io.defter.core.app.api.CreateUser;
import io.defter.core.app.api.RejectMemberInvitation;
import io.defter.core.app.api.UserCreated;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.XSlf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

@XSlf4j
@Aggregate
@NoArgsConstructor
public class User {

  @AggregateIdentifier
  private String id;
  private String email;

  @CommandHandler
  public User(CreateUser command) {
    log.debug("handling {}", command);
    // TODO: We should hash this password here.
    apply(new UserCreated(command.getId(), command.getName(), command.getEmail(), command.getPassword()));
  }

  @CommandHandler
  public void handle(AcceptMemberInvitation command) {
    log.debug("handling {}", command);
    apply(
        new MemberInvitationAccepted(
            command.getInvitedUserId(),
            command.getEmailId(),
            command.getInvitationRequestId()
        )
    );
  }

  @CommandHandler
  public void handle(RejectMemberInvitation command) {
    log.debug("handling {}", command);
    apply(
        new MemberInvitationRejected(
            command.getInvitedUserId(),
            command.getEmailId(),
            command.getInvitationRequestId()
        )
    );
  }

  @EventSourcingHandler
  public void on(UserCreated event) {
    this.id = event.getId();
    this.email = event.getEmail();
  }
}
