package com.enak.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

data class LearningVideo(val id: String, val title: String, val category: String)

object VideoCatalog {
    val LEARNING_VIDEOS = listOf(
        LearningVideo("j5VtrvjA_LM", "Learn AI in 15 minutes", "Artificial intelligence"),
        LearningVideo("m-OCMzN4trc", "English learning session", "Language"),
        LearningVideo("BGXGdUj93BM", "How to start fitness", "Fitness"),
        LearningVideo("-4e3ewcTupM", "Web developer roadmap", "Development")
    )
}

class WarningsTest {
    @Test
    fun sessionWarningRules_warnsTwiceAndNeverAThirdTime() {
        assertTrue(Warnings.warningDue(900, 15, 0, 0L, 100_000L, false))
        assertTrue(Warnings.warningDue(960, 15, 1, 100_000L, 160_000L, false))
        assertFalse(Warnings.warningDue(1200, 15, 2, 160_000L, 220_000L, true))
    }

    @Test
    fun sessionWarningRules_waitsAMinuteBetweenWarnings() {
        assertFalse(Warnings.warningDue(959, 15, 1, 100_000L, 159_999L, false))
    }

    @Test
    fun sessionWarningRules_warnsOnDriftButNotBeforeAnOnPurposePlanEnds() {
        assertFalse(Warnings.warningDue(100, 15, 0, 0L, 100_000L, false))
        assertTrue(Warnings.warningDue(100, 15, 0, 0L, 100_000L, true))
    }

    @Test
    fun sessionWarningRules_keepsOpenEndedSessionsOpenUnlessDrifting() {
        assertFalse(Warnings.warningDue(37_000, 600, 0, 0L, 100_000L, false))
    }

    @Test
    fun includesExactlyTheFourSuppliedYouTubeVideos() {
        assertEquals(
            listOf("j5VtrvjA_LM", "m-OCMzN4trc", "BGXGdUj93BM", "-4e3ewcTupM"),
            VideoCatalog.LEARNING_VIDEOS.map { it.id }
        )
    }
}
