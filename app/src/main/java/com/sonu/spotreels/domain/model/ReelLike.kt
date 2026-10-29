package com.sonu.spotreels.domain.model

/** One user's like relationship to one reel; unique by (reelId, userUid). */
data class ReelLike(
    val reelId: String,
    val userUid: String,
    val createdAtMillis: Long
) {
    init {
        require(reelId.isNotBlank()) { "reelId must not be blank" }
        require(userUid.isNotBlank()) { "userUid must not be blank" }
        require(createdAtMillis >= 0) { "createdAtMillis must be non-negative" }
    }
}
