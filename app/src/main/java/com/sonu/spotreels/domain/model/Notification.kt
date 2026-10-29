package com.sonu.spotreels.domain.model

data class AppNotification(
    val id: String,
    val recipientUid: String,
    val type: NotificationType,
    val createdAtMillis: Long,
    val actorUid: String? = null,
    val reelId: String? = null,
    val commentId: String? = null,
    val readAtMillis: Long? = null,
    val actorDisplayNameSnapshot: String? = null
) {
    init {
        require(id.isNotBlank() && recipientUid.isNotBlank()) { "notification IDs must not be blank" }
        require(createdAtMillis >= 0) { "createdAtMillis must be non-negative" }
        require(readAtMillis == null || readAtMillis >= 0) { "readAtMillis must be non-negative" }
        require(actorUid == null || actorUid.isNotBlank()) { "actorUid must not be blank" }
        require(reelId == null || reelId.isNotBlank()) { "reelId must not be blank" }
        require(commentId == null || commentId.isNotBlank()) { "commentId must not be blank" }
    }
}

enum class NotificationType {
    NEW_FOLLOWER,
    REEL_LIKED,
    REEL_COMMENTED,
    COMMENT_REPLIED,
    SYSTEM
}
