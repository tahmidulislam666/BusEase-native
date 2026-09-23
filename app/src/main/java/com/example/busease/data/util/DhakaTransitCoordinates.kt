package com.example.busease.data.util

/**
 * Coordinate model representing a geospatial or normalized route point.
 */
data class LatLng(val latitude: Double, val longitude: Double)

object DhakaTransitCoordinates {

    // Known approximate coordinates for Dhaka bus stops and major hubs
    private val KNOWN_STOPS: Map<String, LatLng> = mapOf(
        // North / Gazipur / Tongi / Uttara
        "gazipur" to LatLng(23.9985, 90.4203),
        "gazipur chowrasta" to LatLng(23.9985, 90.4203),
        "board bazar" to LatLng(23.9450, 90.3980),
        "tongi" to LatLng(23.8967, 90.4034),
        "station road" to LatLng(23.8920, 90.4020),
        "abdullahpur" to LatLng(23.8820, 90.3995),
        "house building" to LatLng(23.8735, 90.3982),
        "azampur" to LatLng(23.8682, 90.4011),
        "rajlakshmi" to LatLng(23.8617, 90.4016),
        "jashimuddin" to LatLng(23.8562, 90.4023),
        "jashimuddin (uttara)" to LatLng(23.8562, 90.4023),
        "airport" to LatLng(23.8510, 90.4078),
        "kawla" to LatLng(23.8410, 90.4130),
        "khilkhet" to LatLng(23.8298, 90.4215),

        // Kuril / Baridhara / Badda / Rampura
        "kuril" to LatLng(23.8188, 90.4188),
        "kuril bishwa road" to LatLng(23.8188, 90.4188),
        "jamuna future park" to LatLng(23.8130, 90.4240),
        "bashundhara" to LatLng(23.8150, 90.4260),
        "nadda" to LatLng(23.8050, 90.4230),
        "notun bazar" to LatLng(23.7980, 90.4230),
        "bashtola" to LatLng(23.7930, 90.4240),
        "shahjadpur" to LatLng(23.7900, 90.4250),
        "uttar badda" to LatLng(23.7885, 90.4260),
        "badda" to LatLng(23.7820, 90.4240),
        "badda link road" to LatLng(23.7810, 90.4220),
        "madhya badda" to LatLng(23.7770, 90.4245),
        "merul" to LatLng(23.7680, 90.4210),
        "rampura" to LatLng(23.7635, 90.4230),
        "rampura bridge" to LatLng(23.7635, 90.4230),
        "banasree" to LatLng(23.7610, 90.4350),
        "malibagh railgate" to LatLng(23.7540, 90.4200),
        "demra" to LatLng(23.7140, 90.4990),
        "demra staff quarter" to LatLng(23.7140, 90.4990),

        // Mohakhali / Banani / Gulshan
        "shewra" to LatLng(23.8080, 90.4140),
        "mes" to LatLng(23.8015, 90.4105),
        "staff road" to LatLng(23.7940, 90.4070),
        "kakali" to LatLng(23.7925, 90.4035),
        "banani" to LatLng(23.7937, 90.4066),
        "sainik club" to LatLng(23.7885, 90.4045),
        "chairman bari" to LatLng(23.7820, 90.4030),
        "mohakhali" to LatLng(23.7780, 90.4005),
        "wireless" to LatLng(23.7770, 90.4080),
        "gulshan 1" to LatLng(23.7780, 90.4160),
        "gulshan 2" to LatLng(23.7925, 90.4155),
        "police plaza" to LatLng(23.7730, 90.4160),
        "shooting club" to LatLng(23.7750, 90.4120),
        "nabisco" to LatLng(23.7700, 90.4040),
        "tivoli" to LatLng(23.7670, 90.4020),
        "satrasta" to LatLng(23.7620, 90.4000),

        // Mirpur / ECB / Kalshi
        "ecb square" to LatLng(23.8230, 90.3930),
        "kalshi" to LatLng(23.8210, 90.3780),
        "purobi" to LatLng(23.8120, 90.3660),
        "mirpur 12" to LatLng(23.8235, 90.3640),
        "mirpur 11" to LatLng(23.8150, 90.3650),
        "mirpur 11.5" to LatLng(23.8180, 90.3660),
        "mirpur 10" to LatLng(23.8070, 90.3685),
        "mirpur 14" to LatLng(23.7990, 90.3820),
        "mirpur 2" to LatLng(23.8020, 90.3570),
        "sony cinema hall" to LatLng(23.7990, 90.3560),
        "mirpur 1" to LatLng(23.7950, 90.3540),
        "ansar camp" to LatLng(23.7880, 90.3535),
        "technical" to LatLng(23.7810, 90.3530),
        "gabtoli" to LatLng(23.7825, 90.3475),
        "amin bazar" to LatLng(23.7860, 90.3340),
        "hemayetpur" to LatLng(23.7920, 90.2730),
        "savar" to LatLng(23.8440, 90.2600),
        "nabinagar" to LatLng(23.9180, 90.2740),
        "baipayl" to LatLng(23.9520, 90.2720),
        "epz" to LatLng(23.9480, 90.2710),

        // West / Ring Road / Shyamoli / Mohammadpur
        "kallyanpur" to LatLng(23.7770, 90.3600),
        "shyamoli" to LatLng(23.7725, 90.3645),
        "shishu mela" to LatLng(23.7705, 90.3670),
        "agargaon" to LatLng(23.7745, 90.3780),
        "bijoy sarani" to LatLng(23.7660, 90.3860),
        "jahangir gate" to LatLng(23.7710, 90.3900),
        "zia uddyan" to LatLng(23.7720, 90.3820),
        "khamar bari" to LatLng(23.7590, 90.3840),
        "college gate" to LatLng(23.7640, 90.3680),
        "asad gate" to LatLng(23.7600, 90.3715),
        "town hall" to LatLng(23.7610, 90.3640),
        "mohammadpur" to LatLng(23.7575, 90.3610),
        "shia mosque" to LatLng(23.7620, 90.3580),
        "japan garden city" to LatLng(23.7650, 90.3550),
        "adabor" to LatLng(23.7710, 90.3560),
        "beribadh" to LatLng(23.7530, 90.3450),
        "bosila" to LatLng(23.7480, 90.3420),

        // Central / Dhanmondi / Farmgate / Shahbag
        "farmgate" to LatLng(23.7570, 90.3888),
        "kawran bazar" to LatLng(23.7515, 90.3930),
        "bangla motor" to LatLng(23.7460, 90.3955),
        "paribagh" to LatLng(23.7420, 90.3955),
        "shahbag" to LatLng(23.7380, 90.3957),
        "dhanmondi 27" to LatLng(23.7540, 90.3740),
        "dhanmondi 32" to LatLng(23.7505, 90.3780),
        "kalabagan" to LatLng(23.7485, 90.3810),
        "city college" to LatLng(23.7405, 90.3820),
        "science lab" to LatLng(23.7410, 90.3830),
        "katabon" to LatLng(23.7390, 90.3890),
        "nilkhet" to LatLng(23.7330, 90.3860),
        "new market" to LatLng(23.7345, 90.3835),
        "azimpur" to LatLng(23.7285, 90.3850),
        "palashi" to LatLng(23.7265, 90.3890),
        "dhaka medical" to LatLng(23.7250, 90.3970),
        "chankhar pul" to LatLng(23.7220, 90.3980),

        // Downtown / Motijheel / Paltan / Old Dhaka
        "matsya bhaban" to LatLng(23.7325, 90.4030),
        "high court" to LatLng(23.7290, 90.4045),
        "press club" to LatLng(23.7280, 90.4070),
        "paltan" to LatLng(23.7310, 90.4125),
        "kakrail" to LatLng(23.7380, 90.4100),
        "shantinagar" to LatLng(23.7410, 90.4130),
        "malibagh moor" to LatLng(23.7470, 90.4150),
        "mouchak" to LatLng(23.7485, 90.4140),
        "moghbazar" to LatLng(23.7490, 90.4040),
        "gpo" to LatLng(23.7275, 90.4120),
        "golap shah mazar" to LatLng(23.7240, 90.4105),
        "gulistan" to LatLng(23.7230, 90.4125),
        "fulbaria" to LatLng(23.7215, 90.4090),
        "motijheel" to LatLng(23.7330, 90.4190),
        "dillu road" to LatLng(23.7440, 90.4020),
        "itfaq moor" to LatLng(23.7260, 90.4230),
        "kamalapur" to LatLng(23.7310, 90.4260),
        "tikatuli" to LatLng(23.7220, 90.4240),
        "rajdhani market" to LatLng(23.7210, 90.4230),
        "sayedabad" to LatLng(23.7180, 90.4280),
        "jatrabari" to LatLng(23.7120, 90.4360),
        "janapath" to LatLng(23.7130, 90.4310),
        "dhalpur" to LatLng(23.7150, 90.4320),
        "postogola" to LatLng(23.6960, 90.4330),
        "sadarghat" to LatLng(23.7085, 90.4135),
        "ray saheb bazar" to LatLng(23.7130, 90.4120),
        "naya bazar" to LatLng(23.7170, 90.4110),
        "babubazar" to LatLng(23.7140, 90.4070),
        "chawkbazar" to LatLng(23.7180, 90.3980),
        "lalbagh" to LatLng(23.7190, 90.3880)
    )

    /**
     * Resolves coordinates for a stop name. If not directly known, calculates
     * a smooth interpolated point based on surrounding matched stops or default bounds.
     */
    fun getStopCoordinate(name: String): LatLng? {
        val clean = name.lowercase().trim()
        KNOWN_STOPS[clean]?.let { return it }

        for ((key, coord) in KNOWN_STOPS) {
            if (clean.contains(key) || key.contains(clean)) {
                return coord
            }
        }
        return null
    }

    /**
     * Given a list of stop names on a bus route, produces an ordered polyline
     * of coordinates with sensible interpolation so the route path displays cleanly.
     */
    fun resolveRoutePolyline(stops: List<String>): List<Pair<String, LatLng>> {
        if (stops.isEmpty()) return emptyList()

        val resolved = mutableListOf<Pair<String, LatLng?>>()
        for (stop in stops) {
            resolved.add(stop to getStopCoordinate(stop))
        }

        // Check if we have at least one known coordinate
        val firstKnownIdx = resolved.indexOfFirst { it.second != null }
        if (firstKnownIdx == -1) {
            // Generate a synthetic diagonal across Dhaka center (e.g. Mirpur to Motijheel)
            return stops.mapIndexed { idx, stop ->
                val ratio = if (stops.size > 1) idx.toDouble() / (stops.size - 1) else 0.5
                val lat = 23.8200 - ratio * 0.1000
                val lng = 90.3600 + ratio * 0.0600
                stop to LatLng(lat, lng)
            }
        }

        // Interpolate missing coordinates between known anchors
        val points = mutableListOf<Pair<String, LatLng>>()
        var lastKnown = resolved[firstKnownIdx].second!!
        var lastKnownIdx = firstKnownIdx

        // Fill pre-first-known points with slight offset
        for (i in 0 until firstKnownIdx) {
            val dist = (firstKnownIdx - i).toDouble()
            points.add(resolved[i].first to LatLng(lastKnown.latitude + dist * 0.003, lastKnown.longitude - dist * 0.002))
        }

        // Main pass with linear interpolation between anchors
        var i = firstKnownIdx
        while (i < resolved.size) {
            val currentCoord = resolved[i].second
            if (currentCoord != null) {
                points.add(resolved[i].first to currentCoord)
                lastKnown = currentCoord
                lastKnownIdx = i
                i++
            } else {
                // Find next known
                val nextKnownIdx = (i + 1 until resolved.size).firstOrNull { resolved[it].second != null }
                if (nextKnownIdx != null) {
                    val nextKnownCoord = resolved[nextKnownIdx].second!!
                    val totalGap = (nextKnownIdx - lastKnownIdx).toDouble()
                    while (i < nextKnownIdx) {
                        val frac = (i - lastKnownIdx) / totalGap
                        val interLat = lastKnown.latitude + frac * (nextKnownCoord.latitude - lastKnown.latitude)
                        val interLng = lastKnown.longitude + frac * (nextKnownCoord.longitude - lastKnown.longitude)
                        points.add(resolved[i].first to LatLng(interLat, interLng))
                        i++
                    }
                } else {
                    // No next known, drift slightly forward
                    val step = (i - lastKnownIdx).toDouble()
                    val latOffset = if (lastKnown.latitude > 23.74) -0.004 * step else 0.004 * step
                    val lngOffset = 0.002 * step
                    points.add(resolved[i].first to LatLng(lastKnown.latitude + latOffset, lastKnown.longitude + lngOffset))
                    i++
                }
            }
        }

        return points
    }

    /**
     * Calculates distance in meters between two coordinates using the Haversine formula.
     */
    fun calculateDistanceMeters(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val r = 6371000.0 // Earth radius in meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return r * c
    }

    /**
     * Returns all known stops situated within the given radius (in meters) of the user's location.
     * Returns pairs of stop name and distance in meters, sorted ascending by distance.
     */
    fun getStopsWithinRadius(
        userLat: Double,
        userLon: Double,
        radiusMeters: Double = 1000.0
    ): List<Pair<String, Double>> {
        val matchingStops = mutableListOf<Pair<String, Double>>()
        for ((stopName, coord) in KNOWN_STOPS) {
            val dist = calculateDistanceMeters(userLat, userLon, coord.latitude, coord.longitude)
            if (dist <= radiusMeters) {
                matchingStops.add(stopName to dist)
            }
        }
        return matchingStops.sortedBy { it.second }
    }
}
