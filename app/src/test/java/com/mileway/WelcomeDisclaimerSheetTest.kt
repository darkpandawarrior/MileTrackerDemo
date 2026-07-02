package com.mileway

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mileway.core.ui.theme.MilewayTheme
import com.mileway.feature.profile.repository.MockAccountRepository
import com.mileway.ui.auth.AuthViewModel
import com.mileway.ui.auth.LoginScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import kotlin.test.assertEquals

/**
 * PLAN_V22 P7.5: behavioural tests for [LoginScreen]'s [com.mileway.ui.auth.WelcomeDisclaimerSheet]
 * integration — covers the four acceptance-clause behaviors: the sheet shows exactly once on a
 * fresh session, "Not now" proceeds without blocking sign-in, "Continue" dismisses the sheet
 * (handing off to the real system permission dialog), and a sign-in tap made while the sheet is
 * showing resumes automatically once it's dismissed.
 *
 * [AuthViewModel] is constructed directly (mirroring [AuthViewModelTest]'s fakes) rather than
 * resolved through Koin, so these tests don't depend on Koin's `ViewModelStore` timing in a
 * Robolectric host.
 *
 * `qualifiers` pins a realistic phone viewport (matching [ScreenshotGalleryTest]'s convention) —
 * without it Robolectric's default ~320x470dp window is too short for [LoginScreen]'s scrolling
 * layout, leaving "Continue as guest" laid out with zero-size bounds ([performScrollTo] before
 * the click is still required so the node is actually within the (scrollable) viewport).
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [33], application = Application::class, qualifiers = "w411dp-h891dp-mdpi")
class WelcomeDisclaimerSheetTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun buildAuthViewModel() =
        AuthViewModel(
            mockAccountRepository = MockAccountRepository(FakeMockAccountDao()),
            activeAccountSource = FakeActiveAccountSource(),
        )

    @Test
    fun `sheet shows on first composition when not yet shown`() {
        composeRule.setContent {
            MilewayTheme {
                LoginScreen(
                    onSignInWithCredentials = {},
                    onContinueAsGuest = {},
                    hasShownWelcomeDisclaimer = false,
                    authViewModel = buildAuthViewModel(),
                )
            }
        }

        composeRule.onNodeWithText("Before you start tracking").assertExists()
    }

    @Test
    fun `sheet does not show once already shown`() {
        composeRule.setContent {
            MilewayTheme {
                LoginScreen(
                    onSignInWithCredentials = {},
                    onContinueAsGuest = {},
                    hasShownWelcomeDisclaimer = true,
                    authViewModel = buildAuthViewModel(),
                )
            }
        }

        composeRule.onNodeWithText("Before you start tracking").assertDoesNotExist()
    }

    @Test
    fun `dismissing with Not now proceeds without blocking a subsequent guest tap`() {
        var shownCallbackCount = 0

        composeRule.setContent {
            MilewayTheme {
                LoginScreen(
                    onSignInWithCredentials = {},
                    onContinueAsGuest = {},
                    hasShownWelcomeDisclaimer = false,
                    onWelcomeDisclaimerShown = { shownCallbackCount++ },
                    authViewModel = buildAuthViewModel(),
                )
            }
        }

        composeRule.onNodeWithText("Not now").performClick()
        composeRule.onNodeWithText("Before you start tracking").assertDoesNotExist()
        assertEquals(1, shownCallbackCount)

        // Sign-in still works normally after the sheet is dismissed (non-blocking): the staged
        // sign-in sequence starts immediately, evidenced by the button flipping to its loading label.
        composeRule.onNodeWithText("Continue as guest").performScrollTo().performClick()
        composeRule.onNodeWithText("Sign In").assertDoesNotExist()
        composeRule.onNodeWithText("Signing in…").assertExists()
    }

    @Test
    fun `a sign-in tap made while the sheet is showing resumes once it is dismissed`() {
        composeRule.setContent {
            MilewayTheme {
                LoginScreen(
                    onSignInWithCredentials = {},
                    onContinueAsGuest = {},
                    hasShownWelcomeDisclaimer = false,
                    authViewModel = buildAuthViewModel(),
                )
            }
        }

        // Tap "Continue as guest" while the disclaimer sheet is still up — it must queue, not fire
        // (the staged sign-in sequence has not started, so the button still reads "Sign In").
        composeRule.onNodeWithText("Continue as guest").performScrollTo().performClick()
        composeRule.onNodeWithText("Signing in…").assertDoesNotExist()

        // Dismissing resumes the queued guest sign-in automatically — the staged sequence now starts.
        composeRule.onNodeWithText("Not now").performClick()
        composeRule.onNodeWithText("Signing in…").assertExists()
    }

    @Test
    fun `tapping Continue dismisses the sheet`() {
        composeRule.setContent {
            MilewayTheme {
                LoginScreen(
                    onSignInWithCredentials = {},
                    onContinueAsGuest = {},
                    hasShownWelcomeDisclaimer = false,
                    authViewModel = buildAuthViewModel(),
                )
            }
        }

        composeRule.onNodeWithText("Continue").performClick()
        composeRule.onNodeWithText("Before you start tracking").assertDoesNotExist()
    }
}
