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
)

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
