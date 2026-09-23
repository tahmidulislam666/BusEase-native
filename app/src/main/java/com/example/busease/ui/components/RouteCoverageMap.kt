package com.example.busease.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.busease.R
import com.example.busease.data.AppLanguage
import com.example.busease.data.util.DhakaTransitCoordinates
import com.example.busease.data.util.LatLng
import kotlin.math.max
import kotlin.math.min

/**
 * Interactive visual representation of a bus route using a static vector map background,
 * animated polyline path rendering, stop node markers, route coverage stats, and tap-to-inspect stops.
 */
@Composable
fun RouteCoverageMap(
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

    var selectedStopIndex by remember(stops) {
        mutableStateOf(
            if (initialSelectedStop != null) {
                resolvedPoints.indexOfFirst { it.first.equals(initialSelectedStop, ignoreCase = true) }.takeIf { it >= 0 }
            } else null
        )
    }

    var zoomLevel by remember { mutableFloatStateOf(1.0f) }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("route_coverage_map_card")
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
                        Text(
                            text = if (lang == AppLanguage.BANGLA) "রুটের ম্যাপ ও কভারেজ" else "Route Map & Coverage",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (lang == AppLanguage.BANGLA) "${stops.size}টি স্টপ সংলগ্ন পথ" else "${stops.size} Stops Polyline Path",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                // Zoom controls & Open in Google Maps action
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = { zoomLevel = (zoomLevel - 0.2f).coerceAtLeast(0.8f) },
                        modifier = Modifier
                            .size(32.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                            .testTag("map_zoom_out")
                    ) {
                        Icon(
                            Icons.Default.ZoomOut,
                            contentDescription = "Zoom Out",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = { zoomLevel = (zoomLevel + 0.2f).coerceAtMost(1.8f) },
                        modifier = Modifier
                            .size(32.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                            .testTag("map_zoom_in")
                    ) {
                        Icon(
                            Icons.Default.ZoomIn,
                            contentDescription = "Zoom In",
                            modifier = Modifier.size(16.dp)
                        )
                    }
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
                            .size(32.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                            .testTag("open_external_google_maps_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = "Open in Google Maps",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Map canvas container
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFE8ECEF))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .testTag("map_canvas_box")
            ) {
                val boxWidth = constraints.maxWidth.toFloat()
                val boxHeight = constraints.maxHeight.toFloat()

                // Static Cartographic Map Background
                Image(
                    painter = painterResource(id = R.drawable.dhaka_transit_map_bg_1790082034776),
                    contentDescription = "Dhaka City Map Vector Background",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Polyline and Node Canvas
                val primaryColor = MaterialTheme.colorScheme.primary
                val startColor = Color(0xFF10B981) // Green for start
                val endColor = Color(0xFFEF4444)   // Red for destination
                val pathGlowColor = primaryColor.copy(alpha = 0.25f)

                // Compute bounding box
                val lats = resolvedPoints.map { it.second.latitude }
                val lngs = resolvedPoints.map { it.second.longitude }
                val minLat = (lats.minOrNull() ?: 23.70) - 0.015
                val maxLat = (lats.maxOrNull() ?: 23.90) + 0.015
                val minLng = (lngs.minOrNull() ?: 90.30) - 0.015
                val maxLng = (lngs.maxOrNull() ?: 90.48) + 0.015

                val latSpan = max(0.01, maxLat - minLat)
                val lngSpan = max(0.01, maxLng - minLng)

                val paddingX = 40f
                val paddingY = 40f
                val usableW = (boxWidth - paddingX * 2) * zoomLevel
                val usableH = (boxHeight - paddingY * 2) * zoomLevel
                val offsetX = (boxWidth - usableW) / 2f
                val offsetY = (boxHeight - usableH) / 2f

                // Coordinate to Screen Offset function
                val screenPoints = remember(resolvedPoints, boxWidth, boxHeight, zoomLevel) {
                    resolvedPoints.map { (_, coord) ->
                        val xNorm = ((coord.longitude - minLng) / lngSpan).toFloat()
                        // Invert latitude because screen Y goes downwards
                        val yNorm = (1f - ((coord.latitude - minLat) / latSpan).toFloat())
                        Offset(offsetX + xNorm * usableW, offsetY + yNorm * usableH)
                    }
                }

                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(screenPoints) {
                            detectTapGestures { tapOffset ->
                                // Find closest stop node to tap
                                val nearestIndex = screenPoints.indices.minByOrNull { i ->
                                    val dx = screenPoints[i].x - tapOffset.x
                                    val dy = screenPoints[i].y - tapOffset.y
                                    dx * dx + dy * dy
                                }
                                if (nearestIndex != null) {
                                    val pt = screenPoints[nearestIndex]
                                    val distSq = (pt.x - tapOffset.x) * (pt.x - tapOffset.x) +
                                            (pt.y - tapOffset.y) * (pt.y - tapOffset.y)
                                    if (distSq < 1600f) { // Within 40px radius
                                        selectedStopIndex = nearestIndex
                                    } else {
                                        selectedStopIndex = null
                                    }
                                }
                            }
                        }
                ) {
                    if (screenPoints.size >= 2) {
                        // 1. Draw smooth polyline outer glow
                        val path = Path().apply {
                            moveTo(screenPoints[0].x, screenPoints[0].y)
                            for (idx in 1 until screenPoints.size) {
                                lineTo(screenPoints[idx].x, screenPoints[idx].y)
                            }
                        }

                        // Outer casing shadow
                        drawPath(
                            path = path,
                            color = Color.White.copy(alpha = 0.8f),
                            style = Stroke(
                                width = 10.dp.toPx(),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )

                        // Outer glow
                        drawPath(
                            path = path,
                            color = pathGlowColor,
                            style = Stroke(
                                width = 8.dp.toPx(),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )

                        // 2. Draw solid transit line
                        drawPath(
                            path = path,
                            brush = Brush.linearGradient(
                                colors = listOf(startColor, primaryColor, endColor),
                                start = screenPoints.first(),
                                end = screenPoints.last()
                            ),
                            style = Stroke(
                                width = 4.5.dp.toPx(),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )

                        // 3. Draw dashed center track for metro/transit look
                        drawPath(
                            path = path,
                            color = Color.White.copy(alpha = 0.7f),
                            style = Stroke(
                                width = 1.8.dp.toPx(),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f), 0f)
                            )
                        )

                        // 4. Draw Waypoint Nodes
                        for (idx in screenPoints.indices) {
                            val pt = screenPoints[idx]
                            val isFirst = idx == 0
                            val isLast = idx == screenPoints.size - 1
                            val isSelected = idx == selectedStopIndex

                            when {
                                isFirst -> {
                                    // Origin beacon
                                    drawCircle(Color.White, radius = 8.dp.toPx(), center = pt)
                                    drawCircle(startColor, radius = 6.dp.toPx(), center = pt)
                                    drawCircle(Color.White, radius = 2.dp.toPx(), center = pt)
                                }
                                isLast -> {
                                    // Destination beacon
                                    drawCircle(Color.White, radius = 8.dp.toPx(), center = pt)
                                    drawCircle(endColor, radius = 6.dp.toPx(), center = pt)
                                    drawCircle(Color.White, radius = 2.dp.toPx(), center = pt)
                                }
                                isSelected -> {
                                    // Selected node highlight
                                    drawCircle(primaryColor.copy(alpha = 0.3f), radius = 12.dp.toPx(), center = pt)
                                    drawCircle(Color.White, radius = 7.dp.toPx(), center = pt)
                                    drawCircle(primaryColor, radius = 5.dp.toPx(), center = pt)
                                }
                                else -> {
                                    // Regular transit stop node
                                    drawCircle(Color.White, radius = 3.5.dp.toPx(), center = pt)
                                    drawCircle(primaryColor, radius = 2.dp.toPx(), center = pt)
                                }
                            }
                        }
                    }
                }

                // Legend overlay (Top Left)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color(0xFF10B981), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (lang == AppLanguage.BANGLA) "শুরু" else "Start",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color(0xFFEF4444), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (lang == AppLanguage.BANGLA) "গন্তব্য" else "End",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Selected Stop Info Banner (Bottom Floating Badge)
                if (selectedStopIndex != null) {
                    val idx = selectedStopIndex!!
                    val stopPair = resolvedPoints.getOrNull(idx)
                    if (stopPair != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 4.dp,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 10.dp)
                                .clickable { selectedStopIndex = null }
                                .padding(horizontal = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Place,
                                    contentDescription = null,
                                    tint = if (idx == 0) Color(0xFF10B981)
                                    else if (idx == resolvedPoints.size - 1) Color(0xFFEF4444)
                                    else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${idx + 1}. ${stopPair.first}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (idx == 0) (if (lang == AppLanguage.BANGLA) "(শুরুর স্টপ)" else "(Origin)")
                                    else if (idx == resolvedPoints.size - 1) (if (lang == AppLanguage.BANGLA) "(শেষ স্টপ)" else "(Terminal)")
                                    else (if (lang == AppLanguage.BANGLA) "(স্টপেজ)" else "(Waypoint)"),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick summary strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (lang == AppLanguage.BANGLA)
                        "প্রথম স্টপ: ${stops.firstOrNull() ?: ""} → শেষ স্টপ: ${stops.lastOrNull() ?: ""}"
                    else
                        "From: ${stops.firstOrNull() ?: ""} → To: ${stops.lastOrNull() ?: ""}",
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
                        text = if (lang == AppLanguage.BANGLA) "গুগলে দেখুন" else "Google Maps",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(13.dp)
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

        // Google Maps Directions Intent with transit travelmode
        val mapUri = Uri.parse("https://www.google.com/maps/dir/?api=1&origin=$originParam&destination=$destParam&travelmode=transit")
        val intent = Intent(Intent.ACTION_VIEW, mapUri).apply {
            setPackage("com.google.android.apps.maps")
        }

        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            // Fallback to browser
            val webIntent = Intent(Intent.ACTION_VIEW, mapUri)
            context.startActivity(webIntent)
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Opening Google Maps...", Toast.LENGTH_SHORT).show()
        val fallbackUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode("$busName Bus Route Dhaka")}")
        context.startActivity(Intent(Intent.ACTION_VIEW, fallbackUri))
    }
}
