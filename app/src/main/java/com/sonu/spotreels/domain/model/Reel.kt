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
) {
    init {
        require(id.isNotBlank()) { "id must not be blank" }
        require(ownerUid.isNotBlank()) { "ownerUid must not be blank" }
        require(createdAtMillis >= 0 && updatedAtMillis >= 0) { "timestamps must be non-negative" }
        require(publishedAtMillis == null || publishedAtMillis >= 0) { "publishedAtMillis must be non-negative" }
        require(likeCount >= 0 && commentCount >= 0 && viewCount >= 0) { "reel counts must be non-negative" }
        require(hashtags.all { it.isNotBlank() }) { "hashtags must not contain blank values" }
    }
}

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
