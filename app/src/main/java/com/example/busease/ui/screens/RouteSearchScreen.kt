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
import com.example.busease.data.model.BusRoute
import com.example.busease.data.repository.BusRepository
import com.example.busease.ui.components.AutocompleteStopInput
import com.example.busease.ui.components.BusCard
import kotlinx.coroutines.launch

@Composable
fun RouteSearchScreen(
    onBusClick: (String) -> Unit
) {
    val context = LocalContext.current
    val repository = remember { BusRepository.getInstance(context) }
    val scope = rememberCoroutineScope()

    var startLocation by remember { mutableStateOf("") }
    var endLocation by remember { mutableStateOf("") }
    var allStops by remember { mutableStateOf<List<String>>(emptyList()) }
    var searchResults by remember { mutableStateOf<List<BusRoute>>(emptyList()) }
    var isLoadingStops by remember { mutableStateOf(true) }
    var isSearching by remember { mutableStateOf(false) }
    var hasSearched by remember { mutableStateOf(false) }

    val favorites by repository.getAllFavorites().collectAsState(initial = emptyList())
    val favoriteRouteIds = remember(favorites) { favorites.map { it.routeId }.toSet() }

    LaunchedEffect(Unit) {
        isLoadingStops = true
        allStops = repository.getAllStops()
        isLoadingStops = false
        // Initial list shows popular buses
        searchResults = repository.getAllBuses()
    }

    fun performSearch() {
        scope.launch {
            isSearching = true
            searchResults = repository.searchBusesBetween(startLocation, endLocation)
            isSearching = false
            hasSearched = true
        }
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
                        text = "Find Dhaka City Bus",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Enter pickup and drop-off stops to see connecting buses",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Start Stop Input
                    AutocompleteStopInput(
                        label = "Starting Stop (e.g. Mirpur, Farmgate)",
                        value = startLocation,
                        onValueChange = {
                            startLocation = it
                            performSearch()
                        },
                        suggestions = allStops,
                        testTagPrefix = "route_start_input"
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
                        label = "Destination Stop (e.g. Motijheel, Uttara)",
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
                            text = "Search Buses",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Results header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (startLocation.isNotBlank() || endLocation.isNotBlank()) "Available Routes (${searchResults.size})" else "All Buses (${searchResults.size})",
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
                            text = "No direct bus found between these stops",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Try searching nearby transit hubs or switching stops",
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
