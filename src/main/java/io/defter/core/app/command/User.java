package io.defter.core.app.command;

import io.defter.core.app.api.AcceptAffiliationRequest;
import io.defter.core.app.api.AffiliationRequestAccepted;
import io.defter.core.app.api.AffiliationRequestRejected;
import io.defter.core.app.api.AffiliationRequestSent;
import io.defter.core.app.api.CreateUser;
import io.defter.core.app.api.RejectAffiliationRequest;
import io.defter.core.app.api.SendAffiliationRequest;
import io.defter.core.app.api.UserCreated;
import java.util.HashSet;
import java.util.Set;
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
  private Set<String> affiliations = new HashSet<>();

  @CommandHandler
  public User(CreateUser command) {
    log.debug("handling {}", command);
    apply(new UserCreated(command.getId(), command.getUsername(), command.getEmail()));
  }

  @CommandHandler
  public void handle(SendAffiliationRequest command) {
    log.debug("handling {}", command);
    if (affiliations.contains(command.getAffiliatingUserId())) {
      return;
    }

    apply(
        new AffiliationRequestSent(
            command.getUserId(),
            command.getAffiliatingUserId(),
            command.getAffiliationRequestId()
        )
    );
  }

  @CommandHandler
  public void handle(AcceptAffiliationRequest command) {
    log.debug("handling {}", command);
    apply(
        new AffiliationRequestAccepted(
            command.getUserId(),
            command.getAffiliatingUserId(),
            command.getEmailId(),
            command.getAffiliationRequestId()
        )
    );
  }

  @CommandHandler
  public void handle(RejectAffiliationRequest command) {
    log.debug("handling {}", command);
    apply(
        new AffiliationRequestRejected(
            command.getUserId(),
            command.getAffiliatingUserId(),
            command.getEmailId(),
            command.getAffiliationRequestId()
        )
    );
  }

  @EventSourcingHandler
  public void on(UserCreated event) {
    this.id = event.getId();
    this.email = event.getEmail();
  }

  @EventSourcingHandler
  public void on(AffiliationRequestAccepted event) {
    affiliations.add(event.getAffiliatingUserId());
  }
}
