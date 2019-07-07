package io.defter.core.app.api

/**
 * Expense Groups View
 */
class CountExpenseGroupViewsQuery(val filter: ExpenseGroupViewFilter = ExpenseGroupViewFilter()) {
    override fun toString(): String = "CountExpenseGroupSummaryQuery"
}

data class ExpenseGroupViewFilter(val idStartsWith: String = "")
data class FetchExpenseGroupViewQuery(val id: String)
data class FetchExpenseGroupViewsQuery(val offset: Int, val limit: Int, val filter: ExpenseGroupViewFilter)
data class CountExpenseGroupViewsResponse(val count: Int, val lastEvent: Long)

/**
 * User View
 */
data class UserViewFilter(val usernameStartsWith: String = "")
data class FetchUserViewsQuery(val offset: Int, val limit: Int, val filter: UserViewFilter)
data class FetchUserViewByEmail(val email: String)
data class FetchUserViewById(val id: String)
data class FetchUserViewsByIds(val ids: List<String>)

/**
 * Affiliates View
 */
data class FetchUserAffiliatesQuery(val userId: String)

/**
 * Splits View
 */
data class FetchExpenseGroupsSplitsQuery(val groupId: String)

/**
 * Settlement View
 */
data class FetchExpenseGroupSettlementQuery(val userId: String, val groupId: String)

/**
 * Invitation
 */
data class FetchInvitationsOfUser(val userId: String)
