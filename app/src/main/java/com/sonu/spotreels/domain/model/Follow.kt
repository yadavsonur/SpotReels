package com.sonu.spotreels.domain.model

/**
 * Directional relationship: followerUid follows followedUid.
 * Persist one canonical record per pair; reverse indexes are optional.
 */
data class Follow(
    val followerUid: String,
    val followedUid: String,
    val createdAtMillis: Long
)
