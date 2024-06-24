package com.example.littlelemon

import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun Navigation(navController: NavHostController, sharedPreferences: SharedPreferences) {
    val userRegistered = remember { sharedPreferences.getBoolean("user_registered", false)}
    val startDestination = if (userRegistered) "home" else "onboarding"

    NavHost(navController = navController, startDestination =  startDestination) {
        composable("home") {Home(navController, sharedPreferences)}
        composable("profile") {Profile(navController, sharedPreferences)}
        composable("onboarding") { Onboarding(navController, sharedPreferences)}
    }
}