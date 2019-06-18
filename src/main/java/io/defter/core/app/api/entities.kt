package io.defter.core.app.api

import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.NamedQueries
import javax.persistence.NamedQuery

enum class Currency {
    USD,
    TRY,
    EURO
}

@Entity
@NamedQueries(
        NamedQuery(name = "UserView.fetch", query = "SELECT c FROM UserView c WHERE c.username LIKE CONCAT(:usernameStartsWith, '%') ORDER BY c.username")
)
data class UserView(@Id var id: String, var username: String) {
    constructor() : this("", "")
}

@Entity
@NamedQueries(
        NamedQuery(name = "ExpenseGroupView.fetch", query = "SELECT c FROM ExpenseGroupView c WHERE c.id LIKE CONCAT(:idStartsWith, '%') ORDER BY c.id"),
        NamedQuery(name = "ExpenseGroupView.count", query = "SELECT COUNT(c) FROM ExpenseGroupView c WHERE c.id LIKE CONCAT(:idStartsWith, '%')")
)
data class ExpenseGroupView(@Id var id: String, var name: String, var currency: Currency, var balance: Double, var numberOfSplits: Int) {
    constructor() : this("", "", Currency.USD, .0, 0)
}
