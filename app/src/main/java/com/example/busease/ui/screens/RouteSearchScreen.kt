package com.example.busease.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.busease.data.AppLanguage
import com.example.busease.data.AppSettings
import com.example.busease.data.AppStrings
import com.example.busease.data.model.BusRoute
import com.example.busease.data.repository.BusRepository
import com.example.busease.data.util.DhakaTransitCoordinates
import com.example.busease.data.util.LocationHelper
import com.example.busease.ui.components.AutocompleteStopInput
import com.example.busease.ui.components.BusCard
import com.example.busease.ui.components.RouteCoverageMap
import kotlinx.coroutines.launch

@Composable
fun RouteSearchScreen(
    onBusClick: (String) -> Unit
) {
    val context = LocalContext.current
    val repository = remember { BusRepository.getInstance(context) }
    val appSettings = remember { AppSettings.getInstance(context) }
    val lang = appSettings.appLanguage
    val scope = rememberCoroutineScope()

    var startLocation by remember { mutableStateOf("") }
    var endLocation by remember { mutableStateOf("") }
    var allStops by remember { mutableStateOf<List<String>>(emptyList()) }
    var searchResults by remember { mutableStateOf<List<BusRoute>>(emptyList()) }
    var isLoadingStops by remember { mutableStateOf(true) }
    var isSearching by remember { mutableStateOf(false) }
    var hasSearched by remember { mutableStateOf(false) }
    var isDetectingLocation by remember { mutableStateOf(false) }

    val favorites by repository.getAllFavorites().collectAsState(initial = emptyList())
    val favoriteRouteIds = remember(favorites) { favorites.map { it.routeId }.toSet() }

    fun performSearch() {
        scope.launch {
            isSearching = true
            searchResults = repository.searchBusesBetween(startLocation, endLocation)
            isSearching = false
            hasSearched = true
        }
    }

    fun detectCurrentStop() {
        scope.launch {
            isDetectingLocation = true
            val loc = LocationHelper.getCurrentLocation(context)
            val targetLat = loc?.latitude ?: 23.7570 // default Dhaka Farmgate if offline/emulator
            val targetLon = loc?.longitude ?: 90.3888
            val nearby = DhakaTransitCoordinates.getStopsWithinRadius(targetLat, targetLon, 1000.0)
            val closest = nearby.firstOrNull()?.first
            if (closest != null) {
                // Find matching stop name with proper case from allStops if possible
                val matched = allStops.firstOrNull { it.equals(closest, ignoreCase = true) } ?: closest.replaceFirstChar { it.uppercase() }
                startLocation = matched
            } else {
                startLocation = "Farmgate"
            }
            isDetectingLocation = false
            performSearch()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fine = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarse = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fine || coarse) {
            detectCurrentStop()
        }
    }

    fun requestLocationForStart() {
        val fineStatus = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarseStatus = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (fineStatus == PackageManager.PERMISSION_GRANTED || coarseStatus == PackageManager.PERMISSION_GRANTED) {
            detectCurrentStop()
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    LaunchedEffect(Unit) {
        isLoadingStops = true
        allStops = repository.getAllStops()
        isLoadingStops = false
        // Initial list shows popular buses
        searchResults = repository.getAllBuses()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("route_search_screen")
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))

            // Search Filter Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (lang == AppLanguage.BANGLA) "রুট অনুযায়ী বাস খুঁজুন" else "Find Dhaka City Bus",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (lang == AppLanguage.BANGLA) "শুরু এবং গন্তব্য স্টপ লিখে সরাসরি বাস দেখুন" else "Enter pickup and drop-off stops to see connecting buses",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Start Stop Input with GPS Detection
                    AutocompleteStopInput(
                        label = if (lang == AppLanguage.BANGLA) "শুরুর স্থান (যেমন: মিরপুর, ফার্মগেট)" else "Starting Stop (e.g. Mirpur, Farmgate)",
                        value = startLocation,
                        onValueChange = {
                            startLocation = it
                            performSearch()
                        },
                        suggestions = allStops,
                        testTagPrefix = "route_start_input",
                        trailingAction = {
                            IconButton(
                                onClick = { requestLocationForStart() },
                                modifier = Modifier.testTag("route_start_gps_button")
                            ) {
                                if (isDetectingLocation) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.MyLocation,
                                        contentDescription = "Detect My Nearest Stop",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    )

                    // Swap Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        IconButton(
                            onClick = {
                                val temp = startLocation
                                startLocation = endLocation
                                endLocation = temp
                                performSearch()
                            },
                            modifier = Modifier.testTag("swap_locations_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapVert,
                                contentDescription = "Swap Locations",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Destination Stop Input
                    AutocompleteStopInput(
                        label = if (lang == AppLanguage.BANGLA) "গন্তব্য স্থান (যেমন: মতিঝিল, উত্তরা)" else "Destination Stop (e.g. Motijheel, Uttara)",
                        value = endLocation,
                        onValueChange = {
                            endLocation = it
                            performSearch()
                        },
                        suggestions = allStops,
                        testTagPrefix = "route_end_input"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { performSearch() },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("find_buses_button")
                    ) {
                        Text(
                            text = if (lang == AppLanguage.BANGLA) "বাস খুঁজুন" else "Search Buses",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Interactive visual route polyline map for queried stops or top route
            val queryRouteStops = remember(startLocation, endLocation, searchResults) {
                if (startLocation.isNotBlank() && endLocation.isNotBlank()) {
                    val matchingBus = searchResults.firstOrNull()
                    if (matchingBus != null) {
                        val sIdx = matchingBus.routes.indexOfFirst { it.contains(startLocation.trim(), ignoreCase = true) }
                        val eIdx = matchingBus.routes.indexOfFirst { it.contains(endLocation.trim(), ignoreCase = true) }
                        if (sIdx != -1 && eIdx != -1) {
                            if (sIdx <= eIdx) matchingBus.routes.subList(sIdx, eIdx + 1)
                            else matchingBus.routes.subList(eIdx, sIdx + 1).reversed()
                        } else {
                            listOf(startLocation.trim(), endLocation.trim())
                        }
                    } else {
                        listOf(startLocation.trim(), endLocation.trim())
                    }
                } else if (startLocation.isNotBlank()) {
                    val matchingBus = searchResults.firstOrNull()
                    matchingBus?.routes?.take(6) ?: listOf(startLocation.trim())
                } else if (endLocation.isNotBlank()) {
                    val matchingBus = searchResults.firstOrNull()
                    matchingBus?.routes?.takeLast(6) ?: listOf(endLocation.trim())
                } else null
            }

            if (queryRouteStops != null && queryRouteStops.size >= 2) {
                RouteCoverageMap(
                    stops = queryRouteStops,
                    lang = lang,
                    busName = searchResults.firstOrNull()?.englishName ?: "",
                    initialSelectedStop = startLocation.takeIf { it.isNotBlank() } ?: endLocation.takeIf { it.isNotBlank() },
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Results header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val headerText = if (startLocation.isNotBlank() || endLocation.isNotBlank()) {
                    if (lang == AppLanguage.BANGLA) "উপলব্ধ বাস (${searchResults.size})" else "Available Routes (${searchResults.size})"
                } else {
                    if (lang == AppLanguage.BANGLA) "সকল বাস (${searchResults.size})" else "All Buses (${searchResults.size})"
                }
                Text(
                    text = headerText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                if (isSearching) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 2.dp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        if (searchResults.isEmpty() && !isSearching) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.DirectionsBus,
                            contentDescription = null,
                            modifier = Modifier.size(54.dp),
                            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (lang == AppLanguage.BANGLA) "এই রুটে কোনো সরাসরি বাস পাওয়া যায়নি" else "No direct bus found between these stops",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (lang == AppLanguage.BANGLA) "নিকটবর্তী কোনো স্টপ দিয়ে খুঁজে দেখতে পারেন" else "Try searching nearby transit hubs or switching stops",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        } else {
            items(searchResults, key = { it.routeId }) { bus ->
                val isFav = favoriteRouteIds.contains(bus.routeId)
                BusCard(
                    bus = bus,
                    isFavorite = isFav,
                    onFavoriteToggle = {
                        scope.launch {
                            if (isFav) {
                                repository.removeFavorite(bus.routeId)
                            } else {
                                repository.toggleFavorite(bus)
                            }
                        }
                    },
                    onClick = { onBusClick(bus.routeId) },
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
