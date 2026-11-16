package com.example.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast

/**
 * مساعد مشاركة وتحميل تطبيق سكينة
 * يوفر روابط عامة ومستقلة تعمل على أي هاتف متصفح دون أخطاء أو جلسات مؤقتة
 */
object AppShareHelper {

    // المعرف العام للتطبيق في منصة AI Studio
    const val APP_ID = "43199d1a-9aae-469e-870c-abc8778e4f10"

    // الرابط العام المستقل المباشر لفتح التطبيق في أي متصفح هاتف (PWA / Web App)
    const val PUBLIC_APP_URL = "https://aistudio.google.com/app/$APP_ID"

    /**
     * نص الرسالة الشامل والمستقل لمشاركة التطبيق
     */
    fun getShareText(): String {
        return """
🌿 تطبيق سكينة | رفيقك اليومي للقرآن الكريم والعبادة بدون إنترنت

✨ مميزات التطبيق:
📖 مصحف المدينة المنورة المصور الكامل (طبعة مجمع الملك فهد - رواية حفص)
🕋 مواقيت الصلاة الدقيقة والأذان بأجمل الأصوات محلياً بدون إنترنت
🧭 بوصلة القبلة الدقيقة لكل المدن
📿 أذكار المسلم اليومية والتسبيح وحصن المسلم كاملاً
📜 الأربعون النووية الشاملة مع الشرح والأدعية المأثورة

🌐 لتشغيل التطبيق مباشرة على أي هاتف (PWA / Web):
$PUBLIC_APP_URL

📦 لتحميل ملف الـ APK المباشر وتثبيته على أي هاتف:
يمكنك تنزيل ملف APK مباشرة وتثبيته عبر قائمة خيارات التطبيق (Settings ⚙️ -> Export Project -> Download APK).
        """.trimIndent()
    }

    /**
     * فتح نافذة مشاركة النظام الرسمية (Android Share Sheet)
     */
    fun shareApp(context: Context) {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, getShareText())
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "مشاركة تطبيق سكينة الإسلامي")
        context.startActivity(shareIntent)
    }

    /**
     * نسخ الرابط العام المستقل إلى الحافظة
     */
    fun copyLink(context: Context) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("رابط تطبيق سكينة", PUBLIC_APP_URL)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "تم نسخ الرابط المستقل إلى الحافظة بنجاح 📋", Toast.LENGTH_SHORT).show()
    }
}
