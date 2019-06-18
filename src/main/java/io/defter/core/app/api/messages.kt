package io.defter.core.app.api

import org.axonframework.modelling.command.TargetAggregateIdentifier
import javax.validation.constraints.Min
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

data class CreateUser(@TargetAggregateIdentifier val id: String, @NotNull @Size(min = 5) val username: String)
data class UserCreated(val id: String, val username: String)

data class CreateExpenseGroup(@TargetAggregateIdentifier val id: String, @NotNull @Size(min = 5) val name: String, val currency: Currency, val members: List<String>)
data class ExpenseGroupCreated(val id: String, val name: String, val currency: Currency, val members: List<String>)

data class MemberAddedToGroup(@TargetAggregateIdentifier val id: String, val memberId: String)

data class AddSplitToGroup(@TargetAggregateIdentifier val id: String, @NotNull @Min(0, message = "Amount must be preset") val amount: Double, val payedBy: String, val description: String, val submittedBy: String)
data class SplitAddedToGroup(val id: String, val amount: Double, val payedBy: String, val description: String, val submittedBy: String)
