package eu.kanade.tachiyomi.animeextension.en.donghuastream

import android.util.Log
import eu.kanade.tachiyomi.animeextension.en.donghuastream.extractors.StreamPlayExtractor
import eu.kanade.tachiyomi.animesource.model.Video
import eu.kanade.tachiyomi.lib.dailymotionextractor.DailymotionExtractor
import eu.kanade.tachiyomi.multisrc.animestream.AnimeStream
import eu.kanade.tachiyomi.network.GET

class DonghuaStream : AnimeStream(
    "en",
    "DonghuaStream",
    "https://donghuastream.org",
) {
    override val fetchFilters: Boolean
        get() = false

    override fun popularAnimeRequest(page: Int) = GET("$baseUrl/anime/?page=$page&order=popular")
    override fun latestUpdatesRequest(page: Int) = GET("$baseUrl/anime/?page=$page&order=update")

    // ============================ Video Links =============================

    private val dailymotionExtractor by lazy { DailymotionExtractor(client, headers) }
    private val streamPlayExtractor by lazy { StreamPlayExtractor(client, headers) }

    override fun getVideoList(url: String, name: String): List<Video> {
        Log.i(name, "getVideoList -> URL => $url || Name => $name")
        val prefix = "$name - "
        return when {
            url.contains("dailymotion") -> dailymotionExtractor.videosFromUrl(url, prefix = prefix)
            url.contains("streamplay") -> streamPlayExtractor.videosFromUrl(url, prefix = prefix)
            url.contains("rumple") -> streamPlayExtractor.videosFromUrl(url, prefix = prefix)
            else -> emptyList()
        }
    }

    // ============================= Utilities ==============================

    override fun List<Video>.sort(): List<Video> {
        val quality = preferences.getString(videoSortPrefKey, videoSortPrefDefault)!!
        return sortedWith(
            compareBy(
                { it.quality.contains(quality) },
                { Regex("""(\d+)p""").find(it.quality)?.groupValues?.get(1)?.toIntOrNull() ?: 0 },
            ),
        ).reversed()
    }
}
