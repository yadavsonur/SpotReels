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
) {
    init {
        require(id.isNotBlank()) { "id must not be blank" }
        require(reelId.isNotBlank()) { "reelId must not be blank" }
        require(authorUid.isNotBlank()) { "authorUid must not be blank" }
        require(text.isNotBlank()) { "text must not be blank" }
        require(text.length <= MAX_TEXT_LENGTH) { "comment exceeds maximum length" }
        require(createdAtMillis >= 0) { "createdAtMillis must be non-negative" }
        require(updatedAtMillis == null || updatedAtMillis >= 0) { "updatedAtMillis must be non-negative" }
        require(parentCommentId == null || parentCommentId.isNotBlank()) { "parentCommentId must not be blank" }
    }

    companion object {
        const val MAX_TEXT_LENGTH = 2_000
    }
}

enum class CommentStatus {
    VISIBLE,
    HIDDEN,
    REMOVED
}
