package com.cst3115.enterprise.techfix.Data.remote

/**
 * Matches the test API's post format.
 * The body contains task totals, without client details or resolution notes.
 */
data class DispatchUpdateRequest(
    val title: String,
    val body: String,
    val userId: Int = 1
)

/**
 * The test API returns an ID to acknowledge the simulated update.
 */
data class DispatchUpdateResponse(
    val id: Int? = null
)