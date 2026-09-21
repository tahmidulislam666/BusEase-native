package com.example.busease

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.DirectionsBus
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.example.busease.ui.screens.BusDetailsScreen
import com.example.busease.ui.screens.BusFareSearchScreen
import com.example.busease.ui.screens.BusNameSearchScreen
import com.example.busease.ui.screens.FavoriteBusesScreen
import com.example.busease.ui.screens.RouteSearchScreen
import com.example.busease.ui.theme.DarkColorScheme
import com.example.busease.ui.theme.LightColorScheme

enum class AppTab(
    val title: String,
    val activeIcon: ImageVector,
    val inactiveIcon: ImageVector
) {
    ROUTE_SEARCH("Route", Icons.Filled.DirectionsBus, Icons.Outlined.DirectionsBus),
    BUS_SEARCH("Buses", Icons.Filled.Search, Icons.Outlined.Search),
    FARE_SEARCH("Fare", Icons.Filled.Payments, Icons.Outlined.Payments),
    FAVORITES("Saved", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val darkTheme = isSystemInDarkTheme()
            MaterialTheme(
                colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
            ) {
                BusEaseApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusEaseApp() {
    var selectedTab by remember { mutableStateOf(AppTab.BUS_SEARCH) }
    var selectedBusDetailsName by remember { mutableStateOf<String?>(null) }

    if (selectedBusDetailsName != null) {
        BusDetailsScreen(
            busId = selectedBusDetailsName!!,
            onBack = { selectedBusDetailsName = null }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
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
                        val isSelected = selectedTab == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { selectedTab = tab },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) tab.activeIcon else tab.inactiveIcon,
                                    contentDescription = tab.title
                                )
                            },
                            label = {
                                Text(
                                    text = tab.title,
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
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "tab_transition"
                ) { targetTab ->
                    when (targetTab) {
                        AppTab.ROUTE_SEARCH -> RouteSearchScreen(
                            onBusClick = { busName -> selectedBusDetailsName = busName }
                        )
                        AppTab.BUS_SEARCH -> BusNameSearchScreen(
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
