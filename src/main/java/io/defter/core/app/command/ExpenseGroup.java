package io.defter.core.app.command;

import io.defter.core.app.api.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

  @AggregateIdentifier private String id;
  private List<ExpenseGroupMember> members;

  // FIXME: Actually stack is not a suitable data structure here.
  //  We want to be able to undo any event which was done in any time.
  //  Undo/Redo in split is per action not like a shared application history.
  //  So here we keep info on how to undo/redo not `can` we undo/redo.
  //
  // TODO: maybe have an undoredoId kind of identifier provided by client side
  //  to undoable events  so we can pull undo/redo info in here and apply it.
  //  so undo action would say `new RevertAction("some undo id")`
  //  and we would know how to undo it.
  private Map<String, IUndoableAction> undoableActions = new HashMap<>();
  private Map<String, IUndoableAction> redoableActions = new HashMap<>();

  @CommandHandler
  public ExpenseGroup(CreateExpenseGroup command) {
    log.debug("handling {}", command);
    if (command.getMembers().size() < 2) {
      throw new IllegalStateException("Group needs at least 2 members");
    }

    List<String> memberIds =
        command.getMembers().stream().map(ExpenseGroupMember::getId).collect(Collectors.toList());

    if (command.getMembers().size() != new HashSet<>(memberIds).size()) {
      throw new IllegalStateException("Group contains duplicate members");
    }

    apply(
        new ExpenseGroupCreated(
            command.getId(),
            command.getName(),
            command.getCurrency(),
            command.getCreatedBy(),
            command.getMembers()));

    command.getMembers().forEach(member -> apply(new MemberAddedToGroup(command.getId(), member)));
  }

  @CommandHandler
  public void handle(AddSplitToGroup cmd) {
    log.debug("handling {}", cmd);

    Double sumOfShares =
        cmd.getMembers().stream().map(SplitMember::getShare).reduce(.0, Double::sum);

    if (Math.ceil(sumOfShares) != 100) {
      throw new IllegalStateException("Total of split share is not correct");
    }

    List<String> memberIds =
        cmd.getMembers().stream().map(SplitMember::getId).collect(Collectors.toList());

    List<String> groupMemberIds =
        members.stream().map(ExpenseGroupMember::getId).collect(Collectors.toList());

    if (!groupMemberIds.contains(cmd.getSubmittedBy())) {
      throw new IllegalStateException("User needs to be a part of group to be able to add a split");
    }
    if (!groupMemberIds.contains(cmd.getPayedBy())) {
      throw new IllegalStateException("User needs to be a part of group to be able to pay a split");
    }
    if (!groupMemberIds.containsAll(memberIds)) {
      throw new IllegalStateException("Split contains a member who is not part of this group");
    }

    apply(
        new SplitAddedToGroup(
            cmd.getId(),
            cmd.getAmount(),
            cmd.getPayedBy(),
            cmd.getDescription(),
            cmd.getSubmittedBy(),
            cmd.getCreatedAt(),
            cmd.getMembers(),
            cmd.getCurrency()));
  }

  @CommandHandler
  public void handle(AcceptExpenseGroupInvitation command) {
    log.debug("handling {}", command);

    apply(
        new ExpenseGroupInvitationAccepted(
            command.getGroupId(), command.getInvitedUserId(), command.getInvitationRequestId()));
  }

  @CommandHandler
  public void handle(RejectExpenseGroupInvitation command) {
    log.debug("handling {}", command);

    apply(
        new ExpenseGroupInvitationRejected(
            command.getGroupId(), command.getInvitedUserId(), command.getInvitationRequestId()));
  }

  @CommandHandler
  public void handle(SettleExpenseGroup command) {
    log.debug("handling {}", command);

    // TODO: Insert Main business logic here.
    apply(new ExpenseGroupSettled(this.id, "TODO"));
  }

  @CommandHandler
  public void handle(SettleMember command) {
    log.debug("handling {}", command);

    // TODO: Insert Main business logic here.
    apply(new MemberSettled(this.id, "TODO", .5, Currency.USD));
  }

  @CommandHandler
  public void handle(ArchiveExpenseGroup command) {
    log.debug("handling {}", command);

    // TODO: Insert Main business logic here.
    apply(new MemberSettled(this.id, "TODO", .5, Currency.USD));
  }

  @EventSourcingHandler
  public void on(ExpenseGroupCreated event) {
    id = event.getId();
    members = event.getMembers();
  }

  @EventSourcingHandler
  public void on(ExpenseGroupSettled event) {
    this.undoStack.push(event);
  }

  @EventSourcingHandler
  public void on(MemberSettled event) {
    this.undoStack.push(event);
  }

  @EventSourcingHandler
  public void on(ExpenseGroupArchived event) {
    this.undoStack.push(event);
  }
}
