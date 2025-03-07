package com.example.myapplication.ui.screens

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@Composable
fun MapScreen(navController: NavController = rememberNavController()) {
    BasicScreen(title = "地圖", navController = navController)
} 