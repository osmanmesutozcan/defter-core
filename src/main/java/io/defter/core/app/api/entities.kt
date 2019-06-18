package io.defter.core.app.api

import javax.persistence.*

enum class Currency {
    USD,
    TRY,
    EURO
}

@Entity
@NamedQueries(
        NamedQuery(name = "UserView.fetch", query = "SELECT c FROM UserView c WHERE c.username LIKE CONCAT(:usernameStartsWith, '%') ORDER BY c.username"),
        NamedQuery(name = "UserView.fetchWhereIdIn", query = "SELECT c FROM UserView c WHERE c.id IN (:idsList) ORDER BY c.username")
)
data class UserView(@Id var id: String, var username: String, var avatar: String) {
    constructor() : this("", "", "")
}

@Entity
@NamedQueries(
        NamedQuery(name = "UserAffiliateView.fetchByUserId", query = "SELECT c FROM UserAffiliateView c WHERE c.userId = :userId ORDER BY c.id")
)
data class UserAffiliateView(@Id var id: String, var userId: String, var friendId: String) {
    constructor() : this("", "", "")
}

@Entity
@NamedQueries(
        NamedQuery(name = "ExpenseGroupView.fetch", query = "SELECT c FROM ExpenseGroupView c WHERE c.id LIKE CONCAT(:idStartsWith, '%') ORDER BY c.id"),
        NamedQuery(name = "ExpenseGroupView.count", query = "SELECT COUNT(c) FROM ExpenseGroupView c WHERE c.id LIKE CONCAT(:idStartsWith, '%')")
)
data class ExpenseGroupView(@Id var id: String, var name: String, var currency: Currency, var balance: Double, var numberOfSplits: Int, @ElementCollection(targetClass = String::class) var members: List<String>) {
    constructor() : this("", "", Currency.USD, .0, 0, ArrayList<String>())
}
