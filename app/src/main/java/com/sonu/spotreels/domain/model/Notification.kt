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
)

enum class NotificationType {
    NEW_FOLLOWER,
    REEL_LIKED,
    REEL_COMMENTED,
    COMMENT_REPLIED,
    SYSTEM
}
