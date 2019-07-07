package io.defter.core.app.command;

import io.defter.core.app.api.*;
import java.util.HashSet;
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

    List<String> memberIds = command
        .getMembers()
        .stream()
        .map(ExpenseGroupMember::getId)
        .collect(Collectors.toList());

    if (command.getMembers().size() != new HashSet<>(memberIds).size()) {
      throw new IllegalStateException("Group contains duplicate members");
    }

    apply(new ExpenseGroupCreated(
        command.getId(),
        command.getName(),
        command.getCurrency(),
        command.getCreatedBy(),
        command.getMembers()
    ));

    command
        .getMembers()
        .forEach(member -> apply(new MemberAddedToGroup(command.getId(), member)));
  }

  @CommandHandler
  public void handle(AddSplitToGroup cmd) {
    log.debug("handling {}", cmd);

    Double sumOfShares = cmd
        .getMembers()
        .stream()
        .map(SplitMember::getShare)
        .reduce(.0, Double::sum);

    if (Math.ceil(sumOfShares) != 100) {
      throw new IllegalStateException("Total of split share is not correct");
    }

    List<String> memberIds = cmd
        .getMembers()
        .stream()
        .map(SplitMember::getId)
        .collect(Collectors.toList());

    List<String> groupMemberIds = members
        .stream()
        .map(ExpenseGroupMember::getId)
        .collect(Collectors.toList());

    if (!groupMemberIds.contains(cmd.getSubmittedBy())) {
      throw new IllegalStateException("User needs to be a part of group to be able to add a split");
    }
    if (!groupMemberIds.contains(cmd.getPayedBy())) {
      throw new IllegalStateException("User needs to be a part of group to be able to pay a split");
    }
    if (!groupMemberIds.containsAll(memberIds)) {
      throw new IllegalStateException("Split contains a member who is not part of this group");
    }

    apply(new SplitAddedToGroup(cmd.getId(), cmd.getAmount(), cmd.getPayedBy(), cmd.getDescription(),
        cmd.getSubmittedBy(), cmd.getCreatedAt(), cmd.getMembers(), cmd.getCurrency()));
  }

  @CommandHandler
  public void handle(AcceptExpenseGroupInvitation command) {
    apply(
        new ExpenseGroupInvitationAccepted(
            command.getGroupId(),
            command.getInvitedUserId(),
            command.getInvitationRequestId()
        )
    );
  }

  @CommandHandler
  public void handle(RejectExpenseGroupInvitation command) {
    apply(
        new ExpenseGroupInvitationRejected(
            command.getGroupId(),
            command.getInvitedUserId(),
            command.getInvitationRequestId()
        )
    );
  }

  @EventSourcingHandler
  public void on(ExpenseGroupCreated event) {
    id = event.getId();
    members = event.getMembers();
  }
}
