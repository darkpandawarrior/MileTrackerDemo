package com.mileway.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mileway.core.ui.components.DotsIndicator
import com.mileway.core.ui.theme.DesignTokens
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel

/** Prefilled demo identity so the fake login works without typing anything. */
private const val DEMO_EMAIL = "demo@mileway.app"

/** App name shown beside the logo mark. */
private const val APP_NAME = "Mileway"

/** How long each onboarding slide is shown before auto-advancing. */
private const val ONBOARDING_AUTO_ADVANCE_MS = 2_600L

/** Brief pause after [MilewayAuthState.Success] so the completed checklist is visible. */
private const val POST_SUCCESS_PAUSE_MS = 250L

/**
 * A single onboarding slide: a simple drawn illustration (Material icon on a tinted disc),
 * a short title, and a supporting caption about tracking miles offline.
 */
private data class OnboardingSlide(
    val icon: ImageVector,
    val title: String,
    val caption: String,
)

/** The three onboarding slides cycled by the welcome pager. */
private val ONBOARDING_SLIDES = listOf(
    OnboardingSlide(
        icon = Icons.Filled.Explore,
        title = "Track every mile",
        caption = "Start a trip and Mileway records your route, distance, and duration automatically.",
    ),
    OnboardingSlide(
        icon = Icons.Filled.CloudOff,
        title = "Works fully offline",
        caption = "No signal, no problem. Trips are saved on your device and are ready whenever you are.",
    ),
    OnboardingSlide(
        icon = Icons.Filled.LocationOn,
        title = "Your data stays put",
        caption = "Everything lives locally on your phone: capture mileage anywhere, sync nothing.",
    ),
)

/**
 * Polished fake login screen for the demo. No real authentication happens: any non-empty
 * email and password are accepted, the prefilled [DEMO_EMAIL] hint lets a reviewer sign in
 * with one tap, and the guest button skips credentials entirely.
 *
 * Layout, top to bottom:
 * - app mark + name header,
 * - a 3-page auto-advancing onboarding [HorizontalPager] with drawn illustrations and captions,
 *   plus a shared [DotsIndicator],
 * - email + password [OutlinedTextField]s (non-empty validation only),
 * - a full-width "Sign In" button with a brief fake-loading state,
 * - a "Continue as guest" text button.
 *
 * The screen owns its insets directly: the scrolling [Column] applies [statusBarsPadding] and
 * [imePadding] so fields lift above the keyboard, and the pinned bottom actions add
 * [navigationBarsPadding]. It is hosted full-screen by the integrator (no bottom bar), so it is
 * stateless apart from internal field/loading/animation state, no ViewModel is required.
 *
 * @param onSignInWithCredentials invoked with the entered email once a credentials sign-in
 *   completes. Captured via [rememberUpdatedState] so the fake-loading coroutine always calls the
 *   latest lambda.
 * @param onContinueAsGuest invoked when the user chooses the guest path. The integrator persists
 *   the session so it survives navigation, deep links and process recreation.
 */
@Composable
fun LoginScreen(
    onSignInWithCredentials: (email: String) -> Unit,
    onContinueAsGuest: () -> Unit,
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = koinViewModel(),
) {
    val currentOnCredentials by rememberUpdatedState(onSignInWithCredentials)
    val currentOnGuest by rememberUpdatedState(onContinueAsGuest)

    var email by remember { mutableStateOf(DEMO_EMAIL) }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isGuestPath by remember { mutableStateOf(false) }
    var attemptedSubmit by remember { mutableStateOf(false) }

    val authState by authViewModel.state.collectAsStateWithLifecycle()
    val isSigningIn = authState !is MilewayAuthState.Idle

    val emailValid = email.isNotBlank()
    val passwordValid = password.isNotBlank()
    val canSubmit = emailValid && passwordValid && !isSigningIn

    // P7.2: staged sign-in. AuthViewModel steps MilewayAuthState through named Loading stages;
    // once it reports Success, report completion down the path the user actually took (guest vs
    // credentials) so the session is persisted with the right kind, then reset back to Idle.
    LaunchedEffect(authState) {
        if (authState is MilewayAuthState.Success) {
            delay(POST_SUCCESS_PAUSE_MS)
            if (isGuestPath) currentOnGuest() else currentOnCredentials(email)
            authViewModel.reset()
        }
    }

    val scrollState = rememberScrollState()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .statusBarsPadding()
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = DesignTokens.Spacing.xl),
        ) {
            Spacer(Modifier.height(DesignTokens.Spacing.xl))

            BrandHeader()

            Spacer(Modifier.height(DesignTokens.Spacing.xl))

            OnboardingPager()

            Spacer(Modifier.height(DesignTokens.Spacing.xl))

            CredentialFields(
                email = email,
                onEmailChange = { email = it },
                password = password,
                onPasswordChange = { password = it },
                passwordVisible = passwordVisible,
                onTogglePasswordVisible = { passwordVisible = !passwordVisible },
                emailError = attemptedSubmit && !emailValid,
                passwordError = attemptedSubmit && !passwordValid,
                enabled = !isSigningIn,
                onImeDone = {
                    attemptedSubmit = true
                    if (emailValid && passwordValid) authViewModel.beginSignIn()
                },
            )

            Spacer(Modifier.height(DesignTokens.Spacing.xl))

            Button(
                onClick = {
                    attemptedSubmit = true
                    if (emailValid && passwordValid) authViewModel.beginSignIn()
                },
                enabled = canSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = DesignTokens.Shape.roundedMd,
            ) {
                if (isSigningIn) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                    Spacer(Modifier.width(DesignTokens.Spacing.m))
                    Text("Signing in…", fontWeight = FontWeight.SemiBold)
                } else {
                    Text("Sign In", fontWeight = FontWeight.SemiBold)
                }
            }

            AnimatedVisibility(visible = isSigningIn) {
                Column {
                    Spacer(Modifier.height(DesignTokens.Spacing.l))
                    SignInProgressOverlay(state = authState)
                }
            }

            Spacer(Modifier.height(DesignTokens.Spacing.s))

            TextButton(
                onClick = {
                    if (!isSigningIn) {
                        isGuestPath = true
                        authViewModel.beginSignIn()
                    }
                },
                enabled = !isSigningIn,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Continue as guest")
            }

            Spacer(Modifier.height(DesignTokens.Spacing.l))
        }
    }
}

/**
 * Mileway's own stepped-progress affordance for the staged sign-in sequence: a vertical
 * checklist of [SIGN_IN_STEPS], each row showing a filled dot for pending, a spinner for the
 * currently active step, and an animated checkmark once that step completes. Models the
 * reference app's staged-overlay *pattern* (a driven multi-step sequence rather than one flat
 * spinner) with Mileway's own card-and-checklist visual language, not a copy of its UI.
 */
@Composable
private fun SignInProgressOverlay(state: MilewayAuthState) {
    val activeStep = (state as? MilewayAuthState.Loading)?.step ?: 0
    val isComplete = state is MilewayAuthState.Success

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = DesignTokens.Shape.roundedMd,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Column(modifier = Modifier.padding(DesignTokens.Spacing.l)) {
            SIGN_IN_STEPS.forEachIndexed { index, step ->
                val stepNumber = index + 1
                val completed = isComplete || stepNumber < activeStep
                val active = !isComplete && stepNumber == activeStep
                SignInStepRow(label = step.label, completed = completed, active = active)
                if (stepNumber != SIGN_IN_STEPS.size) {
                    Spacer(Modifier.height(DesignTokens.Spacing.s))
                }
            }
        }
    }
}

/** One row of [SignInProgressOverlay]'s checklist: a leading status glyph plus the step label. */
@Composable
private fun SignInStepRow(
    label: String,
    completed: Boolean,
    active: Boolean,
) {
    val checkAlpha by animateFloatAsState(
        targetValue = if (completed) 1f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "signInStepCheck",
    )

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(DesignTokens.IconSize.badge),
            contentAlignment = Alignment.Center,
        ) {
            when {
                completed ->
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        modifier = Modifier
                            .size(DesignTokens.IconSize.badge)
                            .alpha(checkAlpha),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                active ->
                    CircularProgressIndicator(
                        modifier = Modifier.size(DesignTokens.IconSize.inline),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                    )
                else ->
                    Surface(
                        modifier = Modifier.size(8.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.outlineVariant,
                    ) {}
            }
        }

        Spacer(Modifier.width(DesignTokens.Spacing.m))

        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (active || completed) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
    }
}

/** App mark + name, left-aligned at the top of the screen. */
@Composable
private fun BrandHeader() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        CompassMark(modifier = Modifier.size(40.dp))
        Spacer(Modifier.width(DesignTokens.Spacing.m))
        Column {
            Text(
                text = APP_NAME,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = "Mileage tracking, offline-first",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * Auto-advancing welcome carousel. Each slide is a drawn illustration plus copy; the shared
 * [DotsIndicator] tracks the current page. The pager advances on a timer keyed to the current
 * page, so a manual swipe resets the countdown naturally.
 */
@Composable
private fun OnboardingPager() {
    val pagerState = rememberPagerState(pageCount = { ONBOARDING_SLIDES.size })

    LaunchedEffect(pagerState.currentPage) {
        delay(ONBOARDING_AUTO_ADVANCE_MS)
        val next = (pagerState.currentPage + 1) % ONBOARDING_SLIDES.size
        pagerState.animateScrollToPage(next)
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = DesignTokens.Spacing.xs),
            modifier = Modifier.fillMaxWidth(),
        ) { page ->
            OnboardingSlideContent(slide = ONBOARDING_SLIDES[page])
        }

        Spacer(Modifier.height(DesignTokens.Spacing.l))

        DotsIndicator(
            pageCount = ONBOARDING_SLIDES.size,
            selectedIndex = pagerState.currentPage,
        )
    }
}

/** A single onboarding slide: tinted icon disc, title, and caption. */
@Composable
private fun OnboardingSlideContent(slide: OnboardingSlide) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DesignTokens.Spacing.s),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Surface(
            modifier = Modifier.size(120.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = slide.icon,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }

        Spacer(Modifier.height(DesignTokens.Spacing.l))

        Text(
            text = slide.title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(DesignTokens.Spacing.s))

        Text(
            text = slide.caption,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(0.9f),
        )
    }
}

/**
 * Email + password fields. Validation is non-empty only; errors surface only after the user
 * has attempted to submit (driven by the [emailError]/[passwordError] flags) to avoid shouting
 * at an untouched form.
 */
@Composable
private fun CredentialFields(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onTogglePasswordVisible: () -> Unit,
    emailError: Boolean,
    passwordError: Boolean,
    enabled: Boolean,
    onImeDone: () -> Unit,
) {
    Column {
        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            singleLine = true,
            isError = emailError,
            label = { Text("Email") },
            placeholder = { Text(DEMO_EMAIL) },
            leadingIcon = {
                Icon(Icons.Outlined.MailOutline, contentDescription = null)
            },
            supportingText = if (emailError) {
                { Text("Enter your email to continue") }
            } else {
                null
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
            ),
            shape = DesignTokens.Shape.roundedMd,
        )

        Spacer(Modifier.height(DesignTokens.Spacing.m))

        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            singleLine = true,
            isError = passwordError,
            label = { Text("Password") },
            placeholder = { Text("Any password works") },
            leadingIcon = {
                Icon(Icons.Outlined.Lock, contentDescription = null)
            },
            trailingIcon = {
                IconButton(onClick = onTogglePasswordVisible) {
                    Icon(
                        imageVector = if (passwordVisible) {
                            Icons.Filled.VisibilityOff
                        } else {
                            Icons.Filled.Visibility
                        },
                        contentDescription = if (passwordVisible) {
                            "Hide password"
                        } else {
                            "Show password"
                        },
                    )
                }
            },
            visualTransformation = if (passwordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            supportingText = if (passwordError) {
                { Text("Enter any password to continue") }
            } else {
                null
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onDone = { onImeDone() }),
            shape = DesignTokens.Shape.roundedMd,
        )
    }
}
