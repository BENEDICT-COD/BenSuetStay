package com.mansuetdev.bensuetstay

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

/**
 * REST API: OpenStreetMap Nominatim geocoding (https://nominatim.org, free, no key).
 *   GET /search?q=<address>&format=json&limit=1&countrycodes=za  ->  [{"lat":"..","lon":".."}]
 * Used to turn a listing's location and a student's campus into coordinates so the app can
 * show the distance between them. Results are cached (memory + SharedPreferences), and
 * requests are sent one at a time, at most one per second, as Nominatim's usage policy asks.
 */
object GeocodingApi {
    data class Place(val latitude: Double, val longitude: Double)

    private const val ENDPOINT = "https://nominatim.openstreetmap.org/search"
    private const val USER_AGENT = "BenSuetStay/1.0 (Android student accommodation app; github.com/BENEDICT-COD/BenSuetStay)"
    private const val TAG = "GeocodingApi"

    private val executor = Executors.newSingleThreadExecutor()
    private val mainThread = Handler(Looper.getMainLooper())
    private val memory = ConcurrentHashMap<String, Place>()
    @Volatile private var lastRequestAt = 0L

    /** Settings → Offline & data → Clear saved locations. */
    fun clearCache(context: Context) {
        memory.clear()
        context.applicationContext.getSharedPreferences("geocode_cache", Context.MODE_PRIVATE).edit().clear().apply()
    }

    /** Looks up [query] (a South African address or campus). Calls back on the main thread; null if not found. */
    fun search(context: Context, query: String, callback: (Place?) -> Unit) {
        val key = query.trim().lowercase()
        if (key.isEmpty()) { callback(null); return }
        memory[key]?.let { callback(it); return }
        val prefs = context.applicationContext.getSharedPreferences("geocode_cache", Context.MODE_PRIVATE)
        prefs.getString(key, null)?.split(",")?.mapNotNull { it.toDoubleOrNull() }?.takeIf { it.size == 2 }?.let {
            val place = Place(it[0], it[1]); memory[key] = place; callback(place); return
        }
        executor.execute {
            val place = try { request(query) } catch (e: Exception) { Log.w(TAG, "Geocoding failed for '$query'", e); null }
            if (place != null) {
                memory[key] = place
                prefs.edit().putString(key, "${place.latitude},${place.longitude}").apply()
            }
            mainThread.post { callback(place) }
        }
    }

    private fun request(query: String): Place? {
        val wait = 1100 - (System.currentTimeMillis() - lastRequestAt)
        if (wait > 0) Thread.sleep(wait)
        lastRequestAt = System.currentTimeMillis()

        val url = URL("$ENDPOINT?format=json&limit=1&countrycodes=za&q=" + URLEncoder.encode(query, "UTF-8"))
        val connection = url.openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", USER_AGENT)
            connection.setRequestProperty("Accept", "application/json")
            connection.connectTimeout = 10_000
            connection.readTimeout = 10_000
            if (connection.responseCode != HttpURLConnection.HTTP_OK) return null
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            val results = JSONArray(body)
            if (results.length() == 0) return null
            val first = results.getJSONObject(0)
            return Place(first.getString("lat").toDouble(), first.getString("lon").toDouble())
        } finally {
            connection.disconnect()
        }
    }
}
