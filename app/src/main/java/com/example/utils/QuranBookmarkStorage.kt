package com.example.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.data.models.QuranBookmark
import com.example.data.sources.QuranDataSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

class QuranBookmarkStorage(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("sakina_quran_bookmarks_prefs", Context.MODE_PRIVATE)

    private val _lastReadPage = MutableStateFlow(loadLastReadPage())
    val lastReadPage: StateFlow<Int> = _lastReadPage.asStateFlow()

    private val _bookmarks = MutableStateFlow(loadBookmarks())
    val bookmarks: StateFlow<List<QuranBookmark>> = _bookmarks.asStateFlow()

    private fun loadLastReadPage(): Int {
        return prefs.getInt("quran_last_read_page", 1).coerceIn(1, 604)
    }

    private fun loadBookmarks(): List<QuranBookmark> {
        val jsonString = prefs.getString("quran_bookmarks_list", null) ?: return emptyList()
        val list = mutableListOf<QuranBookmark>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    QuranBookmark(
                        id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                        pageNumber = obj.optInt("pageNumber", 1).coerceIn(1, 604),
                        surahName = obj.optString("surahName", ""),
                        surahId = obj.optInt("surahId", 1),
                        juzNumber = obj.optInt("juzNumber", 1),
                        hizbNumber = obj.optInt("hizbNumber", 1),
                        note = obj.optString("note", ""),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
        } catch (_: Exception) {
            return emptyList()
        }
        return list.sortedByDescending { it.timestamp }
    }

    private fun persistBookmarks(list: List<QuranBookmark>) {
        try {
            val jsonArray = JSONArray()
            list.forEach { item ->
                val obj = JSONObject().apply {
                    put("id", item.id)
                    put("pageNumber", item.pageNumber)
                    put("surahName", item.surahName)
                    put("surahId", item.surahId)
                    put("juzNumber", item.juzNumber)
                    put("hizbNumber", item.hizbNumber)
                    put("note", item.note)
                    put("timestamp", item.timestamp)
                }
                jsonArray.put(obj)
            }
            prefs.edit().putString("quran_bookmarks_list", jsonArray.toString()).apply()
        } catch (_: Exception) {}
        _bookmarks.value = list
    }

    fun setLastReadPage(page: Int) {
        val clamped = page.coerceIn(1, 604)
        if (_lastReadPage.value != clamped) {
            prefs.edit().putInt("quran_last_read_page", clamped).apply()
            _lastReadPage.value = clamped
        }
    }

    fun saveBookmark(pageNumber: Int, note: String = ""): QuranBookmark {
        val pageData = QuranDataSource.getPageData(pageNumber.coerceIn(1, 604))
        val currentList = _bookmarks.value.toMutableList()

        // Remove if page already exists to update it with new timestamp/note
        currentList.removeAll { it.pageNumber == pageData.pageNumber }

        val newBookmark = QuranBookmark(
            pageNumber = pageData.pageNumber,
            surahName = pageData.surah.nameArabic,
            surahId = pageData.surah.id,
            juzNumber = pageData.juzNumber,
            hizbNumber = pageData.hizbNumber,
            note = note.trim(),
            timestamp = System.currentTimeMillis()
        )

        currentList.add(0, newBookmark)
        persistBookmarks(currentList)
        return newBookmark
    }

    fun removeBookmark(pageNumber: Int) {
        val currentList = _bookmarks.value.toMutableList()
        val removed = currentList.removeAll { it.pageNumber == pageNumber }
        if (removed) {
            persistBookmarks(currentList)
        }
    }

    fun removeBookmarkById(id: String) {
        val currentList = _bookmarks.value.toMutableList()
        val removed = currentList.removeAll { it.id == id }
        if (removed) {
            persistBookmarks(currentList)
        }
    }

    fun toggleBookmark(pageNumber: Int, note: String = ""): Boolean {
        return if (isPageBookmarked(pageNumber)) {
            removeBookmark(pageNumber)
            false
        } else {
            saveBookmark(pageNumber, note)
            true
        }
    }

    fun isPageBookmarked(pageNumber: Int): Boolean {
        return _bookmarks.value.any { it.pageNumber == pageNumber }
    }

    fun getBookmarkForPage(pageNumber: Int): QuranBookmark? {
        return _bookmarks.value.find { it.pageNumber == pageNumber }
    }
}
