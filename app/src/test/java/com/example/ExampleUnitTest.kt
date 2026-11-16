package com.example

import com.example.utils.HijriCalendarHelper
import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar

class ExampleUnitTest {
    @Test
    fun testHijriConversionNotNull() {
        val cal = Calendar.getInstance()
        val hijriDate = HijriCalendarHelper.gregorianToHijri(cal)
        assertTrue(hijriDate.day in 1..30)
        assertTrue(hijriDate.month in 1..12)
        assertTrue(hijriDate.year >= 1445)
        assertNotNull(hijriDate.monthNameArabic)
        assertNotNull(hijriDate.dayNameArabic)
        assertNotNull(hijriDate.gregorianFormatted)
    }

    @Test
    fun testIslamicOccasionsIntegrity() {
        val occasions = HijriCalendarHelper.ISLAMIC_OCCASIONS
        assertTrue("Occasions list should not be empty", occasions.size >= 10)
        assertTrue("Ashura should be in Muharram (month 1)", occasions.any { it.id == "ashura" && it.hijriMonth == 1 })
        assertTrue("Eid al-Fitr should be in Shawwal (month 10)", occasions.any { it.id == "eid_al_fitr" && it.hijriMonth == 10 })
        assertTrue("Arafah should be in Dhu al-Hijjah (month 12)", occasions.any { it.id == "arafah" && it.hijriMonth == 12 })
    }

    @Test
    fun testMonthInfoGeneration() {
        val monthInfo = HijriCalendarHelper.getHijriMonthInfo(1448, 9) // Ramadan
        assertEquals(9, monthInfo.monthNumber)
        assertEquals("رَمَضَان", monthInfo.monthNameArabic)
        assertTrue(monthInfo.daysCount in 29..30)
        assertEquals(monthInfo.daysCount, monthInfo.days.size)
    }

    @Test
    fun testQiblaCalculations() {
        // Cairo coordinates: 30.0444 N, 31.2357 E
        val cairoLat = 30.0444
        val cairoLng = 31.2357
        val qiblaBearing = com.example.utils.QiblaCalculator.calculateQiblaBearing(cairoLat, cairoLng)
        // Cairo Qibla azimuth is approximately 136°
        assertTrue("Cairo Qibla bearing should be around 136°", qiblaBearing in 134f..138f)

        val direction = com.example.utils.QiblaCalculator.getDirectionArabic(qiblaBearing)
        assertEquals("جنوب شرق", direction)

        val distance = com.example.utils.QiblaCalculator.calculateDistanceToMakkahKm(cairoLat, cairoLng)
        assertTrue("Distance between Cairo and Makkah should be ~1280 km", distance in 1200.0..1400.0)

        // Madinah coordinates: 24.4672 N, 39.6111 E (almost directly north of Makkah)
        val madinahLat = 24.4672
        val madinahLng = 39.6111
        val madinahQibla = com.example.utils.QiblaCalculator.calculateQiblaBearing(madinahLat, madinahLng)
        assertTrue("Madinah Qibla bearing should be ~176° (South)", madinahQibla in 170f..180f)
    }

    @Test
    fun testMadinahQuran604Pages() {
        // Page 1 must be Al-Fatihah (7 verses)
        val page1 = com.example.data.sources.QuranDataSource.getPageData(1)
        assertEquals(1, page1.pageNumber)
        assertEquals(1, page1.juzNumber)
        assertEquals(1, page1.hizbNumber)
        assertEquals("الفَاتِحَة", page1.surah.nameArabic)
        assertEquals(7, page1.ayahs.size)
        assertTrue(page1.isSurahStart)

        // Page 2 must be Al-Baqarah verses 1 to 5 (Alif-Lam-Mim ... Muflihoon)
        val page2 = com.example.data.sources.QuranDataSource.getPageData(2)
        assertEquals(2, page2.pageNumber)
        assertEquals(1, page2.juzNumber)
        assertEquals(1, page2.hizbNumber)
        assertEquals("البَقَرَة", page2.surah.nameArabic)
        assertTrue(page2.isSurahStart)
        assertEquals(5, page2.ayahs.size)
        assertEquals(1, page2.ayahs[0].verseNumber)
        assertTrue(page2.ayahs[0].textArabic.contains("الم"))
        assertEquals(5, page2.ayahs[4].verseNumber)
        assertTrue(page2.ayahs[4].textArabic.contains("الْمُفْلِحُونَ"))

        // Page 604 must be in Juz 30 and Hizb 60
        val page604 = com.example.data.sources.QuranDataSource.getPageData(604)
        assertEquals(604, page604.pageNumber)
        assertEquals(30, page604.juzNumber)
        assertEquals(60, page604.hizbNumber)
    }

    @Test
    fun testEgyptianSurveyAuthorityPrayerTimes() {
        val cairoLat = 30.0444
        val cairoLng = 31.2357
        val prayers = com.example.utils.PrayerTimeCalculator.computePrayerTimes(cairoLat, cairoLng)
        // Verify prayer hour validity
        assertTrue("Fajr hour should be between 3 and 6 AM", prayers.fajrH in 3..6)
        assertTrue("Sunrise should be between 5 and 7 AM", prayers.sunH in 5..7)
        assertTrue("Dhuhr should be around 11 to 13", prayers.dhuhrH in 11..13)
        assertTrue("Asr should be in the afternoon 14 to 17", prayers.asrH in 14..17)
        assertTrue("Maghrib should be in the evening 17 to 20", prayers.maghribH in 17..20)
        assertTrue("Isha should be in the night 18 to 22", prayers.ishaH in 18..22)
    }
}
