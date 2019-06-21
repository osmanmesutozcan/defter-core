package io.defter.core.app.api

import java.util.*
import javax.persistence.*
import kotlin.collections.ArrayList

enum class Currency {
    USD,
    TRY,
    EURO
}

@Entity
@NamedQueries(
        NamedQuery(name = "UserView.fetch", query = "SELECT c FROM UserView c WHERE c.username LIKE CONCAT(:usernameStartsWith, '%') ORDER BY c.username"),
        NamedQuery(name = "UserView.fetchWhereIdIn", query = "SELECT c FROM UserView c WHERE c.id IN (:idsList) ORDER BY c.username"),
        NamedQuery(name = "UserView.fetchById", query = "SELECT c FROM UserView c WHERE c.id = :userId")
)
data class UserView(
        @Id var id: String,
        var username: String,
        var avatar: String
) {
    constructor() : this("", "", "")
}

@Entity
@NamedQueries(
        NamedQuery(name = "UserAffiliateView.exists", query = "SELECT COUNT(c) FROM UserAffiliateView c WHERE c.userId = :userId AND c.friendId = :friendId"),
        NamedQuery(name = "UserAffiliateView.fetchByUserId", query = "SELECT c FROM UserAffiliateView c WHERE c.userId = :userId ORDER BY c.id")
)
@Table(uniqueConstraints = [UniqueConstraint(columnNames = ["userId", "friendId"])])
data class UserAffiliateView(
        @Id var id: String,
        var userId: String,
        var friendId: String
) {
    constructor() : this("", "", "")
}

@Entity
@NamedQueries(
        NamedQuery(name = "ExpenseGroupView.fetch", query = "SELECT c FROM ExpenseGroupView c WHERE c.id LIKE CONCAT(:idStartsWith, '%') ORDER BY c.id"),
        NamedQuery(name = "ExpenseGroupView.count", query = "SELECT COUNT(c) FROM ExpenseGroupView c WHERE c.id LIKE CONCAT(:idStartsWith, '%')")
)
data class ExpenseGroupView(
        @Id var id: String,
        var name: String,
        var currency: Currency, // TODO: Enum this.
        var balance: Double,
        var numberOfSplits: Int,
        @ElementCollection(targetClass = String::class) var members: List<String>
) {
    constructor() : this("", "", Currency.USD, .0, 0, ArrayList<String>())
}

@Entity
@NamedQueries(
        NamedQuery(name = "SplitView.fetch", query = "SELECT c FROM SplitView c WHERE c.groupId = :groupId ORDER BY c.createdAt")
)
data class SplitView(
        @Id var id: String,
        var total: Double,
        var groupId: String,
        var description: String,
        var payedBy: String,
        var submittedBy: String,
        var createdAt: Date,
        @ElementCollection var members: List<SplitMember>
) {
    constructor() : this("", .0, "", "", "", "", Date(), ArrayList<SplitMember>())
}

@Embeddable
data class SplitMember(var id: String, var locked: Boolean, var share: Float) {
    constructor() : this("", true, 0f)
}

@Entity
@NamedQueries(
        NamedQuery(name = "SettlementView.fetch", query = "SELECT c FROM SettlementView c WHERE c.groupId = :groupId"),
        NamedQuery(name = "SettlementView.fetchForUser", query = "SELECT c FROM SettlementView c WHERE c.groupId = :groupId AND c.userId = :userId")
)
data class SettlementView(
        @Id var id: String,
        var userId: String,
        var groupId: String,
        var createdAt: Date,
        @ElementCollection var balances: List<SettlementBalance>
) {
    constructor() : this("", "", "", Date(), ArrayList<SettlementBalance>())
}

@Embeddable
data class SettlementBalance(var userId: String, var balance: Double) {
    constructor() : this("", .0)
}
