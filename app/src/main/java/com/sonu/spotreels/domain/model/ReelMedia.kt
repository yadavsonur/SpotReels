package com.sonu.spotreels.domain.model

/**
 * Durable reference and technical metadata for media stored outside Firestore.
 * Prefer bucket + object path over persisting only a public playback URL.
 */
data class ReelMedia(
    val bucket: String,
    val objectPath: String,
    val mimeType: String,
    val sizeBytes: Long,
    val durationMillis: Long,
    val widthPixels: Int? = null,
    val heightPixels: Int? = null,
    val thumbnailObjectPath: String? = null
)
