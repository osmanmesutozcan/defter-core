package io.defter.core.app.api

data class ExpenseGroupViewFilter(val idStartsWith: String = "")
class CountExpenseGroupViewsQuery(val filter: ExpenseGroupViewFilter = ExpenseGroupViewFilter()) {
    override fun toString(): String = "CountExpenseGroupSummaryQuery"
}

data class CountExpenseGroupViewsResponse(val count: Int, val lastEvent: Long)
data class FetchExpenseGroupViewsQuery(val offset: Int, val limit: Int, val filter: ExpenseGroupViewFilter)

data class UserViewFilter(val usernameStartsWith: String = "")
data class FetchUserViewsQuery(val offset: Int, val limit: Int, val filter: UserViewFilter)
data class FetchUserViewsByIds(val ids: List<String>)
