package com.sonu.spotreels.domain.model

data class ContentReport(
    val id: String,
    val reporterUid: String,
    val targetType: ReportTargetType,
    val targetId: String,
    val reason: ReportReason,
    val createdAtMillis: Long,
    val details: String? = null,
    val status: ReportStatus = ReportStatus.OPEN,
    val reviewedAtMillis: Long? = null,
    val reviewerUid: String? = null
)

enum class ReportTargetType {
    USER,
    REEL,
    COMMENT
}

enum class ReportReason {
    SPAM,
    HARASSMENT,
    HATE_OR_ABUSE,
    SEXUAL_CONTENT,
    VIOLENCE,
    COPYRIGHT,
    MISINFORMATION,
    OTHER
}

enum class ReportStatus {
    OPEN,
    IN_REVIEW,
    ACTIONED,
    DISMISSED
}
