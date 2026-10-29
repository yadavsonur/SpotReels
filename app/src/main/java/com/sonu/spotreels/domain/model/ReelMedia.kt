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
) {
    init {
        require(bucket.isNotBlank()) { "bucket must not be blank" }
        require(objectPath.isNotBlank()) { "objectPath must not be blank" }
        require(mimeType.isNotBlank()) { "mimeType must not be blank" }
        require(sizeBytes >= 0) { "sizeBytes must be non-negative" }
        require(durationMillis >= 0) { "durationMillis must be non-negative" }
        require(widthPixels == null || widthPixels > 0) { "widthPixels must be positive when provided" }
        require(heightPixels == null || heightPixels > 0) { "heightPixels must be positive when provided" }
    }
}
