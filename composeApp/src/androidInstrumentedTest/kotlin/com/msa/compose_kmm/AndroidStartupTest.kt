package com.msa.compose_kmm

import android.content.Context
import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.accessibility.enableAccessibilityChecks
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.tryPerformAccessibilityChecks
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.msa.compose_kmm.data.AndroidGameSettingsStore
import com.msa.compose_kmm.data.AndroidGameStateStore
import com.msa.compose_kmm.domain.Game
import com.msa.compose_kmm.domain.GameSettings
import com.msa.compose_kmm.domain.SaveDurability
import com.msa.compose_kmm.ui.UiTestTags
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.junit.runners.model.Statement

@RunWith(AndroidJUnit4::class)
class AndroidStartupTest {
    private val composeRule = createAndroidComposeRule<MainActivity>()

    private val clearPersistedStateRule = TestRule { base, _ ->
        object : Statement() {
            override fun evaluate() {
                val context = ApplicationProvider.getApplicationContext<Context>()
                context.getSharedPreferences("msa_bee_state", Context.MODE_PRIVATE)
                    .edit()
                    .clear()
                    .commit()
                context.getSharedPreferences("msa_bee_settings", Context.MODE_PRIVATE)
                    .edit()
                    .clear()
                    .commit()
                base.evaluate()
            }
        }
    }

    @get:Rule
    val rules: RuleChain = RuleChain
        .outerRule(clearPersistedStateRule)
        .around(composeRule)

    @Test
    fun realApplicationStartsAndPrimaryFlowPassesAccessibilityChecks() {
        composeRule.enableAccessibilityChecks()

        composeRule.onNodeWithTag(UiTestTags.START_OVERLAY).assertIsDisplayed()
        composeRule.onRoot().tryPerformAccessibilityChecks()

        composeRule.onNodeWithTag(UiTestTags.START_BUTTON)
            .assertIsDisplayed()
            .performClick()

        composeRule.onNodeWithTag(UiTestTags.GAME_CANVAS).assertIsDisplayed()
        composeRule.onRoot().tryPerformAccessibilityChecks()
    }

    @Test
    fun activeGameRestoresAfterActivityRecreation() {
        composeRule.onNodeWithTag(UiTestTags.START_BUTTON)
            .assertIsDisplayed()
            .performClick()
        composeRule.onNodeWithTag(UiTestTags.GAME_CANVAS).assertIsDisplayed()

        composeRule.activityRule.scenario.recreate()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(UiTestTags.GAME_CANVAS).assertIsDisplayed()
        composeRule.onNodeWithTag(UiTestTags.START_OVERLAY).assertDoesNotExist()
    }

    @Test
    fun androidStateStoreRoundTripsACompleteSnapshot() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val store = AndroidGameStateStore(context)
        val game = Game(Random(7)).apply {
            start()
            jump()
            repeat(25) { updateNanos(8_333_333L) }
        }

        assertTrue(store.save(game.snapshot(), SaveDurability.Immediate))

        assertNotNull(store.load())
        assertEquals(game.snapshot(), store.load())
        assertTrue(store.clear(SaveDurability.Immediate))
        assertEquals(null, store.load())
    }
    @Test
    fun settingsOpenFromStartAndMasterSoundCanBeChanged() {
        composeRule.onNodeWithTag(UiTestTags.SETTINGS_BUTTON)
            .assertIsDisplayed()
            .performClick()
        composeRule.onNodeWithTag(UiTestTags.SETTINGS_OVERLAY).assertIsDisplayed()
        composeRule.onNodeWithTag(UiTestTags.SOUND_TOGGLE)
            .assertIsDisplayed()
            .performClick()
        composeRule.onNodeWithTag(UiTestTags.SETTINGS_DONE)
            .assertIsDisplayed()
            .performClick()
        composeRule.onNodeWithTag(UiTestTags.START_OVERLAY).assertIsDisplayed()
    }

    @Test
    fun androidSettingsStoreRoundTripsPreferences() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val store = AndroidGameSettingsStore(context)
        val settings = GameSettings(
            soundEnabled = false,
            musicVolume = 37,
            effectsVolume = 79,
            reduceMotion = true,
            showGameplayHints = false
        )

        assertTrue(store.save(settings))
        assertEquals(settings, store.load())
        assertTrue(store.clear())
        assertEquals(null, store.load())
    }

}
