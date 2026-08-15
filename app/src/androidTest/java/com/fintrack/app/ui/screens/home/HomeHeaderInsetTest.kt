package com.fintrack.app.ui.screens.home

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fintrack.app.MainActivity
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeHeaderInsetTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun greetingAppearsDirectlyBelowTheSystemStatusBar() {
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule
                .onAllNodesWithText("FinTrack Dashboard")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        composeRule.onNodeWithText("Xin chào 👋").assertExists()
        val greetingTop = composeRule
            .onNodeWithText("Xin chào 👋")
            .fetchSemanticsNode()
            .boundsInRoot
            .top
        val maximumTop = with(composeRule.density) { 72.dp.toPx() }

        assertTrue(
            "Greeting should not be offset by a second status-bar inset: top=$greetingTop",
            greetingTop < maximumTop
        )
    }
}
