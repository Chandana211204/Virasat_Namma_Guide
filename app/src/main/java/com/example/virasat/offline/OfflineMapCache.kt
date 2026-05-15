package com.example.virasat.offline

import android.content.Context
import com.example.virasat.data.HeritageRepository
import com.example.virasat.data.HeritageSite
import org.json.JSONArray
import org.json.JSONObject

object OfflineMapCache {
    private const val PREF_NAME = "virasat_offline_map_cache"
    private const val KEY_SITES = "cached_sites"

    fun refreshCache(context: Context) {
        val array = JSONArray()

        HeritageRepository.sites.forEach { site ->
            array.put(
                JSONObject()
                    .put("id", site.id)
                    .put("name", site.name)
                    .put("location", site.location)
                    .put("latitude", site.latitude)
                    .put("longitude", site.longitude)
            )
        }

        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_SITES, array.toString())
            .apply()
    }

    fun getCachedSites(context: Context): List<CachedHeritageLocation> {
        val raw = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_SITES, null)
            ?: return HeritageRepository.sites.map { it.toCachedLocation() }

        val array = JSONArray(raw)
        return List(array.length()) { index ->
            val item = array.getJSONObject(index)
            CachedHeritageLocation(
                id = item.getString("id"),
                name = item.getString("name"),
                location = item.getString("location"),
                latitude = item.getDouble("latitude"),
                longitude = item.getDouble("longitude")
            )
        }
    }

    fun getCachedSite(context: Context, siteId: String): CachedHeritageLocation? {
        return getCachedSites(context).find { it.id == siteId }
    }

    private fun HeritageSite.toCachedLocation(): CachedHeritageLocation {
        return CachedHeritageLocation(id, name, location, latitude, longitude)
    }
}

data class CachedHeritageLocation(
    val id: String,
    val name: String,
    val location: String,
    val latitude: Double,
    val longitude: Double
)
