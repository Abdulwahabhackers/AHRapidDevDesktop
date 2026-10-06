package com.rapiddev.ah.core.guide

import com.rapiddev.ah.data.model.GuideSection
import com.rapiddev.ah.data.model.GuideStep

object GuideContent {

    fun sections(): List<GuideSection> = listOf(

        GuideSection(
            id = "intro",
            title = "المقدمة - كيف تستخدم هذا الدليل",
            emoji = "\uD83D\uDCD6",
            steps = listOf(
                GuideStep(
                    title = "ما هو AH RapidDev؟",
                    body = "تطبيق يساعدك على تحويل الأفكار إلى مشاريع جاهزة باستخدام الذكاء الاصطناعي.\n\n\u2022 تستخدم AI (DeepSeek أو غيره) لتوليد الشجرة والأكواد.\n\u2022 تستخدم AH RapidDev لتحويل الرد إلى ملفات ومجلدات حقيقية.\n\u2022 تستخدم Android IDE PRO لفتح المشروع وبنائه وتشغيله.\n\nالدورة الكاملة:\nالفكرة \u2190 AI \u2190 شجرة + أكواد \u2190 AH RapidDev \u2190 مشروع \u2190 Android IDE PRO \u2190 تطبيق."
                ),
                GuideStep(
                    title = "الأدوات المطلوبة",
                    body = "1. AH RapidDev (هذا التطبيق) - لبناء المشاريع.\n2. Android IDE PRO - لفتح المشروع وبنائه.\n3. تطبيق ذكاء اصطناعي (DeepSeek / ChatGPT / Claude) - لتوليد الأكواد.\n4. (اختياري) Termux - لأوامر متقدمة.\n\nكل هذه التطبيقات تعمل على أندرويد بدون كمبيوتر."
                ),
                GuideStep(
                    title = "متى تستخدم كل شاشة؟",
                    body = "\u2022 من شجرة \u2190 لبناء هيكل مشروع من شجرة AI (ملفات فارغة).\n\u2022 استيراد AI \u2190 لكتابة الأكواد داخل ملفات موجودة.\n\u2022 مشروع جديد \u2190 لإنشاء مشروع Android أو Web من الصفر.\n\u2022 عرض شجرة \u2190 لتوليد شجرة مشروعك وإرسالها للـ AI.\n\u2022 تصفح الملفات \u2190 لإدارة ملفاتك (نسخ، حذف، تعديل).\n\u2022 دمج المشاريع \u2190 لدمج مشروع محدّث مع مشروعك الحالي.\n\u2022 الجلسات \u2190 لحفظ مشاريعك للوصول السريع."
                )
            )
        ),

        GuideSection(
            id = "context",
            title = "الخطوة 1: تعريف الذكاء الاصطناعي ببيئة العمل",
            emoji = "\uD83C\uDFAF",
            steps = listOf(
                GuideStep(
                    title = "لماذا هذه الخطوة ضرورية؟",
                    body = "قبل أن تطلب من AI أي كود، يجب أن تعرّفه على:\n\u2022 بيئة التطوير (Android IDE PRO على أندرويد).\n\u2022 إصدارات الأدوات (Kotlin, Gradle, SDK).\n\u2022 شكل الرد المطلوب (شجرة + أكواد).\n\nبدون هذه المقدمة، سيولّد AI كوداً لا يعمل على هاتفك.\n\nالصق البرومبت التالي في بداية كل محادثة جديدة:"
                ),
                GuideStep(
                    title = "برومبت: تعريف البيئة",
                    body = "الصق هذا النص في بداية أي محادثة جديدة مع DeepSeek أو ChatGPT:",
                    prompt = CONTEXT_PROMPT,
                    showCopy = true
                )
            )
        ),

        GuideSection(
            id = "templates",
            title = "الخطوة 2: قوالب جاهزة لأفكار التطبيقات",
            emoji = "\uD83D\uDCA1",
            steps = listOf(
                GuideStep(
                    title = "قالب: تطبيق أندرويد عام",
                    body = "استبدل النص بين [ ] بفكرتك ثم أرسله للـ AI:",
                    prompt = TEMPLATE_ANDROID,
                    showCopy = true
                ),
                GuideStep(
                    title = "قالب: تطبيق تصفح/محتوى",
                    body = "مناسب لتطبيقات المواقع، الأخبار، الفيديوهات:",
                    prompt = TEMPLATE_CONTENT,
                    showCopy = true
                ),
                GuideStep(
                    title = "قالب: تطبيق أدوات",
                    body = "مناسب لتطبيقات الحسابات، المحولات، الأدوات:",
                    prompt = TEMPLATE_TOOLS,
                    showCopy = true
                ),
                GuideStep(
                    title = "قالب: موقع ويب",
                    body = "مناسب للمواقع الثابتة (Portfolio, شركة, هوية):",
                    prompt = TEMPLATE_WEB,
                    showCopy = true
                ),
                GuideStep(
                    title = "قالب: لعبة بسيطة",
                    body = "مناسب لألعاب 2D بسيطة:",
                    prompt = TEMPLATE_GAME,
                    showCopy = true
                )
            )
        ),

        GuideSection(
            id = "get-tree",
            title = "الخطوة 3: الحصول على الشجرة والأكواد",
            emoji = "\uD83C\uDF33",
            steps = listOf(
                GuideStep(
                    title = "كيف تطلب الشجرة؟",
                    body = "بعد إرسال فكرتك، استخدم هذا الطلب:",
                    prompt = REQUEST_TREE,
                    showCopy = true
                ),
                GuideStep(
                    title = "شكل الرد الصحيح",
                    body = "يجب أن يكون الرد على شكل شجرة، ثم لكل ملف مساره وكوده."
                ),
                GuideStep(
                    title = "إذا لم يلتزم AI بالشكل",
                    body = "اطلب منه بشكل صريح:",
                    prompt = REMIND_FORMAT,
                    showCopy = true
                )
            )
        ),

        GuideSection(
            id = "build-project",
            title = "الخطوة 4: بناء المشروع في AH RapidDev",
            emoji = "\uD83C\uDFD7\uFE0F",
            steps = listOf(
                GuideStep(
                    title = "الطريقة (أ): بناء الشجرة أولاً",
                    body = "1. افتح التطبيق \u2190 من شجرة.\n2. الصق الشجرة فقط (بدون الأكواد).\n3. اسم المشروع: مثال MyApp.\n4. المسار: /storage/emulated/0/AndroidIDEProjects.\n5. اضغط تحليل \u2190 سيظهر عدد المجلدات والملفات.\n6. اضغط إنشاء المشروع.\n\nالنتيجة: مشروع بهيكل كامل، لكن الملفات فارغة."
                ),
                GuideStep(
                    title = "الطريقة (ب): بناء مباشر من رد AI الكامل",
                    body = "1. افتح التطبيق \u2190 استيراد أكواد AI.\n2. الصق رد AI كاملاً (شجرة + أكواد).\n3. اضغط تحليل الرسالة.\n4. اختر المسار.\n5. اضغط استيراد الملفات.\n\nالنتيجة: مشروع كامل الأكواد جاهز للفتح مباشرة."
                ),
                GuideStep(
                    title = "أيهما تختار؟",
                    body = "\u2022 من شجرة: عندك الهيكل فقط وتريد بناء الأكواد لاحقاً.\n\u2022 استيراد AI: رد AI يحتوي على أكواد كاملة \u2014 الأسرع والأفضل عادةً.\n\nنصيحة: إذا رد AI يحتوي على شجرة وأكواد، استخدم استيراد AI مباشرة."
                )
            )
        ),

        GuideSection(
            id = "open-ide",
            title = "الخطوة 5: فتح المشروع في Android IDE PRO",
            emoji = "\uD83D\uDEE0\uFE0F",
            steps = listOf(
                GuideStep(
                    title = "الفتح الأساسي",
                    body = "1. افتح تطبيق Android IDE PRO.\n2. اضغط Open Project.\n3. تنقل إلى المسار:\n/storage/emulated/0/AndroidIDEProjects/اسم_المشروع\n4. اختر المجلد \u2190 Open."
                ),
                GuideStep(
                    title = "أول مرة - Gradle Sync",
                    body = "بعد الفتح، سيقوم التطبيق بـ Gradle Sync:\n\u2022 قد يستغرق 2-10 دقائق.\n\u2022 يجب أن يكون الإنترنت متصلاً.\n\u2022 لا تغلق التطبيق أثناء المزامنة."
                ),
                GuideStep(
                    title = "مشاكل شائعة",
                    body = "\u2022 Error: SDK not found \u2190 ثبّت Android SDK من إعدادات التطبيق.\n\u2022 Error: Kotlin version \u2190 اختر Compose Compiler متوافق مع Kotlin 1.9.0 (الإصدار 1.5.2).\n\u2022 Error: Theme.Material3 \u2190 استبدل بـ Theme.Material.NoActionBar.\n\u2022 Error: cannot access SDK \u2190 فعّل الصلاحيات في الإعدادات."
                )
            )
        ),

        GuideSection(
            id = "build-run",
            title = "الخطوة 6: بناء المشروع وتشغيله",
            emoji = "\uD83D\uDE80",
            steps = listOf(
                GuideStep(
                    title = "بناء APK للتجربة",
                    body = "1. من قائمة Android IDE PRO.\n2. Build \u2190 Build Debug APK.\n3. انتظر انتهاء البناء.\n4. ستجد APK في:\napp/build/outputs/apk/debug/app-debug.apk"
                ),
                GuideStep(
                    title = "تشغيل التطبيق",
                    body = "بعد نجاح البناء:\n1. اضغط Install أو Run.\n2. أو افتح APK من مدير ملفات الهاتف.\n3. وافق على التثبيت من مصادر غير معروفة.\n4. سيظهر التطبيق على الشاشة الرئيسية."
                ),
                GuideStep(
                    title = "إذا فشل البناء",
                    body = "انسخ رسالة الخطأ من Logcat، ثم أرسلها للـ AI مع هذا البرومبت:",
                    prompt = FIX_ERROR,
                    showCopy = true
                ),
                GuideStep(
                    title = "بناء نسخة نهائية (Release)",
                    body = "لنشر التطبيق على Google Play أو مشاركته:\n\nBuild \u2190 Build Release APK\n\nسيحتاج APK إلى توقيع (Signing).\nAndroid IDE PRO يوفّر إنشاء مفتاح توقيع تلقائي."
                )
            )
        ),

        GuideSection(
            id = "updates",
            title = "الخطوة 7: تحديث المشروع ودمج التعديلات",
            emoji = "\uD83D\uDD04",
            steps = listOf(
                GuideStep(
                    title = "الحصول على تحديثات من AI",
                    body = "عندما تريد إضافة ميزة:\n1. أرسل للـ AI ملفات المشروع الحالية (أو أسماء الملفات).\n2. اطلب التعديلات.",
                    prompt = REQUEST_UPDATE,
                    showCopy = true
                ),
                GuideStep(
                    title = "دمج التحديثات",
                    body = "1. استخدم استيراد AI لكتابة التحديثات مباشرة.\n   (سيستبدل الملفات المعدّلة، ويضيف الجديدة.)\n\nأو:\n1. احفظ التحديثات في مجلد مؤقت.\n2. استخدم دمج المشاريع.\n3. اختر وضع استبدال للملفات المتغيرة.\n\nالطريقة الأولى أسرع وأدق."
                ),
                GuideStep(
                    title = "النسخ الاحتياطي",
                    body = "قبل أي تحديث كبير:\n1. تصفح الملفات \u2190 اضغط مطولاً على مجلد المشروع.\n2. اختر نسخ إلى...\n3. اختر مجلد النسخ الاحتياطي.\n4. احتفظ بنسخة قبل كل تعديل كبير."
                )
            )
        ),

        GuideSection(
            id = "sessions-guide",
            title = "الخطوة 8: إدارة الجلسات",
            emoji = "\uD83D\uDCBE",
            steps = listOf(
                GuideStep(
                    title = "لماذا الجلسات؟",
                    body = "إذا كنت تعمل على عدة مشاريع:\n\u2022 الجلسات تحفظ كل مشروع بمساره.\n\u2022 فتح بضغطة واحدة في تصفح الملفات أو عارض الشجرة.\n\u2022 إضافة ملاحظات لكل مشروع.\n\u2022 تتبع آخر وقت فتح."
                ),
                GuideStep(
                    title = "كيف تضيف جلسة؟",
                    body = "1. من الرئيسية \u2190 الجلسات.\n2. اضغط زر + إضافة.\n3. اسم المشروع.\n4. المسار.\n5. النوع (Android / موقع / شجرة / مستورد).\n6. اضغط إضافة."
                ),
                GuideStep(
                    title = "الاستخدام اليومي",
                    body = "\u2022 اضغط على جلسة \u2190 تفتح في تصفح الملفات.\n\u2022 قائمة الخيارات \u2190 فتح في عارض الشجرة.\n\u2022 قائمة الخيارات \u2190 إعادة تسمية / ملاحظة / حذف.\n\nإذا كان المسار مفقوداً، ستظهر علامة تحذير بجانب الاسم."
                )
            )
        ),

        GuideSection(
            id = "tips",
            title = "نصائح احترافية",
            emoji = "\u2B50",
            steps = listOf(
                GuideStep(
                    title = "نصائح للتعامل مع AI",
                    body = "\u2022 ابدأ كل محادثة جديدة بالبرومبت التعريفي.\n\u2022 اطلب من AI الشرح خطوة بخطوة إذا كان الرد كبيراً.\n\u2022 لا تقبل كوداً لا تفهمه.\n\u2022 اطلب مراجعة إذا ظهرت أخطاء متكررة.\n\u2022 استخدم DeepSeek للكود، ChatGPT للشرح."
                ),
                GuideStep(
                    title = "تنظيم المشاريع",
                    body = "استخدم هذا الهيكل:\n\nAndroidIDEProjects/\n\u251C\u2500\u2500 MyApp1/\n\u251C\u2500\u2500 MyApp2/\n\u251C\u2500\u2500 Projects_Backup/\n\u2514\u2500\u2500 Temp/\n\nلا تضع مسافات في أسماء المشاريع لتجنب مشاكل Gradle."
                ),
                GuideStep(
                    title = "الأمان",
                    body = "\u2022 لا تشارك توكن GitHub مع أي أحد.\n\u2022 احتفظ بنسخة احتياطية قبل كل تعديل كبير.\n\u2022 لا تمنح صلاحيات MANAGE_EXTERNAL_STORAGE لتطبيق لا تثق به.\n\u2022 استخدم AH RapidDev فقط على مشاريعك الشخصية."
                ),
                GuideStep(
                    title = "مصادر التعلم",
                    body = "\u2022 developer.android.com\n\u2022 kotlinlang.org\n\u2022 developer.android.com/jetpack/compose\n\u2022 chat.deepseek.com"
                )
            )
        )
    )

    private const val CONTEXT_PROMPT = "أنت مساعد ذكاء اصطناعي متخصص في تطوير تطبيقات أندرويد.\n\nبيئة العمل الخاصة بي:\n- الجهاز: هاتف أندرويد\n- بيئة التطوير: Android IDE PRO\n- لغة البرمجة: Kotlin\n- الإصدارات:\n  - compileSdk = 34\n  - minSdk = 24\n  - targetSdk = 34\n  - Kotlin = 1.9.0\n  - Compose Compiler = 1.5.2\n  - Gradle = 8.1.1\n  - Java/JVM = 17\n\nقواعد الكتابة:\n1. اكتب الرد على شكل شجرة أولاً، ثم لكل ملف اكتب اسمه أو مساره الكامل متبوعاً بكتلة كود.\n2. استخدم التنسيق التالي:\n\nاسم المشروع/\n\u251C\u2500\u2500 app/\n\u2502   \u251C\u2500\u2500 build.gradle.kts\n\u2502   \u2514\u2500\u2500 src/main/\n\u2502       \u251C\u2500\u2500 AndroidManifest.xml\n\u2502       \u2514\u2500\u2500 java/com/example/\n\u2502           \u2514\u2500\u2500 MainActivity.kt\n\u2514\u2500\u2500 settings.gradle.kts\n\nثم لكل ملف:\n\napp/build.gradle.kts\n```kotlin\n// كود الملف هنا\n```\n\n3. لا تستخدم أي مكتبة غير موجودة في Maven Central أو Google Maven.\n4. تجنب الحلول التي تحتاج صلاحيات root.\n5. اكتب أكواداً تعمل على Android IDE PRO بدون تعديل يدوي.\n6. اشرح فقط ما هو ضروري، ولا تطل.\n\nالآن أنا جاهز، انتظر فكرتي."

    private const val TEMPLATE_ANDROID = "أريد تطبيق أندرويد كامل باستخدام Kotlin و Jetpack Compose.\n\nالفكرة العامة:\n[اكتب هنا فكرة تطبيقك في 3-5 أسطر]\n\nالميزات المطلوبة:\n1. [الميزة الأولى]\n2. [الميزة الثانية]\n3. [الميزة الثالثة]\n\nقواعد التصميم:\n- تصميم عصري بسيط\n- دعم الوضع الداكن والفاتح\n- واجهة عربية RTL\n\nيرجى أن ترسل:\n1. شجرة المشروع الكاملة.\n2. أكواد جميع الملفات (build.gradle.kts, AndroidManifest.xml, جميع ملفات .kt).\n3. تعليمات التشغيل."

    private const val TEMPLATE_CONTENT = "أريد تطبيق أندرويد لعرض/تصفح [نوع المحتوى].\n\nالفكرة:\n- التطبيق يعرض [المحتوى: فيديوهات/مقالات/مواقع/صور].\n- المستخدم يستطيع [البحث/التصفية/المفضلة].\n\nالتفاصيل:\n- الشاشات المطلوبة: [الرئيسية، التفاصيل، الإعدادات].\n- الألوان: [تفضيلاتك].\n- المتطلبات الخاصة: [أي شيء إضافي].\n\nالبيئة: Kotlin + Jetpack Compose، Android IDE PRO، SDK 34، Kotlin 1.9.0.\n\nأرسل شجرة المشروع، ثم كل ملف وكوده على حدة."

    private const val TEMPLATE_TOOLS = "أريد تطبيق أندرويد كأداة [نوع الأداة].\n\nالوظائف:\n1. [الوظيفة الأولى]\n2. [الوظيفة الثانية]\n\nالواجهة:\n- شاشة رئيسية بأزرار واضحة.\n- نتائج فورية.\n\nالبيئة: Kotlin + Jetpack Compose، SDK 34.\n\nأرسل شجرة المشروع كاملة، ثم الأكواد لكل ملف.\n\nتأكد أن الكود يعمل بدون مكتبات خارجية إن أمكن."

    private const val TEMPLATE_WEB = "أريد موقع ويب ثابت (Static Website).\n\nالاسم: [اسم الموقع]\nالهدف: [وصف مختصر]\nالصفحات: [الرئيسية، من نحن، تواصل]\n\nالتقنيات:\n- HTML5 + CSS3\n- JavaScript خفيف إن لزم\n- تصميم متجاوب (Responsive)\n- دعم RTL للعربية\n\nيرجى أن ترسل شجرة المجلدات ثم أكواد جميع الملفات."

    private const val TEMPLATE_GAME = "أريد لعبة أندرويد 2D بسيطة.\n\nالفكرة: [وصف اللعبة]\nالميكانيكا: [كيف يلعب المستخدم]\nالمستويات: [عدد المراحل]\n\nالتقنيات:\n- Kotlin + Canvas أو Jetpack Compose\n- بدون محرك ألعاب خارجي\n- رسومات بسيطة بـ Vector Drawables أو Compose Canvas\n\nالبيئة: Android IDE PRO، SDK 34، Kotlin 1.9.0.\n\nأرسل شجرة المشروع، ثم الأكواد كاملة لكل ملف."

    private const val REQUEST_TREE = "الآن، بعد أن عرفت الفكرة، يرجى أن ترسل:\n\n1. شجرة المشروع الكاملة بتنسيق نصي (باستخدام الرموز).\n\n2. بعد الشجرة، لكل ملف، اكتب:\n   - المسار الكامل للملف في سطر منفصل.\n   - ثم كتلة الكود محاطة بثلاث علامات backtick.\n\n3. تأكد من:\n   - أن جميع الملفات الضرورية مذكورة.\n   - أن الأكواد كاملة وقابلة للتشغيل.\n   - أنك لم تختصر أي جزء.\n\nابدأ."

    private const val REMIND_FORMAT = "الرجاء الالتزام بهذا التنسيق بالضبط:\n\nاسم المشروع/\n\u251C\u2500\u2500 ملف1.kt\n\u251C\u2500\u2500 مجلد/\n\u2502   \u2514\u2500\u2500 ملف2.kt\n\u2514\u2500\u2500 ملف3.kt\n\nثم لكل ملف اكتب اسمه أو مساره الكامل في سطر منفصل، ثم كتلة كوده.\n\nبدون أي شرح إضافي بين الملفات."

    private const val FIX_ERROR = "واجهت هذا الخطأ عند بناء المشروع في Android IDE PRO:\n\n[انسخ نص الخطأ كاملاً هنا]\n\nالملف المسؤول:\n[اكتب اسم الملف إذا كان معروفاً]\n\nبيئة البناء:\n- Kotlin 1.9.0\n- Gradle 8.1.1\n- Compose Compiler 1.5.2\n- SDK 34\n\nالرجاء إعطائي:\n1. سبب الخطأ باختصار.\n2. الملف الصحيح كاملاً بعد التعديل.\n3. أي ملف إضافي يجب تعديله."

    private const val REQUEST_UPDATE = "أريد إضافة ميزة/تحديث على مشروعي الحالي.\n\nالمشروع الحالي:\n- [اسم المشروع]\n- الملفات الحالية:\n[انسخ قائمة الملفات من عارض الشجرة]\n\nالميزة الجديدة المطلوبة:\n[اشرح الميزة بالتفصيل]\n\nالرجاء:\n1. أخبرني أي ملفات ستعدّل، وأي ملفات جديدة ستضيف.\n2. أعطني الأكواد الكاملة للملفات المعدّلة (لا تختصر).\n3. اكتب شجرة بالملفات الجديدة والمعدّلة."
}