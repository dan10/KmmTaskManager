package com.danioliveira.taskmanager.feature.auth.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.danioliveira.taskmanager.feature.auth.ui.login.LoginScreen
import com.danioliveira.taskmanager.feature.auth.ui.register.RegisterScreen
import kotlinx.serialization.Serializable

@Serializable data object LoginRoute

@Serializable data object RegisterRoute

@Serializable data object AuthBaseRoute

fun NavController.navigateToLogin(navOptions: NavOptions? = null) = navigate(LoginRoute, navOptions)

fun NavController.navigateToRegister(navOptions: NavOptions? = null) = navigate(RegisterRoute, navOptions)

/**
 * Adds the auth section to the navigation graph.
 * Includes login and register screens with slide animations.
 * - Login → Register: slide in from right
 * - Register → Login: slide out to right (back)
 */
fun NavGraphBuilder.authSection(
    onNavigateToRegister: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    navigation<AuthBaseRoute>(startDestination = LoginRoute) {
        composable<LoginRoute>(
            enterTransition = {
               slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            exitTransition = {
                // When going to Register, slide out to left
                slideOutHorizontally(
                    targetOffsetX = { -it },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            },
            popEnterTransition = {
                // When coming back to Login from Register (via back), slide in from left
                slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                // Not typically used for Login as it's the start destination
                slideOutHorizontally(
                    targetOffsetX = { -it },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            }
        ) {
            LoginScreen(
                navigateToRegister = onNavigateToRegister,
                navigateToHome = onNavigateToHome
            )
        }

        composable<RegisterRoute>(
            enterTransition = {
                // When coming from Login, slide in from right
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                // When going forward from Register (to home)
                slideOutHorizontally(
                    targetOffsetX = { -it },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            },
            popEnterTransition = {
                // When returning to Register (not typical)
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                // When going back to Login via back button, slide out to right
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            }
        ) {
            RegisterScreen(
                navigateToLogin = onNavigateToLogin,
                navigateToHome = onNavigateToHome
            )
        }
    }
}
