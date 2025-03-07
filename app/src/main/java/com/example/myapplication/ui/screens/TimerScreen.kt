package com.example.myapplication.ui.screens

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@Composable
fun TimerScreen(navController: NavController = rememberNavController()) {
    BasicScreen(title = "定時", navController = navController)
} 