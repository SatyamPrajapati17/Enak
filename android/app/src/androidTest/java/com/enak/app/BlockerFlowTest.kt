package com.enak.app

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.enak.tracking.BlockerScreen
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BlockerFlowTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun blockerFlow_showsWarningAndCloseAction() {
        var closed = false

        composeTestRule.setContent {
            BlockerScreen(
                packageName = "com.instagram.android",
                warningNumber = 1,
                isHardBlock = false,
                onCloseApp = { closed = true },
                onDismissWarning = {}
            )
        }

        // Verify warning badge and title
        composeTestRule.onNodeWithText("Mindful Warning 1 of 2").assertIsDisplayed()
        composeTestRule.onNodeWithText("Is this what you came for?").assertIsDisplayed()

        // Verify Close button is present and clickable
        val closeBtn = composeTestRule.onNodeWithText("Close App & Step Away")
        closeBtn.assertIsDisplayed()
        closeBtn.performClick()

        assertTrue("Expected onCloseApp to be invoked", closed)
    }

    @Test
    fun blockerFlow_hardBlockPreventsNegotiation() {
        composeTestRule.setContent {
            BlockerScreen(
                packageName = "com.instagram.android",
                warningNumber = 2,
                isHardBlock = true,
                onCloseApp = {},
                onDismissWarning = {}
            )
        }

        // Hard block label
        composeTestRule.onNodeWithText("Hard Daily Limit Reached").assertIsDisplayed()
        composeTestRule.onNodeWithText("Time to step away.").assertIsDisplayed()

        // Negotiation button should NOT exist on hard block
        composeTestRule.onNodeWithText("Ask AI Coach for 5 more minutes").assertDoesNotExist()
    }
}
