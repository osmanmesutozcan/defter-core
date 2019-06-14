package io.defter.core.app.api

import org.axonframework.modelling.command.TargetAggregateIdentifier

import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.NamedQueries
import javax.persistence.NamedQuery

data class CreateExpenseGroup(@TargetAggregateIdentifier val id: String, val name: String, val currency: String, val members: List<String>)
data class ExpenseGroupCreated(@TargetAggregateIdentifier val id: String, val name: String, val currency: String, val members: List<String>)

@Entity
@NamedQueries(
        NamedQuery(name = "ExpenseGroup.fetch",
                query = "SELECT c FROM ExpenseGroup c WHERE c.id LIKE CONCAT(:idStartsWith, '%') ORDER BY c.id"),
        NamedQuery(name = "ExpenseGroup.count",
                query = "SELECT COUNT(c) FROM ExpenseGroup c WHERE c.id LIKE CONCAT(:idStartsWith, '%')"))
data class ExpenseGroup(@Id var id: String, var initialValue: Int, var remainingValue: Int) {
    constructor() : this("", 0, 0)
}

