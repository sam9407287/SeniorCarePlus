package com.example.myapplication.ui.screens

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@Composable
fun MonitorScreen(navController: NavController = rememberNavController()) {
    BasicScreen(title = "監控", navController = navController)
} 