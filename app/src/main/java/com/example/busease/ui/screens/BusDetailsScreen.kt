package com.example.busease.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LinearScale
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.busease.R
import com.example.busease.data.AppLanguage
import com.example.busease.data.AppSettings
import com.example.busease.data.AppStrings
import com.example.busease.data.model.BusRoute
import com.example.busease.data.repository.BusRepository
import com.example.busease.data.repository.RouteFareEstimate
import com.example.busease.ui.components.GoogleMapsRouteView
import com.example.busease.ui.components.RouteCoverageMap
import kotlinx.coroutines.launch
import android.content.Intent
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Polyline
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusDetailsScreen(
    busId: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { BusRepository.getInstance(context) }
    val appSettings = remember { AppSettings.getInstance(context) }
    val lang = appSettings.appLanguage
    val scope = rememberCoroutineScope()

    var bus by remember { mutableStateOf<BusRoute?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isReverseRoute by remember { mutableStateOf(false) }
    var selectedMapTab by remember { mutableStateOf(1) } // Default to 1: Fast Native Vector Route Map, 0: Google Maps

    // Route-specific Fare Estimation Tool state
    var fareStartStop by remember { mutableStateOf("") }
    var fareEndStop by remember { mutableStateOf("") }
    var fareEstimate by remember { mutableStateOf<RouteFareEstimate?>(null) }
    var isCalculatingFare by remember { mutableStateOf(false) }

    LaunchedEffect(busId) {
        isLoading = true
        val loadedBus = repository.getBusByName(busId)
        bus = loadedBus
        if (loadedBus != null && loadedBus.routes.isNotEmpty()) {
            fareStartStop = loadedBus.routes.first()
            fareEndStop = loadedBus.routes.last()
        }
        isLoading = false
    }

    // Automatically recalculate estimated fare whenever start or end stops change
    LaunchedEffect(fareStartStop, fareEndStop, bus) {
        val currentBus = bus
        if (currentBus != null && fareStartStop.isNotBlank() && fareEndStop.isNotBlank()) {
            isCalculatingFare = true
            fareEstimate = repository.estimateRouteFare(fareStartStop, fareEndStop, currentBus.routes)
            isCalculatingFare = false
        }
    }

    val currentRouteId = bus?.routeId ?: busId
    val isFav by repository.isFavorite(currentRouteId).collectAsState(initial = false)

    BackHandler(onBack = onBack)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (lang == AppLanguage.BANGLA && !bus?.banglaName.isNullOrBlank()) bus!!.banglaName else (bus?.englishName ?: if (lang == AppLanguage.BANGLA) "বাস রুটের বিবরণ" else "Bus Route Details"),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("bus_details_back")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    bus?.let { currentBus ->
                        IconButton(
                            onClick = {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        "Check out Dhaka bus ${currentBus.englishName} (${currentBus.banglaName}) route: ${currentBus.routes.joinToString(" -> ")}"
                                    )
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share Bus Route"))
                            }
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            bus?.let { currentBus ->
                FloatingActionButton(
                    onClick = {
                        scope.launch {
                            if (isFav) {
                                repository.removeFavorite(currentBus.routeId)
                            } else {
                                repository.toggleFavorite(currentBus)
                            }
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = if (isFav) Color(0xFFE11D48) else MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.testTag("details_favorite_fab")
                ) {
                    Icon(
                        imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Toggle Favorite"
                    )
                }
            }
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (bus == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Bus not found", style = MaterialTheme.typography.titleMedium)
            }
        } else {
            val currentBus = bus!!
            val displayStops = if (isReverseRoute) currentBus.routes.reversed() else currentBus.routes

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))

                    // Hero Card with Image & Info
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Bus banner image with reliable high quality fallback
                            AsyncImage(
                                model = currentBus.image.ifBlank { R.drawable.dhaka_city_bus_hero_1790080570471 },
                                placeholder = painterResource(R.drawable.dhaka_city_bus_hero_1790080570471),
                                error = painterResource(R.drawable.dhaka_city_bus_hero_1790080570471),
                                fallback = painterResource(R.drawable.dhaka_city_bus_hero_1790080570471),
                                contentDescription = currentBus.englishName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(14.dp))
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = currentBus.englishName,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (currentBus.banglaName.isNotBlank()) {
                                Text(
                                    text = currentBus.banglaName,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (currentBus.serviceType.isNotBlank()) {
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                MaterialTheme.colorScheme.primaryContainer,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = currentBus.serviceType,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                if (currentBus.time.isNotBlank()) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Timer,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.outline,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = currentBus.time,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Map View Switcher: Google Maps View vs Static Polyline Coverage
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = selectedMapTab == 0,
                                onClick = { selectedMapTab = 0 },
                                label = {
                                    Text(if (lang == AppLanguage.BANGLA) "গুগল ম্যাপ ভিউ" else "Google Maps View")
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Map,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                modifier = Modifier.testTag("tab_google_maps")
                            )

                            FilterChip(
                                selected = selectedMapTab == 1,
                                onClick = { selectedMapTab = 1 },
                                label = {
                                    Text(if (lang == AppLanguage.BANGLA) "ভেক্টর রুট ম্যাপ" else "Vector Route Map")
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Polyline,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                ),
                                modifier = Modifier.testTag("tab_vector_map")
                            )
                        }

                        if (selectedMapTab == 0) {
                            GoogleMapsRouteView(
                                stops = displayStops,
                                lang = lang,
                                busName = currentBus.englishName
                            )
                        } else {
                            RouteCoverageMap(
                                stops = displayStops,
                                lang = lang,
                                busName = currentBus.englishName
                            )
                        }
                    }

                    // ==========================================
                    // ROUTE DETAIL ESTIMATED FARE CALCULATOR TOOL
                    // ==========================================
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 18.dp)
                            .testTag("route_detail_fare_card")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Calculate,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = AppStrings.routeFareToolTitle(lang),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = AppStrings.routeFareToolSubtitle(lang),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Starting Stop Selector
                            var startExpanded by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(
                                expanded = startExpanded,
                                onExpandedChange = { startExpanded = !startExpanded },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = fareStartStop,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text(AppStrings.routePickupLabel(lang)) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Place,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = startExpanded)
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                    ),
                                    modifier = Modifier
                                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                        .fillMaxWidth()
                                        .testTag("fare_tool_start_selector")
                                )
                                ExposedDropdownMenu(
                                    expanded = startExpanded,
                                    onDismissRequest = { startExpanded = false }
                                ) {
                                    currentBus.routes.forEach { stop ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = stop,
                                                    fontWeight = if (stop == fareStartStop) FontWeight.Bold else FontWeight.Normal
                                                )
                                            },
                                            onClick = {
                                                fareStartStop = stop
                                                startExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Swap Stops Button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                IconButton(
                                    onClick = {
                                        val temp = fareStartStop
                                        fareStartStop = fareEndStop
                                        fareEndStop = temp
                                    },
                                    modifier = Modifier.testTag("fare_tool_swap_stops_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SwapVert,
                                        contentDescription = "Swap Stops",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            // Destination Stop Selector
                            var endExpanded by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(
                                expanded = endExpanded,
                                onExpandedChange = { endExpanded = !endExpanded },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = fareEndStop,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text(AppStrings.routeDropoffLabel(lang)) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Place,
                                            contentDescription = null,
                                            tint = Color(0xFFE11D48),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = endExpanded)
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                    ),
                                    modifier = Modifier
                                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                        .fillMaxWidth()
                                        .testTag("fare_tool_end_selector")
                                )
                                ExposedDropdownMenu(
                                    expanded = endExpanded,
                                    onDismissRequest = { endExpanded = false }
                                ) {
                                    currentBus.routes.forEach { stop ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = stop,
                                                    fontWeight = if (stop == fareEndStop) FontWeight.Bold else FontWeight.Normal
                                                )
                                            },
                                            onClick = {
                                                fareEndStop = stop
                                                endExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Fare Computation Result Badge & Details
                            if (isCalculatingFare) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            } else if (fareEstimate != null) {
                                val estimate = fareEstimate!!
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    shadowElevation = 1.dp,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = AppStrings.estimatedFareLabel(lang),
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = MaterialTheme.colorScheme.outline
                                                )
                                                Row(verticalAlignment = Alignment.Bottom) {
                                                    Text(
                                                        text = "৳ ${estimate.fareTaka}",
                                                        style = MaterialTheme.typography.headlineMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "BDT",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.outline,
                                                        modifier = Modifier.padding(bottom = 4.dp)
                                                    )
                                                }
                                            }

                                            // Distance and Waypoint Count Badge
                                            Column(horizontalAlignment = Alignment.End) {
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                                ) {
                                                    Text(
                                                        text = "~${"%.1f".format(estimate.distanceKm)} km",
                                                        style = MaterialTheme.typography.labelMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "${estimate.stopsTraveled} stops along route",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.outline
                                                )
                                            }
                                        }

                                        HorizontalDivider(
                                            modifier = Modifier.padding(vertical = 10.dp),
                                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                                        )

                                        // Student Discount & Official Fare Formula Notice
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.School,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = AppStrings.studentDiscountFare(estimate.fareTaka, lang),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }

                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = MaterialTheme.colorScheme.secondaryContainer
                                            ) {
                                                Text(
                                                    text = if (estimate.isExactBrtaMatch) "BRTA Official" else "BRTA Rate Est.",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                    fontWeight = FontWeight.SemiBold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = AppStrings.brtaOfficialRateNote(lang),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.outline,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Route direction toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (lang == AppLanguage.BANGLA) "রুটের স্টপেজসমূহ (${currentBus.totalStopsCount})" else "Route Waypoints (${currentBus.totalStopsCount})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        FilterChip(
                            selected = isReverseRoute,
                            onClick = { isReverseRoute = !isReverseRoute },
                            label = {
                                Text(
                                    if (isReverseRoute) {
                                        if (lang == AppLanguage.BANGLA) "উল্টো দিক" else "Reverse Direction"
                                    } else {
                                        if (lang == AppLanguage.BANGLA) "সোজা দিক" else "Forward Direction"
                                    }
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (isReverseRoute) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Route Timeline Nodes
                items(displayStops.size) { index ->
                    val stop = displayStops[index]
                    val isFirst = index == 0
                    val isLast = index == displayStops.size - 1

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    ) {
                        // Timeline vertical indicator
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(36.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isFirst -> MaterialTheme.colorScheme.primary
                                            isLast -> Color(0xFFE11D48)
                                            else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isFirst || isLast) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                    )
                                }
                            }

                            if (!isLast) {
                                Box(
                                    modifier = Modifier
                                        .width(2.dp)
                                        .height(32.dp)
                                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(bottom = if (isLast) 24.dp else 12.dp)
                        ) {
                            Text(
                                text = stop,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isFirst || isLast) FontWeight.Bold else FontWeight.Medium,
                                color = if (isFirst || isLast) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            if (isFirst) {
                                Text(
                                    text = "Starting Terminal",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            } else if (isLast) {
                                Text(
                                    text = "Destination Terminal",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFE11D48)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
