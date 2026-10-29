package com.sonu.spotreels.domain.model

/**
 * Public/application profile metadata. Firebase Authentication remains the
 * source of truth for identity and credentials; never store passwords here.
 */
data class UserProfile(
    val uid: String,
    val handle: String,
    val displayName: String,
    val bio: String = "",
    val avatarStoragePath: String? = null,
    val visibility: ProfileVisibility = ProfileVisibility.PUBLIC,
    val status: AccountStatus = AccountStatus.ACTIVE,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
    val followerCount: Long = 0,
    val followingCount: Long = 0,
    val reelCount: Long = 0
) {
    init {
        require(uid.isNotBlank()) { "uid must not be blank" }
        require(handle.isNotBlank()) { "handle must not be blank" }
        require(displayName.isNotBlank()) { "displayName must not be blank" }
        require(createdAtMillis >= 0 && updatedAtMillis >= 0) { "timestamps must be non-negative" }
        require(followerCount >= 0 && followingCount >= 0 && reelCount >= 0) {
            "profile counts must be non-negative"
        }
    }
}

enum class ProfileVisibility {
    PUBLIC,
    PRIVATE
}

enum class AccountStatus {
    ACTIVE,
    DEACTIVATION_PENDING,
    SUSPENDED,
    DELETED
}
