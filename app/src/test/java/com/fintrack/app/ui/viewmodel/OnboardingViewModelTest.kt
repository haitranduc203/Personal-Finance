package com.fintrack.app.ui.viewmodel

import com.fintrack.app.data.local.preferences.UserPreferences
import com.fintrack.app.ui.screens.onboarding.OnboardingUiEvent
import com.fintrack.app.ui.screens.onboarding.OnboardingViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var fakePreferencesRepository: FakePreferencesRepository
    private lateinit var viewModel: OnboardingViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakePreferencesRepository = FakePreferencesRepository(
            initialPreferences = UserPreferences(isOnboardingCompleted = false)
        )
        viewModel = OnboardingViewModel(fakePreferencesRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun completeOnboarding_updatesPreferencesAndEmitsNavigateToHomeEvent() = testScope.runTest {
        val collectedEvents = mutableListOf<OnboardingUiEvent>()
        val job = launch {
            viewModel.events.collect { collectedEvents.add(it) }
        }

        viewModel.completeOnboarding()
        advanceUntilIdle()

        val updatedPrefs = fakePreferencesRepository.userPreferencesFlow.first()
        assertTrue(updatedPrefs.isOnboardingCompleted)
        assertEquals(1, collectedEvents.size)
        assertTrue(collectedEvents.first() is OnboardingUiEvent.NavigateToHome)

        job.cancel()
    }
}
