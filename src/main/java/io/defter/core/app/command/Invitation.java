package io.defter.core.app.command;

import graphql.GraphQLException;
import io.defter.core.app.api.AnswerExpenseGroupInvitation;
import io.defter.core.app.api.EmailDispatched;
import io.defter.core.app.api.ExpenseGroupInvitationAnswered;
import io.defter.core.app.api.ExpenseGroupInvitationSent;
import io.defter.core.app.api.MemberInvitationAnswered;
import io.defter.core.app.api.MemberInvitationSent;
import io.defter.core.app.api.AnswerMemberInvitation;
import io.defter.core.app.api.PushNotificationDispatched;
import io.defter.core.app.api.SendExpenseGroupInvitation;
import io.defter.core.app.api.SendMemberInvitation;
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
public class Invitation {

  @AggregateIdentifier
  private String invitationRequestId;
  private String invitedUserId;
  private Boolean isRequestFulfilled = false;

  @CommandHandler
  public Invitation(SendMemberInvitation command) {
    log.debug("handling {}", command);

    apply(
        new MemberInvitationSent(
            command.getInvitationRequestId(),
            command.getInvitedUserId(),
            command.getInvitedUserEmail()
        )
    );
  }

  @CommandHandler
  public Invitation(SendExpenseGroupInvitation command) {
    log.debug("handling {}", command);

    apply(
        new ExpenseGroupInvitationSent(
            command.getInvitationRequestId(),
            command.getInvitedUserId(),
            command.getInvitedUserEmail(),
            command.getGroupId()
        )
    );
  }

  @CommandHandler
  public void handle(AnswerMemberInvitation command) {
    log.debug("handling {}", command);

    if (isRequestFulfilled) {
      throw new GraphQLException("This request is already answered");
    }

    apply(
        new MemberInvitationAnswered(
            command.getInvitationRequestId(),
            command.getEmailId(),
            command.getAnswer()
        )
    );
  }

  @CommandHandler
  public void handle(AnswerExpenseGroupInvitation command) {
    log.debug("handling {}", command);

    if (isRequestFulfilled) {
      throw new GraphQLException("This request is already answered");
    }

    if (!command.getAnsweredUserId().equals(invitedUserId)) {
      throw new GraphQLException("Answering user does not match the invited user");
    }

    apply(
        new ExpenseGroupInvitationAnswered(
            command.getInvitationRequestId(),
            command.getAnsweredUserId(),
            command.getAnswer()
        )
    );
  }

  @EventSourcingHandler
  public void on(MemberInvitationSent event) {
    invitationRequestId = event.getInvitationRequestId();
  }

  @EventSourcingHandler
  public void on(ExpenseGroupInvitationSent event) {
    invitationRequestId = event.getInvitationRequestId();
    invitedUserId = event.getInvitedUserId();
  }

  @EventSourcingHandler
  public void on(ExpenseGroupInvitationAnswered event) {
    isRequestFulfilled = true;
  }
}
