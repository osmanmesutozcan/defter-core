package io.defter.core.app.api

import io.defter.core.app.saga.ExpenseGroupInvitationManagement
import lombok.Data
import java.util.*
import javax.persistence.*
import kotlin.collections.ArrayList

enum class Currency {
    USD, TRY, EURO, IDR, KRW, HUF, ISK, JPY, INR,
    RUB, PHP, THB, CZK, MXN, ZAR, SEK, NOK, HKD,
    CNY, DKK, HRK, RON, MYR, BRL, PLN, ILS, BGN,
    NZD, AUD, SGD, CAD, CHF, EUR, GBP
}

@Data
@Entity
@NamedQueries(
        NamedQuery(name = "UserView.fetch", query = "SELECT c FROM UserView c WHERE c.name LIKE CONCAT(:usernameStartsWith, '%') ORDER BY c.name"),
        NamedQuery(name = "UserView.fetchWhereIdIn", query = "SELECT c FROM UserView c WHERE c.id IN (:idsList) ORDER BY c.name"),
        NamedQuery(name = "UserView.fetchById", query = "SELECT c FROM UserView c WHERE c.id = :userId"),
        NamedQuery(name = "UserView.fetchByEmail", query = "SELECT c FROM UserView c WHERE c.email = :email"),
        NamedQuery(name = "UserView.existsByEmail", query = "SELECT COUNT(c) FROM UserView c WHERE c.email = :email")
)
data class UserView(
        @Id var id: String,
        var name: String,
        var email: String,
        var passwordHash: String,
        var avatar: String
) {
    constructor() : this("", "", "", "", "")
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
        NamedQuery(name = "ExpenseGroupView.fetch", query = "SELECT c FROM ExpenseGroupView c WHERE c.id LIKE CONCAT(:idStartsWith, '%') ORDER BY c.updatedAt DESC"),
        NamedQuery(name = "ExpenseGroupView.count", query = "SELECT COUNT(c) FROM ExpenseGroupView c WHERE c.id LIKE CONCAT(:idStartsWith, '%')")
)
data class ExpenseGroupView(
        @Id var id: String,
        var name: String,
        @Enumerated(EnumType.STRING) var currency: Currency, // TODO: Enum this.
        var balance: Double,
        var numberOfSplits: Int,
        var createdBy: String,
        var updatedAt: Date,
        @ElementCollection var members: List<ExpenseGroupMember>
) {
    constructor() : this("", "", Currency.USD, .0, 0, "", Date(), ArrayList<ExpenseGroupMember>())

    @PreUpdate
    fun updatedAt() {
        this.updatedAt = Date()
    }

    @PrePersist
    fun createdAt() {
        this.updatedAt = Date()
    }
}

@Embeddable
data class ExpenseGroupMember(val id: String, val email: String) {
    constructor() : this("", "")
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
        var createdAt: Date, // client provided date to make sure we know the origin time.
        var currency: Currency,
        var rate: Double,
        @ElementCollection var members: List<SplitMember>
) {
    constructor() : this("", .0, "", "", "", "", Date(), Currency.USD, .0, ArrayList<SplitMember>())
}

@Embeddable
data class SplitMember(var id: String, var locked: Boolean, var share: Double) {
    constructor() : this("", true, .0)
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

    @PrePersist
    fun createdAt() {
        this.createdAt = Date()
    }
}

@Embeddable
data class SettlementBalance(var userId: String, var balance: Double) {
    constructor() : this("", .0)
}

@Entity
@NamedQueries(
        NamedQuery(name = "ExpenseGroupInvitationView.fetch", query = "SELECT c FROM ExpenseGroupInvitationView c WHERE c.invitedUserId = :userId")
)
data class ExpenseGroupInvitationView(
        @Id var id: String,
        var invitedUserId: String,
        var expenseGroupId: String,
        @Column(nullable = true) @Enumerated(EnumType.STRING) var answer: ExpenseGroupInvitationManagement.InvitationAnswer?,
        var createdAt: Date
) {
    constructor() : this("", "", "", null, Date())

    @PrePersist
    fun createdAt() {
        this.createdAt = Date()
    }
}

@Entity
@Table(
        indexes = [
            Index(name = "id", columnList = "id", unique = true),
            Index(name = "symbol", columnList = "symbol", unique = false)
        ]
)
@NamedQueries(
        NamedQuery(name = "CurrencyExchangeRate.getLatestBySymbol", query = "SELECT c FROM CurrencyExchangeRate c WHERE c.symbol = :symbol ORDER BY c.createdAt DESC")
)
data class CurrencyExchangeRate(
        @Id var id: String,
        @Enumerated(EnumType.STRING) var symbol: Currency,
        var rate: Double,
        var createdAt: Date
) {
    constructor() : this("", Currency.USD, .0, Date())

    @PrePersist
    fun createdAt() {
        this.createdAt = Date()
    }
}

@Entity
data class ActivityView(
        @Id var id: String,
        var actor: String,
        var verb: String,
        var activityObject: String,
        var foreignId: String,
        var createdAt: Date
) {
    constructor() : this("", "", "", "", "", Date())

    @PrePersist
    fun createdAt() {
        this.createdAt = Date()
    }
}
