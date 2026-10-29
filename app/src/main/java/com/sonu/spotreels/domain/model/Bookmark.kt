package com.sonu.spotreels.domain.model

/** A user's private saved-reel relationship. */
data class Bookmark(
    val userUid: String,
    val reelId: String,
    val createdAtMillis: Long
)
