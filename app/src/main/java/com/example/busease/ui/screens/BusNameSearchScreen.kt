package com.example.busease.ui.screens

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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.busease.data.AppLanguage
import com.example.busease.data.AppSettings
import com.example.busease.data.AppStrings
import com.example.busease.data.model.BusRoute
import com.example.busease.data.repository.BusRepository
import com.example.busease.ui.components.BusCard
import kotlinx.coroutines.launch

@Composable
fun BusNameSearchScreen(
    onBusClick: (String) -> Unit
) {
    val context = LocalContext.current
    val repository = remember { BusRepository.getInstance(context) }
    val appSettings = remember { AppSettings.getInstance(context) }
    val lang = appSettings.appLanguage
    val scope = rememberCoroutineScope()

    var query by remember { mutableStateOf("") }
    var busList by remember { mutableStateOf<List<BusRoute>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }

    val favorites by repository.getAllFavorites().collectAsState(initial = emptyList())
    val favoriteRouteIds = remember(favorites) { favorites.map { it.routeId }.toSet() }

    LaunchedEffect(query) {
        isSearching = true
        busList = if (query.isBlank()) {
            repository.getAllBuses()
        } else {
            repository.searchBusesByQuery(query)
        }
        isSearching = false
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("bus_name_search_screen")
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = {
                    Text(
                        if (lang == AppLanguage.BANGLA) "বাসের নাম বা সার্ভিস দিয়ে খুঁজুন"
                        else "Search by bus name or service"
                    )
                },
                placeholder = {
                    Text(
                        if (lang == AppLanguage.BANGLA) "যেমন: অছিম, বিহঙ্গ, সিটিং..."
                        else "e.g. Achim, বিহঙ্গ, Sitting..."
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(
                            onClick = { query = "" },
                            modifier = Modifier.testTag("clear_name_search")
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("bus_name_search_input")
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (lang == AppLanguage.BANGLA) "বাস পাওয়া গেছে (${busList.size})" else "Buses Found (${busList.size})",
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

        if (busList.isEmpty() && !isSearching) {
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
                            text = if (lang == AppLanguage.BANGLA) "‘$query’ নামে কোনো বাস পাওয়া যায়নি" else "No buses matching '$query'",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        } else {
            items(busList, key = { it.routeId }) { bus ->
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
