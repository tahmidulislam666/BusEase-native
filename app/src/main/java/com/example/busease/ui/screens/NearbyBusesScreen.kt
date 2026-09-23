package com.example.busease.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.busease.data.AppLanguage
import com.example.busease.data.AppSettings
import com.example.busease.data.AppStrings
import com.example.busease.data.repository.BusRepository
import com.example.busease.data.repository.NearbyBusMatch
import com.example.busease.data.util.DhakaTransitCoordinates
import com.example.busease.data.util.LatLng
import com.example.busease.data.util.LocationHelper
import com.example.busease.data.util.UserLocation
import com.example.busease.ui.components.BusCard
import com.example.busease.ui.components.RouteCoverageMap
import kotlinx.coroutines.launch

@Composable
fun NearbyBusesScreen(
    onBusClick: (String) -> Unit
) {
    val context = LocalContext.current
    val repository = remember { BusRepository.getInstance(context) }
    val appSettings = remember { AppSettings.getInstance(context) }
    val lang = appSettings.appLanguage
    val scope = rememberCoroutineScope()

    var userLocation by remember { mutableStateOf<UserLocation?>(null) }
    var isLocating by remember { mutableStateOf(false) }
    var permissionDenied by remember { mutableStateOf(false) }
    var nearbyMatches by remember { mutableStateOf<List<NearbyBusMatch>>(emptyList()) }
    var selectedStopFilter by remember { mutableStateOf<String?>(null) }
    var detectedRadiusStops by remember { mutableStateOf<List<Pair<String, Double>>>(emptyList()) }

    val favorites by repository.getAllFavorites().collectAsState(initial = emptyList())
    val favoriteRouteIds = remember(favorites) { favorites.map { it.routeId }.toSet() }

    // Popular Dhaka anchor locations for fallback / manual testing simulation
    val popularDhakaHubs = remember {
        listOf(
            "Farmgate" to LatLng(23.7570, 90.3888),
            "Mirpur 10" to LatLng(23.8070, 90.3685),
            "Shahbag" to LatLng(23.7380, 90.3957),
            "Motijheel" to LatLng(23.7330, 90.4190),
            "Uttara" to LatLng(23.8682, 90.4011),
            "Mohakhali" to LatLng(23.7780, 90.4005),
            "Dhanmondi" to LatLng(23.7485, 90.3810)
        )
    }

    fun searchNearby(lat: Double, lon: Double) {
        scope.launch {
            isLocating = true
            // Find all stops within 1000m (1km)
            val stops = DhakaTransitCoordinates.getStopsWithinRadius(lat, lon, 1000.0)
            detectedRadiusStops = stops
            val results = repository.searchBusesNearLocation(lat, lon, 1000.0)
            nearbyMatches = results
            selectedStopFilter = null
            isLocating = false
        }
    }

    fun requestDeviceLocation() {
        scope.launch {
            isLocating = true
            permissionDenied = false
            val loc = LocationHelper.getCurrentLocation(context)
            if (loc != null) {
                userLocation = loc
                searchNearby(loc.latitude, loc.longitude)
            } else {
                // If location service is off or in emulator without fix, default to Dhaka Center (Farmgate)
                val defaultDhaka = UserLocation(23.7570, 90.3888)
                userLocation = defaultDhaka
                searchNearby(defaultDhaka.latitude, defaultDhaka.longitude)
            }
            isLocating = false
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            permissionDenied = false
            requestDeviceLocation()
        } else {
            permissionDenied = true
        }
    }

    fun checkAndRequestLocation() {
        val fineStatus = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarseStatus = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (fineStatus == PackageManager.PERMISSION_GRANTED || coarseStatus == PackageManager.PERMISSION_GRANTED) {
            requestDeviceLocation()
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Auto-detect on first launch
    LaunchedEffect(Unit) {
        val fineStatus = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarseStatus = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (fineStatus == PackageManager.PERMISSION_GRANTED || coarseStatus == PackageManager.PERMISSION_GRANTED) {
            requestDeviceLocation()
        } else {
            // Check permission
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Filter displayed buses by selected stop if tapped
    val displayedMatches = remember(nearbyMatches, selectedStopFilter) {
        if (selectedStopFilter == null) nearbyMatches
        else nearbyMatches.filter { it.nearestStop.equals(selectedStopFilter, ignoreCase = true) || it.bus.routes.any { r -> r.contains(selectedStopFilter!!, ignoreCase = true) } }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("nearby_buses_screen")
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))

            // Hero Header Card: 1km Radius Location Service
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MyLocation,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = AppStrings.nearbyTitle(lang),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (userLocation != null) {
                                        "Lat: ${"%.4f".format(userLocation!!.latitude)}, Lon: ${"%.4f".format(userLocation!!.longitude)}"
                                    } else {
                                        AppStrings.nearbySubtitle(lang)
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }

                        IconButton(
                            onClick = { checkAndRequestLocation() },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .testTag("refresh_location_button")
                        ) {
                            if (isLocating) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh Location",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = if (lang == AppLanguage.BANGLA)
                            "জিপিএস লোকেশন ব্যবহার করে আপনার ১ কিলোমিটার দূরত্বের ভেতরের সকল বাস স্টপ ও বাস রুট স্বয়ংক্রিয়ভাবে ফিল্টার করা হচ্ছে।"
                        else
                            "Using Android location services to discover every bus route serving stops within a 1 kilometer walking radius of your location.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Location Action Button / Retry
                    if (permissionDenied) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOff,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = AppStrings.locationPermDenied(lang),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Button(
                                    onClick = { checkAndRequestLocation() },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Text("Grant", fontSize = 12.sp)
                                }
                            }
                        }
                    } else {
                        Button(
                            onClick = { checkAndRequestLocation() },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("request_nearby_location_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.NearMe,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isLocating) AppStrings.locatingMsg(lang) else AppStrings.requestLocationBtn(lang),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quick Location Switcher: Common Dhaka Transit Hubs
                    Text(
                        text = if (lang == AppLanguage.BANGLA) "বা দ্রুত ঢাকার যেকোনো স্থান নির্বাচন করুন:" else "Or test near a major Dhaka transit hub:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.outline
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(popularDhakaHubs) { (hubName, coords) ->
                            FilterChip(
                                selected = userLocation?.let {
                                    Math.abs(it.latitude - coords.latitude) < 0.005 && Math.abs(it.longitude - coords.longitude) < 0.005
                                } == true,
                                onClick = {
                                    val simulated = UserLocation(coords.latitude, coords.longitude)
                                    userLocation = simulated
                                    searchNearby(coords.latitude, coords.longitude)
                                },
                                label = { Text(hubName, fontSize = 12.sp) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Place,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Detected stops within 1km chips
            if (detectedRadiusStops.isNotEmpty()) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (lang == AppLanguage.BANGLA)
                                    "১ কি.মি.-এর ভেতরের স্টপসমূহ (${detectedRadiusStops.size} টি)"
                                else
                                    "Stops within 1 km (${detectedRadiusStops.size})",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item {
                                FilterChip(
                                    selected = selectedStopFilter == null,
                                    onClick = { selectedStopFilter = null },
                                    label = { Text(if (lang == AppLanguage.BANGLA) "সবগুলো" else "All Stops", fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                )
                            }
                            items(detectedRadiusStops) { (stopName, dist) ->
                                val isSelected = selectedStopFilter.equals(stopName, ignoreCase = true)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedStopFilter = if (isSelected) null else stopName
                                    },
                                    label = {
                                        Text("$stopName (~${dist.toInt()}m)", fontSize = 11.sp)
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Results count header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (lang == AppLanguage.BANGLA)
                        "উপলব্ধ বাস রুট (${displayedMatches.size})"
                    else
                        "Available Bus Routes (${displayedMatches.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (isLocating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        if (displayedMatches.isEmpty() && !isLocating) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 36.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.DirectionsBus,
                            contentDescription = null,
                            modifier = Modifier.size(54.dp),
                            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = AppStrings.noNearbyBuses(lang),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = AppStrings.noNearbyBusesDesc(lang),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            }
        } else {
            items(displayedMatches, key = { it.bus.routeId }) { match ->
                val isFav = favoriteRouteIds.contains(match.bus.routeId)
                BusCard(
                    bus = match.bus,
                    isFavorite = isFav,
                    onFavoriteToggle = {
                        scope.launch {
                            if (isFav) {
                                repository.removeFavorite(match.bus.routeId)
                            } else {
                                repository.toggleFavorite(match.bus)
                            }
                        }
                    },
                    onClick = { onBusClick(match.bus.routeId) },
                    nearbyStopHighlight = match.nearestStop,
                    nearbyDistanceMeters = match.distanceMeters,
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
