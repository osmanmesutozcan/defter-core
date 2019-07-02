package io.defter.core.app.command;

import io.defter.core.app.api.*;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.XSlf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.context.annotation.Profile;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

@XSlf4j
@Aggregate
@Profile("command")
@NoArgsConstructor
public class ExpenseGroup {

  @AggregateIdentifier
  private String id;
  private List<ExpenseGroupMember> members;

  @CommandHandler
  public ExpenseGroup(CreateExpenseGroup command) {
    log.debug("handling {}", command);
    if (command.getMembers().size() < 2) {
      throw new IllegalStateException("Group needs at least 2 members");
    }

    apply(new ExpenseGroupCreated(command.getId(), command.getName(), command.getCurrency(), command.getMembers()));
    command.getMembers().forEach(member -> apply(new MemberAddedToGroup(command.getId(), member)));
  }

  @CommandHandler
  public void handle(AddSplitToGroup cmd) {
    log.debug("handling {}", cmd);

    List<String> memberIds = cmd
        .getMembers()
        .stream()
        .map(SplitMember::getId)
        .collect(Collectors.toList());

    memberIds.add(cmd.getSubmittedBy());
    memberIds.add(cmd.getPayedBy());

    List<String> groupMemberIds = members
        .stream()
        .map(ExpenseGroupMember::getId)
        .collect(Collectors.toList());

    if (!groupMemberIds.containsAll(memberIds)) {
      throw new IllegalStateException("Split contains a member who is not part of this group");
    }
    apply(new SplitAddedToGroup(cmd.getId(), cmd.getAmount(), cmd.getPayedBy(), cmd.getDescription(),
        cmd.getSubmittedBy(), cmd.getCreatedAt(), cmd.getMembers()));
  }

  @CommandHandler
  public void handle(AcceptExpenseGroupInvitation event) {
    apply(
        new ExpenseGroupInvitationAccepted(
            event.getGroupId(),
            event.getInvitedUserId(),
            event.getInvitationRequestId()
        )
    );
  }

  @CommandHandler
  public void handle(RejectExpenseGroupInvitation event) {
    apply(
        new ExpenseGroupInvitationRejected(
            event.getGroupId(),
            event.getInvitedUserId(),
            event.getInvitationRequestId()
        )
    );
  }

  @EventSourcingHandler
  public void on(ExpenseGroupCreated event) {
    id = event.getId();
    members = event.getMembers();
  }
}
