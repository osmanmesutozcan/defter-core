package io.defter.core.app.api

import io.defter.core.app.saga.MemberAffiliationManagement
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
        @NotNull @Size(min = 5) val username: String,
        @NotNull @Email val email: String
)

data class UserCreated(
        val id: String,
        val username: String,
        val email: String
)

/**
 * Affiliation
 */
data class SendAffiliationRequest(
        @TargetAggregateIdentifier val userId: String,
        @NotNull val affiliatingUserId: String,
        @NotNull val affiliationRequestId: String
)

data class AffiliationRequestSent(
        val userId: String,
        val affiliatingUserId: String,
        val affiliationRequestId: String
)

data class AnswerAffiliationRequest(
        @TargetAggregateIdentifier val affiliationRequestId: String,
        @NotNull val emailId: String,
        @NotNull val answer: MemberAffiliationManagement.AffiliationAnswer
)

data class AffiliationRequestAnswered(
        val affiliationRequestId: String,
        val emailId: String,
        val answer: MemberAffiliationManagement.AffiliationAnswer
)

data class AcceptAffiliationRequest(
        @TargetAggregateIdentifier val userId: String,
        @NotNull val affiliatingUserId: String,
        @NotNull val affiliationRequestId: String,
        @NotNull val emailId: String
)

data class AffiliationRequestAccepted(
        val userId: String,
        val affiliatingUserId: String,
        val affiliationRequestId: String,
        val emailId: String
)

data class RejectAffiliationRequest(
        @TargetAggregateIdentifier val userId: String,
        @NotNull val affiliatingUserId: String,
        @NotNull val affiliationRequestId: String,
        @NotNull val emailId: String
)

data class AffiliationRequestRejected(
        val userId: String,
        val affiliatingUserId: String,
        val affiliationRequestId: String,
        val emailId: String
)

data class CreateExpenseGroup(
        @TargetAggregateIdentifier val id: String,
        @NotNull @Size(min = 5) val name: String,
        val currency: Currency,
        val members: List<ExpenseGroupMember>
)

data class ExpenseGroupCreated(
        val id: String,
        val name: String,
        val currency: Currency,
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
        val members: List<SplitMember>
)

data class SplitAddedToGroup(
        val id: String,
        val amount: Double,
        val payedBy: String,
        val description: String,
        val submittedBy: String,
        val createdAt: Date,
        val members: List<SplitMember>
)
