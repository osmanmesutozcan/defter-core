package io.defter.core.app.api

import io.defter.core.app.saga.ExpenseGroupInvitationManagement
import io.defter.core.app.saga.MemberInvitationManagement
import org.axonframework.modelling.command.TargetAggregateIdentifier
import org.springframework.data.annotation.CreatedBy
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
        @NotNull val password: String
)

data class UserCreated(
        val id: String,
        val name: String,
        val email: String,
        val passwordHash: String
)

/**
 * New User Invitation
 */
open class SendMemberInvitation(
        @TargetAggregateIdentifier @NotNull val invitationRequestId: String,
        @NotNull val invitedUserId: String
)

data class MemberInvitationSent(
        val invitationRequestId: String,
        val invitedUserId: String
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
        @NotNull val groupId: String
)

data class ExpenseGroupInvitationSent(
        val invitationRequestId: String,
        val invitedUserId: String,
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

// PERIPHERAL EVENTS

data class ScheduledElapsed(
        val description: String,
        val type: ScheduledEventTypes
)

enum class ScheduledEventTypes {
    EXCHANGE_RATES_UPDATES
}
