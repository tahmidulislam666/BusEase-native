package com.example.busease

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.DirectionsBus
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.busease.data.AppLanguage
import com.example.busease.data.AppSettings
import com.example.busease.data.AppStrings
import com.example.busease.ui.screens.BusDetailsScreen
import com.example.busease.ui.screens.BusFareSearchScreen
import com.example.busease.ui.screens.BusNameSearchScreen
import com.example.busease.ui.screens.FavoriteBusesScreen
import com.example.busease.ui.screens.NearbyBusesScreen
import com.example.busease.ui.screens.RouteSearchScreen
import com.example.busease.ui.screens.SettingsScreen
import com.example.busease.ui.theme.DarkColorScheme
import com.example.busease.ui.theme.LightColorScheme

enum class AppTab(
    val activeIcon: ImageVector,
    val inactiveIcon: ImageVector
) {
    ROUTE_SEARCH(Icons.Filled.DirectionsBus, Icons.Outlined.DirectionsBus),
    BUS_SEARCH(Icons.Filled.Search, Icons.Outlined.Search),
    NEARBY(Icons.Filled.MyLocation, Icons.Outlined.MyLocation),
    FARE_SEARCH(Icons.Filled.Payments, Icons.Outlined.Payments),
    FAVORITES(Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder);

    fun getTitle(lang: AppLanguage): String = when (this) {
        ROUTE_SEARCH -> AppStrings.routeTab(lang)
        BUS_SEARCH -> AppStrings.busesTab(lang)
        NEARBY -> AppStrings.nearbyTab(lang)
        FARE_SEARCH -> AppStrings.fareTab(lang)
        FAVORITES -> AppStrings.savedTab(lang)
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val appSettings = remember { AppSettings.getInstance(this@MainActivity) }
            // Do NOT use system dark mode automatically; user controls dark mode explicitly via Settings
            val isDark = appSettings.isDarkMode
            MaterialTheme(
                colorScheme = if (isDark) DarkColorScheme else LightColorScheme
            ) {
                BusEaseApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusEaseApp() {
    val context = LocalContext.current
    val appSettings = remember { AppSettings.getInstance(context) }
    val currentLang = appSettings.appLanguage

    var selectedTab by remember { mutableStateOf(AppTab.BUS_SEARCH) }
    var selectedBusDetailsName by remember { mutableStateOf<String?>(null) }
    var showingSettings by remember { mutableStateOf(false) }

    AnimatedContent(
        targetState = selectedBusDetailsName,
        transitionSpec = {
            if (targetState != null) {
                // Navigating Forward to Bus Route Details:
                // Detail view slides up/in from right with smooth fade in
                (slideInHorizontally(
                    initialOffsetX = { fullWidth -> (fullWidth * 0.25f).toInt() },
                    animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
                ) + fadeIn(
                    animationSpec = tween(durationMillis = 280)
                )) togetherWith (slideOutHorizontally(
                    targetOffsetX = { fullWidth -> -(fullWidth * 0.15f).toInt() },
                    animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                ) + fadeOut(
                    animationSpec = tween(durationMillis = 220)
                ))
            } else {
                // Navigating Backward to Bus Route List:
                // List view slides back from left with smooth fade in
                (slideInHorizontally(
                    initialOffsetX = { fullWidth -> -(fullWidth * 0.15f).toInt() },
                    animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                ) + fadeIn(
                    animationSpec = tween(durationMillis = 280)
                )) togetherWith (slideOutHorizontally(
                    targetOffsetX = { fullWidth -> (fullWidth * 0.25f).toInt() },
                    animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
                ) + fadeOut(
                    animationSpec = tween(durationMillis = 220)
                ))
            }
        },
        label = "bus_details_transition"
    ) { busDetailsName ->
        if (busDetailsName != null) {
            BusDetailsScreen(
                busId = busDetailsName,
                onBack = { selectedBusDetailsName = null }
            )
        } else {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = AppStrings.appTitle(currentLang),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        },
                        actions = {
                            // Quick 1-tap language switch toggle (ENG <-> বাংলা)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                                    .clickable {
                                        val nextLang = if (currentLang == AppLanguage.ENGLISH) AppLanguage.BANGLA else AppLanguage.ENGLISH
                                        appSettings.updateLanguage(nextLang)
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                    .testTag("top_bar_language_toggle"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (currentLang == AppLanguage.ENGLISH) "বাংলা" else "ENG",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            // Settings shortcut button
                            IconButton(
                                onClick = { showingSettings = true },
                                modifier = Modifier.testTag("top_bar_settings_button")
                            ) {
                                Icon(
                                    imageVector = if (showingSettings) Icons.Filled.Settings else Icons.Outlined.Settings,
                                    contentDescription = AppStrings.settingsTab(currentLang),
                                    tint = if (showingSettings) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.testTag("app_top_bar")
                    )
                },
                bottomBar = {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.testTag("bottom_nav_bar")
                    ) {
                        AppTab.entries.forEach { tab ->
                            val isSelected = !showingSettings && selectedTab == tab
                            val tabTitle = tab.getTitle(currentLang)
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    showingSettings = false
                                    selectedTab = tab
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (isSelected) tab.activeIcon else tab.inactiveIcon,
                                        contentDescription = tabTitle
                                    )
                                },
                                label = {
                                    Text(
                                        text = tabTitle,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    selectedTextColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                            )
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (showingSettings) {
                        SettingsScreen(
                            onBack = { showingSettings = false }
                        )
                    } else {
                        AnimatedContent(
                            targetState = selectedTab,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(220, easing = LinearOutSlowInEasing)) togetherWith
                                        fadeOut(animationSpec = tween(180, easing = FastOutSlowInEasing))
                            },
                            label = "tab_transition"
                        ) { targetTab ->
                            when (targetTab) {
                                AppTab.ROUTE_SEARCH -> RouteSearchScreen(
                                    onBusClick = { busName -> selectedBusDetailsName = busName }
                                )
                                AppTab.BUS_SEARCH -> BusNameSearchScreen(
                                    onBusClick = { busName -> selectedBusDetailsName = busName }
                                )
                                AppTab.NEARBY -> NearbyBusesScreen(
                                    onBusClick = { busName -> selectedBusDetailsName = busName }
                                )
                                AppTab.FARE_SEARCH -> BusFareSearchScreen()
                                AppTab.FAVORITES -> FavoriteBusesScreen(
                                    onBusClick = { busName -> selectedBusDetailsName = busName }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
