package com.example.utils

import com.example.data.models.HijriDate
import com.example.data.models.HijriMonthInfo
import com.example.data.models.IslamicOccasion
import java.util.Calendar
import java.util.Date
import kotlin.math.floor

object HijriCalendarHelper {

    val HIJRI_MONTH_NAMES = listOf(
        "مُحَرَّم",
        "صَفَر",
        "رَبِيع الأَوَّل",
        "رَبِيع الآخِر",
        "جُمَادَى الأُولَى",
        "جُمَادَى الآخِرَة",
        "رَجَب",
        "شَعْبَان",
        "رَمَضَان",
        "شَوَّال",
        "ذُو القَعْدَة",
        "ذُو الحِجَّة"
    )

    val GREGORIAN_MONTH_NAMES_AR = listOf(
        "يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو",
        "يوليو", "أغسطس", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر"
    )

    val ARABIC_DAY_NAMES = listOf(
        "الأحد", "الاثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة", "السبت"
    )

    // Complete list of Islamic Occasions across the Hijri Year
    val ISLAMIC_OCCASIONS: List<IslamicOccasion> = listOf(
        IslamicOccasion(
            id = "islamic_new_year",
            title = "رأس السنة الهجرية",
            description = "بداية العام الهجري الجديد، تخليداً لذكرى هجرة النبي ﷺ من مكة المكرمة إلى المدينة المنورة.",
            hijriDay = 1,
            hijriMonth = 1,
            recommendedActs = "الدعاء ببركة العام الجديد، الاستغفار، ومحاسبة النفس."
        ),
        IslamicOccasion(
            id = "ashura",
            title = "يوم عاشوراء",
            description = "اليوم العاشر من شهر محرم، اليوم الذي نجى الله فيه موسى عليه السلام وقومه من فرعون.",
            hijriDay = 10,
            hijriMonth = 1,
            recommendedActs = "صيام يوم عاشوراء وصيام يوم تاسوعاء قبله، وهو يكفّر ذنوب سنة ماضية."
        ),
        IslamicOccasion(
            id = "mawlid",
            title = "المولد النبوي الشريف",
            description = "ذكرى مولد خير البرية وخاتم الأنبياء والمرسلين سيدنا محمد ﷺ.",
            hijriDay = 12,
            hijriMonth = 3,
            recommendedActs = "الإكثار من الصلاة والسلام على رسول الله ﷺ، ودراسة سيرته العطرة."
        ),
        IslamicOccasion(
            id = "isra_miraj",
            title = "ذكرى الإسراء والمعراج",
            description = "معجزة إسراء النبي ﷺ من المسجد الحرام إلى المسجد الأقصى وعروجه إلى السماوات العُلا وفرض الصلوات الخمس.",
            hijriDay = 27,
            hijriMonth = 7,
            recommendedActs = "المحافظة على الصلاة بخشوع، الذكر، والتدبر في عظمة قدرة الله تعالى."
        ),
        IslamicOccasion(
            id = "mid_shaban",
            title = "ليلة النصف من شعبان",
            description = "ليلة مباركة تُرفع فيها الأعمال إلى الله تعالى وفيها فضل عظيم ومغفرة للعباد.",
            hijriDay = 15,
            hijriMonth = 8,
            recommendedActs = "قيام الليل، الاستغفار، الدعاء، وصيام نهارها."
        ),
        IslamicOccasion(
            id = "ramadan_start",
            title = "أول أيام شهر رمضان المبارك",
            description = "شهر القرآن والرحمة والمغفرة والعتق من النار وفيه ليلة القدر.",
            hijriDay = 1,
            hijriMonth = 9,
            recommendedActs = "الصيام، تلاوة القرآن، التراويح، الصدقة، وصلة الأرحام."
        ),
        IslamicOccasion(
            id = "badr",
            title = "ذكرى غزوة بدر الكبرى (يوم الفرقان)",
            description = "أول معركة فاصلة في تاريخ الإسلام نصر الله فيها نبيه والمؤمنين نصراً مؤزراً.",
            hijriDay = 17,
            hijriMonth = 9,
            recommendedActs = "الدعاء للأمة الإسلامية والشكر على نعمة الإيمان."
        ),
        IslamicOccasion(
            id = "laylat_al_qadr",
            title = "العشر الأواخر وليلة القدر",
            description = "ليلة القدر خير من ألف شهر، تنزل الملائكة والروح فيها بإذن ربهم من كل أمر.",
            hijriDay = 27,
            hijriMonth = 9,
            recommendedActs = "الاعتكاف، التهجد، الإكثار من: (اللهم إنك عفو تحب العفو فاعفُ عني)."
        ),
        IslamicOccasion(
            id = "eid_al_fitr",
            title = "عيد الفطر المبارك",
            description = "يوم الفرح والسرور وجائزة الصائمين بعد إتمام فريضة صيام رمضان.",
            hijriDay = 1,
            hijriMonth = 10,
            recommendedActs = "صلاة العيد، التكبير، إخراج زكاة الفطر، صلة الرحم وإدخال السرور على الأهل والفقراء."
        ),
        IslamicOccasion(
            id = "dhul_hijjah_ten",
            title = "الأيام العشر الأوائل من ذي الحجة",
            description = "أفضل أيام الدنيا، العمل الصالح فيها أحب إلى الله من أي أيام أخرى.",
            hijriDay = 1,
            hijriMonth = 12,
            recommendedActs = "الإكثار من التهليل والتكبير والتحميد، الصيام، والصدقة."
        ),
        IslamicOccasion(
            id = "arafah",
            title = "يوم عرفة المبارك (الحج الأكبر)",
            description = "أعظم أيام العام، وقوف الحجيج بصعيد عرفات وصيامه لغير الحاج يكفر سنتين (ماضية ومقبلة).",
            hijriDay = 9,
            hijriMonth = 12,
            recommendedActs = "صيام يوم عرفة، الإكثار من الدعاء: (خير الدعاء دعاء يوم عرفة، وخير ما قلت أنا والنبيون من قبلي: لا إله إلا الله وحده لا شريك له...)."
        ),
        IslamicOccasion(
            id = "eid_al_adha",
            title = "عيد الأضحى المبارك (يوم النحر)",
            description = "عيد التضحية والفداء واقتداء بسيدنا إبراهيم وسيدنا إسماعيل عليهما السلام.",
            hijriDay = 10,
            hijriMonth = 12,
            recommendedActs = "صلاة العيد، الأضحية، التكبير المطلق والمقيد، صلة الرحم والابتهاج."
        ),
        IslamicOccasion(
            id = "tashreeq",
            title = "أيام التشريق",
            description = "أيام أكل وشرب وذكر لله تعالى تلي يوم النحر (11، 12، 13 من ذي الحجة).",
            hijriDay = 11,
            hijriMonth = 12,
            recommendedActs = "التكبير عقب الصلوات، ذكر الله تعالى، ويحرم صيامها إلا للمتمتع الذي لم يجد الهدي."
        )
    )

    /**
     * Converts a Gregorian Date to Hijri Date.
     * Uses Kuwaiti / astronomical standard algorithm with optional day offset.
     */
    fun gregorianToHijri(calendar: Calendar, dayOffset: Int = 0): HijriDate {
        val cal = Calendar.getInstance().apply {
            time = calendar.time
            add(Calendar.DAY_OF_MONTH, dayOffset)
        }

        val gYear = cal.get(Calendar.YEAR)
        val gMonth = cal.get(Calendar.MONTH) + 1 // 1-12
        val gDay = cal.get(Calendar.DAY_OF_MONTH)
        val dayOfWeekIndex = cal.get(Calendar.DAY_OF_WEEK) - 1 // 0=Sunday..6=Saturday
        val dayName = ARABIC_DAY_NAMES.getOrElse(dayOfWeekIndex) { "اليوم" }

        // Julian Day Number Calculation
        val a = floor((14 - gMonth.toDouble()) / 12).toInt()
        val y = gYear + 4800 - a
        val m = gMonth + 12 * a - 3
        val jd = gDay + floor((153 * m + 2) / 5.0).toInt() + 365 * y +
                floor(y / 4.0).toInt() - floor(y / 100.0).toInt() +
                floor(y / 400.0).toInt() - 32045

        // Conversion from Julian Day to Hijri
        val l = jd - 1948440 + 10632
        val n = floor((l - 1) / 10631.0).toInt()
        val l1 = l - 10631 * n + 354
        val j = (floor((10985 - l1) / 5316.0) * floor((50 * l1) / 17719.0) +
                floor(l1 / 5670.0) * floor((43 * l1) / 15238.0)).toInt()
        val l2 = l1 - (floor((30 - j) / 15.0) * floor((17719 * j) / 50.0) +
                floor(j / 16.0) * floor((15238 * j) / 43.0)).toInt() + 29
        val hMonth = floor((24 * l2) / 709.0).toInt()
        val hDay = l2 - floor((709 * hMonth) / 24.0).toInt()
        val hYear = 30 * n + j - 30

        val monthName = HIJRI_MONTH_NAMES.getOrElse(hMonth - 1) { "شهر هجري" }
        val gMonthName = GREGORIAN_MONTH_NAMES_AR.getOrElse(gMonth - 1) { "" }
        val gFormatted = "$gDay $gMonthName $gYear م"

        // Occasions for this specific Hijri day and month
        val occasions = getOccasionsFor(hDay, hMonth)
        val isWhiteDay = (hDay in 13..15)
        val isFastingSunnah = (dayOfWeekIndex == Calendar.MONDAY - 1 || dayOfWeekIndex == Calendar.THURSDAY - 1)

        return HijriDate(
            day = hDay,
            month = hMonth,
            year = hYear,
            monthNameArabic = monthName,
            dayNameArabic = dayName,
            gregorianDay = gDay,
            gregorianMonth = gMonth,
            gregorianYear = gYear,
            gregorianFormatted = gFormatted,
            occasions = occasions,
            isWhiteDay = isWhiteDay,
            isFastingSunnah = isFastingSunnah
        )
    }

    /**
     * Converts a Hijri date back to approximate Gregorian Calendar.
     */
    fun hijriToGregorian(hYear: Int, hMonth: Int, hDay: Int): Calendar {
        val jd = floor((11 * hYear + 3) / 30.0).toInt() + 354 * hYear + 30 * hMonth -
                floor((hMonth - 1) / 2.0).toInt() + hDay + 1948440 - 385

        val l = jd + 68569
        val n = floor((4 * l) / 146097.0).toInt()
        val l1 = l - floor((146097 * n + 3) / 4.0).toInt()
        val i = floor((4000 * (l1 + 1)) / 1461001.0).toInt()
        val l2 = l1 - floor((1461 * i) / 4.0).toInt() + 31
        val j = floor((80 * l2) / 2447.0).toInt()
        val gDay = l2 - floor((2447 * j) / 80.0).toInt()
        val l3 = floor(j / 11.0).toInt()
        val gMonth = j + 2 - 12 * l3
        val gYear = 100 * (n - 49) + i + l3

        return Calendar.getInstance().apply {
            set(Calendar.YEAR, gYear)
            set(Calendar.MONTH, gMonth - 1)
            set(Calendar.DAY_OF_MONTH, gDay)
        }
    }

    fun getTodayHijri(dayOffset: Int = 0): HijriDate {
        return gregorianToHijri(Calendar.getInstance(), dayOffset)
    }

    fun getOccasionsFor(hijriDay: Int, hijriMonth: Int): List<IslamicOccasion> {
        return ISLAMIC_OCCASIONS.filter { it.hijriMonth == hijriMonth && it.hijriDay == hijriDay }
    }

    /**
     * Returns days count for a given Hijri month (alternating 30 and 29, with leap year rule).
     */
    fun getHijriMonthDaysCount(year: Int, month: Int): Int {
        if (month % 2 != 0) return 30
        if (month != 12) return 29
        // 12th month has 30 days in leap years
        val isLeapYear = ((11 * year + 14) % 30) < 11
        return if (isLeapYear) 30 else 29
    }

    /**
     * Generates all days of a given Hijri Month.
     */
    fun getHijriMonthInfo(year: Int, month: Int, dayOffset: Int = 0): HijriMonthInfo {
        val daysCount = getHijriMonthDaysCount(year, month)
        val monthName = HIJRI_MONTH_NAMES.getOrElse(month - 1) { "" }

        val daysList = mutableListOf<HijriDate>()
        for (d in 1..daysCount) {
            val gregCal = hijriToGregorian(year, month, d)
            // Re-convert to sync
            val hijriDate = gregorianToHijri(gregCal, dayOffset).copy(
                day = d,
                month = month,
                year = year,
                monthNameArabic = monthName,
                occasions = getOccasionsFor(d, month),
                isWhiteDay = (d in 13..15)
            )
            daysList.add(hijriDate)
        }

        return HijriMonthInfo(
            monthNumber = month,
            monthNameArabic = monthName,
            year = year,
            daysCount = daysCount,
            days = daysList
        )
    }

    /**
     * Finds the next upcoming Islamic occasion relative to today.
     */
    fun getNextUpcomingOccasion(currentHijri: HijriDate): Pair<IslamicOccasion, Int>? {
        // Calculate remaining days for all occasions in current year or next year
        var minDiff = Int.MAX_VALUE
        var closestOccasion: IslamicOccasion? = null

        val currentDayOfYear = (currentHijri.month - 1) * 30 + currentHijri.day

        for (occ in ISLAMIC_OCCASIONS) {
            val occDayOfYear = (occ.hijriMonth - 1) * 30 + occ.hijriDay
            val diff = if (occDayOfYear >= currentDayOfYear) {
                occDayOfYear - currentDayOfYear
            } else {
                (354 - currentDayOfYear) + occDayOfYear
            }

            if (diff < minDiff) {
                minDiff = diff
                closestOccasion = occ
            }
        }

        return if (closestOccasion != null) Pair(closestOccasion, minDiff) else null
    }
}
