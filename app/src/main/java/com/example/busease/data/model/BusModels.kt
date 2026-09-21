package com.example.busease.data.model

data class BusRoute(
    val id: String = "",
    val englishName: String,
    val banglaName: String,
    val image: String,
    val routes: List<String>,
    val serviceType: String,
    val time: String = ""
) {
    val routeId: String
        get() = id.ifBlank { "${englishName}_${startStop}_${endStop}" }

    val startStop: String
        get() = routes.firstOrNull() ?: ""

    val endStop: String
        get() = routes.lastOrNull() ?: ""

    val totalStopsCount: Int
        get() = routes.size
}

data class FareItem(
    val from: String,
    val to: String,
    val distance: String,
    val fare: String,
    val route: String
)
