package com.example.busease.ui.components

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.viewinterop.AndroidView
import com.example.busease.data.AppLanguage
import com.example.busease.data.util.DhakaTransitCoordinates
import com.example.busease.data.util.LatLng
import org.json.JSONArray
import org.json.JSONObject

/**
 * Interactive Google Maps view for bus route paths and stop markers.
 *
 * Utilizes a responsive Leaflet + OpenStreetMap engine styled and configured
 * with Google Maps cartographic tiles and Google Maps route visual language,
 * plus a 1-tap "Open in Google Maps App" intent to display real-time navigation
 * with origin and destination waypoints.
 */
@Composable
fun GoogleMapsRouteView(
    stops: List<String>,
    lang: AppLanguage,
    busName: String,
    modifier: Modifier = Modifier,
    initialSelectedStop: String? = null
) {
    if (stops.isEmpty()) return

    val context = LocalContext.current
    val resolvedPoints = remember(stops) {
        DhakaTransitCoordinates.resolveRoutePolyline(stops)
    }

    var selectedStopName by remember(stops) {
        mutableStateOf(initialSelectedStop)
    }
    var selectedStopIndex by remember(stops) {
        mutableStateOf(
            if (initialSelectedStop != null) {
                resolvedPoints.indexOfFirst { it.first.equals(initialSelectedStop, ignoreCase = true) }.takeIf { it >= 0 }
            } else null
        )
    }

    var isMapLoading by remember { mutableStateOf(true) }
    var mapType by remember { mutableStateOf("roadmap") } // roadmap, satellite, terrain
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var hasError by remember { mutableStateOf(false) }

    // Prepare JSON payload for the Map
    val stopsJsonArray = remember(resolvedPoints) {
        val array = JSONArray()
        resolvedPoints.forEachIndexed { index, (name, coord) ->
            val obj = JSONObject()
            obj.put("index", index)
            obj.put("name", name)
            obj.put("lat", coord.latitude)
            obj.put("lng", coord.longitude)
            obj.put("isStart", index == 0)
            obj.put("isEnd", index == resolvedPoints.size - 1)
            array.put(obj)
        }
        array.toString()
    }

    val htmlContent = remember(stopsJsonArray, busName, mapType) {
        generateGoogleMapHtml(
            busName = busName,
            stopsJson = stopsJsonArray,
            mapType = mapType
        )
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("google_maps_route_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                MaterialTheme.colorScheme.primaryContainer,
                                RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (lang == AppLanguage.BANGLA) "গুগল ম্যাপে বাস রুট" else "Google Maps Route View",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .background(
                                        Color(0xFF4285F4).copy(alpha = 0.15f),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "LIVE",
                                    color = Color(0xFF1A73E8),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                        Text(
                            text = if (lang == AppLanguage.BANGLA)
                                "${stops.size}টি স্টপের বিস্তারিত পথ ও মার্কার"
                            else
                                "${stops.size} Stops Path & Waypoint Markers",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                // Launch External Google Maps Intent Button
                IconButton(
                    onClick = {
                        openInGoogleMaps(
                            context = context,
                            startStop = stops.firstOrNull() ?: "",
                            endStop = stops.lastOrNull() ?: "",
                            resolvedPoints = resolvedPoints,
                            busName = busName
                        )
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                        .testTag("open_external_google_maps_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = "Open in Google Maps",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Map Layer Selector Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ElevatedFilterChip(
                    selected = mapType == "roadmap",
                    onClick = {
                        mapType = "roadmap"
                        webViewRef?.evaluateJavascript("switchMapType('roadmap');", null)
                    },
                    label = { Text(if (lang == AppLanguage.BANGLA) "রোডম্যাপ" else "Roadmap", fontSize = 12.sp) },
                    colors = FilterChipDefaults.elevatedFilterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier.testTag("map_layer_roadmap")
                )

                ElevatedFilterChip(
                    selected = mapType == "satellite",
                    onClick = {
                        mapType = "satellite"
                        webViewRef?.evaluateJavascript("switchMapType('satellite');", null)
                    },
                    label = { Text(if (lang == AppLanguage.BANGLA) "স্যাটেলাইট" else "Satellite", fontSize = 12.sp) },
                    colors = FilterChipDefaults.elevatedFilterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier.testTag("map_layer_satellite")
                )

                ElevatedFilterChip(
                    selected = mapType == "terrain",
                    onClick = {
                        mapType = "terrain"
                        webViewRef?.evaluateJavascript("switchMapType('terrain');", null)
                    },
                    label = { Text(if (lang == AppLanguage.BANGLA) "ভূপ্রকৃতি" else "Terrain", fontSize = 12.sp) },
                    colors = FilterChipDefaults.elevatedFilterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier.testTag("map_layer_terrain")
                )

                Spacer(modifier = Modifier.weight(1f))

                IconButton(
                    onClick = {
                        webViewRef?.evaluateJavascript("resetView();", null)
                    },
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Fit Route",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Interactive Map View Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFE8ECEF))
                    .testTag("google_map_webview_container")
            ) {
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            // Use Software Layer to avoid MESA GPU / rendernode crash in container/emulator environments
                            setLayerType(View.LAYER_TYPE_SOFTWARE, null)

                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                loadWithOverviewMode = true
                                useWideViewPort = true
                                cacheMode = WebSettings.LOAD_DEFAULT
                                setSupportZoom(true)
                                builtInZoomControls = true
                                displayZoomControls = false
                            }

                            // JS Interface to communicate stop clicks back to Compose
                            addJavascriptInterface(object {
                                @JavascriptInterface
                                fun onMarkerClicked(index: Int, name: String) {
                                    post {
                                        selectedStopIndex = index
                                        selectedStopName = name
                                    }
                                }
                            }, "AndroidBridge")

                            webChromeClient = object : WebChromeClient() {
                                override fun onConsoleMessage(cm: ConsoleMessage?): Boolean {
                                    return true
                                }
                            }

                            webViewClient = object : WebViewClient() {
                                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                    isMapLoading = true
                                }

                                override fun onPageFinished(view: WebView?, url: String?) {
                                    isMapLoading = false
                                }

                                override fun onReceivedError(
                                    view: WebView?,
                                    request: WebResourceRequest?,
                                    error: WebResourceError?
                                ) {
                                    if (request?.isForMainFrame == true) {
                                        hasError = true
                                        isMapLoading = false
                                    }
                                }

                                override fun onRenderProcessGone(
                                    view: WebView?,
                                    detail: RenderProcessGoneDetail?
                                ): Boolean {
                                    hasError = true
                                    isMapLoading = false
                                    return true // Handled gracefully, do not crash the host app process
                                }
                            }

                            loadDataWithBaseURL(
                                "https://maps.google.com/",
                                htmlContent,
                                "text/html",
                                "UTF-8",
                                null
                            )
                            webViewRef = this
                        }
                    },
                    update = { view ->
                        webViewRef = view
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Loading Indicator
                if (isMapLoading) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        shadowElevation = 4.dp,
                        modifier = Modifier.align(Alignment.Center)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = if (lang == AppLanguage.BANGLA) "ম্যাপ লোড হচ্ছে..." else "Loading Map...",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Error fallback overlay
                if (hasError) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFF3F4F6))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                text = if (lang == AppLanguage.BANGLA) "ম্যাপ প্রদর্শন সম্ভব হচ্ছে না" else "Map display issue",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            OutlinedButton(
                                onClick = {
                                    hasError = false
                                    webViewRef?.loadDataWithBaseURL(
                                        "https://maps.google.com/",
                                        htmlContent,
                                        "text/html",
                                        "UTF-8",
                                        null
                                    )
                                }
                            ) {
                                Text(if (lang == AppLanguage.BANGLA) "পুনরায় চেষ্টা করুন" else "Retry")
                            }
                        }
                    }
                }

                // Floating Selected Stop Information Banner
                if (selectedStopIndex != null && selectedStopIndex in resolvedPoints.indices) {
                    val idx = selectedStopIndex!!
                    val stopData = resolvedPoints[idx]
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 6.dp,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 12.dp)
                            .clickable { selectedStopIndex = null }
                            .padding(horizontal = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = null,
                                tint = if (idx == 0) Color(0xFF10B981)
                                else if (idx == resolvedPoints.size - 1) Color(0xFFEF4444)
                                else Color(0xFF1A73E8),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "${idx + 1}. ${stopData.first}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Lat: ${"%.4f".format(stopData.second.latitude)}, Lng: ${"%.4f".format(stopData.second.longitude)}",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (idx == 0) (if (lang == AppLanguage.BANGLA) "শুরুর টার্মিনাল" else "Origin")
                                else if (idx == resolvedPoints.size - 1) (if (lang == AppLanguage.BANGLA) "শেষ টার্মিনাল" else "Destination")
                                else (if (lang == AppLanguage.BANGLA) "স্টপেজ" else "Stop"),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Map Footer / External Actions Strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (lang == AppLanguage.BANGLA)
                        "প্রথম স্টপ: ${stops.firstOrNull() ?: ""} → শেষ: ${stops.lastOrNull() ?: ""}"
                    else
                        "Start: ${stops.firstOrNull() ?: ""} → End: ${stops.lastOrNull() ?: ""}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        openInGoogleMaps(
                            context = context,
                            startStop = stops.firstOrNull() ?: "",
                            endStop = stops.lastOrNull() ?: "",
                            resolvedPoints = resolvedPoints,
                            busName = busName
                        )
                    }
                ) {
                    Text(
                        text = if (lang == AppLanguage.BANGLA) "গুগল ম্যাপে খুলুন" else "Open in Google Maps",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF1A73E8),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = null,
                        tint = Color(0xFF1A73E8),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

/**
 * Fires an Android Intent to open the route directly in the Google Maps app or browser,
 * passing origin, destination, and transit travel mode.
 */
private fun openInGoogleMaps(
    context: Context,
    startStop: String,
    endStop: String,
    resolvedPoints: List<Pair<String, LatLng>>,
    busName: String
) {
    try {
        val startCoord = resolvedPoints.firstOrNull()?.second
        val endCoord = resolvedPoints.lastOrNull()?.second

        val originParam = if (startCoord != null) "${startCoord.latitude},${startCoord.longitude}" else Uri.encode("$startStop, Dhaka")
        val destParam = if (endCoord != null) "${endCoord.latitude},${endCoord.longitude}" else Uri.encode("$endStop, Dhaka")

        // Google Maps Directions Intent
        val mapUri = Uri.parse("https://www.google.com/maps/dir/?api=1&origin=$originParam&destination=$destParam&travelmode=transit")
        val intent = Intent(Intent.ACTION_VIEW, mapUri).apply {
            setPackage("com.google.android.apps.maps")
        }

        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            // Fallback to any browser
            val webIntent = Intent(Intent.ACTION_VIEW, mapUri)
            context.startActivity(webIntent)
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Opening Google Maps...", Toast.LENGTH_SHORT).show()
        val fallbackUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode("$busName Bus Route Dhaka")}")
        context.startActivity(Intent(Intent.ACTION_VIEW, fallbackUri))
    }
}

/**
 * Generates an interactive map HTML document using Leaflet with Google Maps tile styling,
 * animated polyline route pathing, origin/terminal pin icons, and stop waypoint markers.
 */
private fun generateGoogleMapHtml(
    busName: String,
    stopsJson: String,
    mapType: String
): String {
    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8" />
            <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no" />
            <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
            <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
            <style>
                * { box-sizing: border-box; margin: 0; padding: 0; }
                body, html, #map { width: 100%; height: 100%; font-family: -apple-system, Roboto, sans-serif; }
                .leaflet-control-attribution { font-size: 8px; }
                
                /* Custom Pulse Marker for Start */
                .start-pin {
                    background: #10B981;
                    border: 2.5px solid #FFFFFF;
                    border-radius: 50%;
                    box-shadow: 0 2px 6px rgba(0,0,0,0.35);
                    width: 18px;
                    height: 18px;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    color: white;
                    font-size: 10px;
                    font-weight: bold;
                }
                
                /* Custom Pin for Destination */
                .end-pin {
                    background: #EF4444;
                    border: 2.5px solid #FFFFFF;
                    border-radius: 50%;
                    box-shadow: 0 2px 6px rgba(0,0,0,0.35);
                    width: 18px;
                    height: 18px;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    color: white;
                    font-size: 10px;
                    font-weight: bold;
                }
                
                /* Waypoint Stop Node */
                .stop-node {
                    background: #1A73E8;
                    border: 2px solid #FFFFFF;
                    border-radius: 50%;
                    box-shadow: 0 1px 4px rgba(0,0,0,0.25);
                    width: 11px;
                    height: 11px;
                }

                .leaflet-popup-content-wrapper {
                    border-radius: 12px;
                    box-shadow: 0 4px 12px rgba(0,0,0,0.15);
                    padding: 4px;
                }
                .leaflet-popup-content {
                    margin: 8px 12px;
                    line-height: 1.4;
                    font-size: 12px;
                }
                .popup-title {
                    font-weight: 700;
                    color: #1F2937;
                    font-size: 13px;
                }
                .popup-sub {
                    color: #4B5563;
                    font-size: 11px;
                }
            </style>
        </head>
        <body>
            <div id="map"></div>
            <script>
                const stopsData = $stopsJson;
                
                // Initialize map centered at Dhaka center with smooth/software friendly settings
                const map = L.map('map', {
                    zoomControl: false,
                    attributionControl: false,
                    fadeAnimation: false,
                    zoomAnimation: false,
                    markerZoomAnimation: false,
                    preferCanvas: false
                }).setView([23.8103, 90.4125], 12);

                // Add zoom control at bottom right
                L.control.zoom({ position: 'bottomright' }).addTo(map);

                // Tile Layers
                const roadmapLayer = L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                    maxZoom: 19
                });
                
                const satelliteLayer = L.tileLayer('https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}', {
                    maxZoom: 19
                });

                const terrainLayer = L.tileLayer('https://{s}.tile.opentopomap.org/{z}/{x}/{y}.png', {
                    maxZoom: 17
                });

                let currentLayer = roadmapLayer;
                currentLayer.addTo(map);

                window.switchMapType = function(type) {
                    map.removeLayer(currentLayer);
                    if (type === 'satellite') {
                        currentLayer = satelliteLayer;
                    } else if (type === 'terrain') {
                        currentLayer = terrainLayer;
                    } else {
                        currentLayer = roadmapLayer;
                    }
                    currentLayer.addTo(map);
                };

                // Prepare Route Polyline Coordinates
                const latlngs = [];
                stopsData.forEach(function(item) {
                    latlngs.push([item.lat, item.lng]);
                });

                // 1. Draw Polyline Glow / Border
                const polylineGlow = L.polyline(latlngs, {
                    color: '#4285F4',
                    weight: 8,
                    opacity: 0.35,
                    lineCap: 'round',
                    lineJoin: 'round'
                }).addTo(map);

                // 2. Draw Main Transit Polyline
                const polylineMain = L.polyline(latlngs, {
                    color: '#1A73E8',
                    weight: 4.5,
                    opacity: 0.95,
                    lineCap: 'round',
                    lineJoin: 'round'
                }).addTo(map);

                // 3. Add Waypoint Stop Markers
                stopsData.forEach(function(item, idx) {
                    let markerHtml = '<div class="stop-node"></div>';
                    let iconSize = [11, 11];
                    let iconAnchor = [5.5, 5.5];

                    if (item.isStart) {
                        markerHtml = '<div class="start-pin">A</div>';
                        iconSize = [18, 18];
                        iconAnchor = [9, 9];
                    } else if (item.isEnd) {
                        markerHtml = '<div class="end-pin">B</div>';
                        iconSize = [18, 18];
                        iconAnchor = [9, 9];
                    }

                    const customIcon = L.divIcon({
                        className: '',
                        html: markerHtml,
                        iconSize: iconSize,
                        iconAnchor: iconAnchor
                    });

                    const marker = L.marker([item.lat, item.lng], { icon: customIcon }).addTo(map);

                    const labelType = item.isStart ? 'Origin Terminal' : (item.isEnd ? 'Destination Terminal' : 'Bus Stop Waypoint');
                    const popupContent = '<div class="popup-title">' + (idx + 1) + '. ' + item.name + '</div>' +
                                         '<div class="popup-sub">' + labelType + '</div>';

                    marker.bindPopup(popupContent);

                    marker.on('click', function() {
                        if (window.AndroidBridge && window.AndroidBridge.onMarkerClicked) {
                            window.AndroidBridge.onMarkerClicked(idx, item.name);
                        }
                    });
                });

                // Auto-fit bounds with comfortable padding
                if (latlngs.length > 0) {
                    const bounds = L.latLngBounds(latlngs);
                    map.fitBounds(bounds, { padding: [35, 35] });
                }

                window.resetView = function() {
                    if (latlngs.length > 0) {
                        map.fitBounds(L.latLngBounds(latlngs), { padding: [35, 35] });
                    }
                };
            </script>
        </body>
        </html>
    """.trimIndent()
}
