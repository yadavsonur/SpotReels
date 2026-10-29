package com.sonu.spotreels.domain.model

data class Reel(
    val id: String,
    val ownerUid: String,
    val caption: String,
    val media: ReelMedia,
    val visibility: ReelVisibility = ReelVisibility.PUBLIC,
    val status: ReelStatus = ReelStatus.DRAFT,
    val hashtags: List<String> = emptyList(),
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
    val publishedAtMillis: Long? = null,
    val likeCount: Long = 0,
    val commentCount: Long = 0,
    val viewCount: Long = 0
)

enum class ReelVisibility {
    PUBLIC,
    FOLLOWERS,
    PRIVATE
}

enum class ReelStatus {
    DRAFT,
    PROCESSING,
    PUBLISHED,
    HIDDEN,
    REMOVED,
    FAILED
}
