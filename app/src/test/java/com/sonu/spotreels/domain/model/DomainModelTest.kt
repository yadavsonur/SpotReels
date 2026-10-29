package com.sonu.spotreels.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class DomainModelTest {

    @Test
    fun reelDefaultsToDraftAndRejectsNegativeCounters() {
        val reel = Reel(
            id = "reel-1",
            ownerUid = "user-1",
            caption = "A test reel",
            media = ReelMedia(
                bucket = "videos",
                objectPath = "uploads/reel-1.mp4",
                mimeType = "video/mp4",
                sizeBytes = 128,
                durationMillis = 1_500
            ),
            createdAtMillis = 100,
            updatedAtMillis = 100
        )

        assertEquals(ReelStatus.DRAFT, reel.status)
        assertEquals(ReelVisibility.PUBLIC, reel.visibility)
        assertThrows(IllegalArgumentException::class.java) {
            reel.copy(likeCount = -1)
        }
    }

    @Test
    fun followRejectsSelfFollow() {
        assertThrows(IllegalArgumentException::class.java) {
            Follow(
                followerUid = "same-user",
                followedUid = "same-user",
                createdAtMillis = 100
            )
        }
    }

    @Test
    fun commentRejectsBlankAndOverlongText() {
        assertThrows(IllegalArgumentException::class.java) {
            Comment(
                id = "comment-1",
                reelId = "reel-1",
                authorUid = "user-1",
                text = "   ",
                createdAtMillis = 100
            )
        }

        assertThrows(IllegalArgumentException::class.java) {
            Comment(
                id = "comment-2",
                reelId = "reel-1",
                authorUid = "user-1",
                text = "x".repeat(Comment.MAX_TEXT_LENGTH + 1),
                createdAtMillis = 100
            )
        }
    }

    @Test
    fun mediaRejectsNegativeSize() {
        assertThrows(IllegalArgumentException::class.java) {
            ReelMedia(
                bucket = "videos",
                objectPath = "uploads/reel.mp4",
                mimeType = "video/mp4",
                sizeBytes = -1,
                durationMillis = 1
            )
        }
    }
}
