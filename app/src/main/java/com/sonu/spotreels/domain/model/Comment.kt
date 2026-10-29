package com.sonu.spotreels.domain.model

data class Comment(
    val id: String,
    val reelId: String,
    val authorUid: String,
    val text: String,
    val createdAtMillis: Long,
    val updatedAtMillis: Long? = null,
    val parentCommentId: String? = null,
    val status: CommentStatus = CommentStatus.VISIBLE
)

enum class CommentStatus {
    VISIBLE,
    HIDDEN,
    REMOVED
}
