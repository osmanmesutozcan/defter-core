package io.defter.core.app.api

import io.defter.core.app.saga.ExpenseGroupInvitationManagement
import io.defter.core.app.saga.MemberInvitationManagement
import org.axonframework.modelling.command.TargetAggregateIdentifier
import java.util.*
import javax.validation.constraints.Email
import javax.validation.constraints.Min
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

/**
 * User
 */
data class CreateUser(
        @TargetAggregateIdentifier val id: String,
        @NotNull @Size(min = 5) val name: String,
        @NotNull @Email val email: String,
        @NotNull val password: String,
        @NotNull val isVerifiedUser: Boolean
)

data class UserCreated(
        val id: String,
        val name: String,
        val email: String,
        val passwordHash: String,
        val isVerifiedUser: Boolean
)

/**
 * New User Invitation
 */
open class SendMemberInvitation(
        @TargetAggregateIdentifier @NotNull val invitationRequestId: String,
        @NotNull val invitedUserId: String,
        val invitedUserEmail: String
)

data class MemberInvitationSent(
        val invitationRequestId: String,
        val invitedUserId: String,
        val invitedUserEmail: String
)

data class AnswerMemberInvitation(
        @TargetAggregateIdentifier val invitationRequestId: String,
        @NotNull val emailId: String,
        @NotNull val answer: MemberInvitationManagement.InvitationAnswer
)

data class MemberInvitationAnswered(
        val invitationRequestId: String,
        val emailId: String,
        val answer: MemberInvitationManagement.InvitationAnswer
)

data class AcceptMemberInvitation(
        @TargetAggregateIdentifier @NotNull val invitedUserId: String,
        @NotNull val invitationRequestId: String,
        @NotNull val emailId: String
)

data class MemberInvitationAccepted(
        val invitedUserId: String,
        val invitationRequestId: String,
        val emailId: String
)

data class RejectMemberInvitation(
        @TargetAggregateIdentifier @NotNull val invitedUserId: String,
        @NotNull val invitationRequestId: String,
        @NotNull val emailId: String
)

data class MemberInvitationRejected(
        val invitedUserId: String,
        val invitationRequestId: String,
        val emailId: String
)

/**
 * Expense Group Invitation
 */
data class SendExpenseGroupInvitation(
        @TargetAggregateIdentifier @NotNull val invitationRequestId: String,
        @NotNull val invitedUserId: String,
        @NotNull val invitedUserEmail: String,
        @NotNull val groupId: String
)

data class ExpenseGroupInvitationSent(
        val invitationRequestId: String,
        val invitedUserId: String,
        val invitedUserEmail: String,
        val groupId: String
)

data class AnswerExpenseGroupInvitation(
        @TargetAggregateIdentifier val invitationRequestId: String,
        @NotNull val answeredUserId: String,
        @NotNull val answer: ExpenseGroupInvitationManagement.InvitationAnswer
)

data class ExpenseGroupInvitationAnswered(
        val invitationRequestId: String,
        val answeredUserId: String,
        val answer: ExpenseGroupInvitationManagement.InvitationAnswer
)

data class AcceptExpenseGroupInvitation(
        @TargetAggregateIdentifier val groupId: String,
        @NotNull val invitedUserId: String,
        @NotNull val invitationRequestId: String
)

data class ExpenseGroupInvitationAccepted(
        val groupId: String,
        val invitedUserId: String,
        val invitationRequestId: String
)

data class RejectExpenseGroupInvitation(
        @TargetAggregateIdentifier val groupId: String,
        @NotNull val invitedUserId: String,
        @NotNull val invitationRequestId: String
)

data class ExpenseGroupInvitationRejected(
        val groupId: String,
        val invitedUserId: String,
        val invitationRequestId: String
)

/**
 * Expense Group
 */
data class CreateExpenseGroup(
        @TargetAggregateIdentifier val id: String,
        @NotNull @Size(min = 5) val name: String,
        val currency: Currency,
        val createdBy: String,
        val members: List<ExpenseGroupMember>
)

data class ExpenseGroupCreated(
        val id: String,
        val name: String,
        val currency: Currency,
        val createdBy: String,
        val members: List<ExpenseGroupMember>
)

data class MemberAddedToGroup(
        @TargetAggregateIdentifier val id: String,
        val member: ExpenseGroupMember
)

data class AddSplitToGroup(
        @TargetAggregateIdentifier val id: String,
        @NotNull @Min(0, message = "Amount must be present") val amount: Double,
        val payedBy: String,
        val description: String,
        val submittedBy: String,
        val createdAt: Date,
        val members: List<SplitMember>,
        val currency: Currency
)

data class SplitAddedToGroup(
        val id: String,
        val amount: Double,
        val payedBy: String,
        val description: String,
        val submittedBy: String,
        val createdAt: Date,
        val members: List<SplitMember>,
        val currency: Currency
)

/**
 *  Archives an expense group.
 *  This action is undoable.
 */
data class ArchiveExpenseGroup(
        @TargetAggregateIdentifier val groupId: String,
        @NotNull val submittedBy: String
)

data class ExpenseGroupArchived(
        val groupId: String,
        override val undoableActionId: String,
        val submittedBy: String
) : IUndoableAction

/**
 * All of group debt between members is settled.
 * This event is undoable.
 */
data class SettleExpenseGroup(
        @TargetAggregateIdentifier val groupId: String,
        @NotNull val submittedBy: String
)

data class ExpenseGroupSettled(
        val groupId: String,
        override val undoableActionId: String,
        val submittedBy: String
) : IUndoableAction

/**
 * A certain amount between the members of a group
 * is settled. This event is undoable
 */
data class SettleMember(
        @TargetAggregateIdentifier val groupId: String,
        @NotNull val submittedBy: String,
        @NotNull val amount: Double,
        @NotNull val currency: Currency
)

data class MemberSettled(
        val groupId: String,
        override val undoableActionId: String,
        val submittedBy: String,
        val amount: Double,
        val currency: Currency
) : IUndoableAction

// PERIPHERAL EVENTS

data class PushNotificationDispatched(
        val userId: String,
        val title: String,
        val body: String
)

data class EmailDispatched(
        val email: String,
        val title: String,
        val body: String
)

data class ScheduledElapsed(
        val description: String,
        val type: ScheduledEventTypes
)

enum class ScheduledEventTypes {
    EXCHANGE_RATES_UPDATES
}

// TODO: Finish this..
interface IUndoableAction {
    val undoableActionId: String;
}
