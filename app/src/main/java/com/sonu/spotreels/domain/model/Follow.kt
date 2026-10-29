package com.sonu.spotreels.domain.model

/**
 * Directional relationship: followerUid follows followedUid.
 * Persist one canonical record per pair; reverse indexes are optional.
 */
data class Follow(
    val followerUid: String,
    val followedUid: String,
    val createdAtMillis: Long
) {
    init {
        require(followerUid.isNotBlank() && followedUid.isNotBlank()) { "user IDs must not be blank" }
        require(followerUid != followedUid) { "a user cannot follow themselves" }
        require(createdAtMillis >= 0) { "createdAtMillis must be non-negative" }
    }
}
