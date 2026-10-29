package com.sonu.spotreels.domain.model

/** A user's private saved-reel relationship. */
data class Bookmark(
    val userUid: String,
    val reelId: String,
    val createdAtMillis: Long
) {
    init {
        require(userUid.isNotBlank()) { "userUid must not be blank" }
        require(reelId.isNotBlank()) { "reelId must not be blank" }
        require(createdAtMillis >= 0) { "createdAtMillis must be non-negative" }
    }
}
