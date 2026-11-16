package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.sources.DailyHadithDataSource
import com.example.data.sources.NawawiDataSource
import com.example.data.sources.QuranDataSource
import com.example.utils.PrayerTimeCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("سكينة الإسلامي", appName)
    }

    @Test
    fun `verify full forty nawawi hadiths loaded`() {
        assertEquals(42, NawawiDataSource.hadiths.size)
        NawawiDataSource.hadiths.forEach { hadith ->
            assertTrue(hadith.matn.isNotBlank())
            assertTrue(hadith.reference.isNotBlank())
            assertTrue(hadith.explanation.isNotBlank())
            assertTrue(hadith.benefits.isNotEmpty())
        }
    }

    @Test
    fun `verify 114 quran surahs and reciters`() {
        assertEquals(114, QuranDataSource.surahs.size)
        assertTrue(QuranDataSource.reciters.size >= 4)
    }

    @Test
    fun `verify hijri calendar and islamic occasions`() {
        val todayHijri = com.example.utils.HijriCalendarHelper.getTodayHijri()
        assertTrue(todayHijri.day in 1..30)
        assertTrue(todayHijri.month in 1..12)
        assertTrue(todayHijri.year >= 1445)
        assertTrue(todayHijri.monthNameArabic.isNotBlank())

        val occasions = com.example.utils.HijriCalendarHelper.ISLAMIC_OCCASIONS
        assertTrue(occasions.isNotEmpty())

        val monthInfo = com.example.utils.HijriCalendarHelper.getHijriMonthInfo(todayHijri.year, todayHijri.month)
        assertTrue(monthInfo.daysCount in 29..30)
        assertEquals(monthInfo.daysCount, monthInfo.days.size)
    }

    @Test
    fun `verify prayer times calculator and daily hadith`() {
        val prayers = PrayerTimeCalculator.calculateTodayPrayers()
        assertNotNull(prayers.fajr)
        assertNotNull(prayers.maghrib)

        val countdown = PrayerTimeCalculator.getNextPrayerCountdown()
        assertTrue(countdown.remainingFormatted.isNotBlank())

        val dailyHadith = DailyHadithDataSource.getTodayHadith()
        assertTrue(dailyHadith.matn.isNotBlank())
    }
}
