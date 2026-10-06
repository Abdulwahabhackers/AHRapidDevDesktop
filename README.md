# AH RapidDev — Desktop Edition 🚀

<div align="center">

**من الفكرة إلى تطبيق — بمساعدة الذكاء الاصطناعي**

[![Kotlin](https://img.shields.io/badge/Kotlin-2.4.20-purple.svg)](https://kotlinlang.org)
[![Compose Multiplatform](https://img.shields.io/badge/Compose_Multiplatform-1.12.1-blue.svg)](https://www.jetbrains.com/lp/compose-multiplatform/)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](#-الرخصة)
[![Platform](https://img.shields.io/badge/platform-Windows_10%2F11-informational.svg)](#)

</div>

---

## 📖 عن التطبيق

**AH RapidDev** أداة متكاملة تحوّل أفكارك إلى مشاريع حقيقية باستخدام الذكاء الاصطناعي. صُمّم خصيصاً للمطورين المبتدئين والمحترفين، ويعمل على **Windows** كبرنامج كمبيوتر كامل.

> 💡 **الفكرة**: أنت تفكر، الذكاء الاصطناعي يكتب، و AH RapidDev يبني المشروع.

---

## ✨ الميزات

| الميزة | الوصف |
|---|---|
| 🤖 **استيراد أكواد AI** | الصق رد الذكاء الاصطناعي واحصل على مشروع كامل بملفاته ومجلداته |
| 🧠 **Smart Import** | تحليل ذكي يكتشف التعليمات (إضافة import، حذف دالة...) وينفذها تلقائياً |
| 📤 **تصدير للـ AI** | حوّل مشروعك إلى ملفات `.txt` جاهزة لإرسالها للذكاء الاصطناعي |
| 📁 **تصفح الملفات** | متصفح كامل مع بحث فوري، تحديد متعدد، وعمليات جماعية |
| 📝 **محرر كود** | محرر مدمج مع تلوين Syntax (Kotlin, XML, JSON, Gradle, Java...) |
| 🌳 **عرض شجرة** | اعرض شجرة أي مجلد واحفظها |
| 🔀 **دمج المشاريع** | دمج التحديثات بثلاث طرق: تخطي / استبدال / إعادة تسمية |
| 💾 **الجلسات** | حفظ المشاريع للوصول السريع |
| 🎨 **واجهة عربية** | تصميم عصري داكن/فاتح مع دعم RTL كامل |
| 🖥️ **اختصار سطح المكتب** | إنشاء اختصار بضغطة واحدة من داخل التطبيق |

---

## 🖼️ لقطات الشاشة

> قريباً — سيتم إضافتها في الإصدار القادم

---

## 🛠️ المتطلبات

- **Windows 10/11** (64-bit)
- **JDK 17+** — للتطوير فقط (النسخة الجاهزة لا تحتاجه)

---

## 🚀 التثبيت

### الطريقة 1: نسخة جاهزة (للمستخدمين)

1. حمّل أحدث إصدار من [Releases](../../releases)
2. فك الضغط
3. شغّل `AH RapidDev.exe`
4. اختر **"نعم، أنشئ الاختصار"** من أول نافذة
5. استمتع! 🎉

### الطريقة 2: من الكود (للمطورين)

```bash
# 1. استنسخ المشروع
git clone https://github.com/YOUR_USERNAME/AHRapidDevDesktop.git
cd AHRapidDevDesktop

# 2. ابنِ النسخة
gradlew.bat :desktopApp:createDistributable

# 3. النتيجة في:
# desktopApp/build/compose/binaries/main/app/AH RapidDev/
```

---

🏗️ البنية

```
AHRapidDevDesktop/
├── desktopApp/              # نقطة دخول الويندوز
│   ├── icon.ico
│   ├── icon.png
│   ├── icon.svg
│   └── src/main/kotlin/
│
├── shared/                  # الكود المشترك
│   └── src/jvmMain/kotlin/com/rapiddev/ah/
│       ├── App.kt
│       ├── core/            # الأدوات والقوالب
│       ├── data/            # البيانات والإعدادات
│       └── presentation/    # كل الشاشات (18 مجلد)
│
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

---

🧰 التقنيات

· Kotlin 2.4.20 — لغة البرمجة
· Compose Multiplatform 1.12.1 — واجهات المستخدم
· Material 3 — نظام التصميم
· Kotlinx Coroutines — البرمجة غير المتزامنة
· Kotlinx Serialization — معالجة JSON
· Kermit — التسجيل

---

🎯 الاستخدام السريع

دورة العمل الكاملة:

```
الفكرة  →  AI  →  شجرة + أكواد  →  AH RapidDev  →  مشروع  →  APK
```

1. اسأل AI (DeepSeek / ChatGPT / Claude) عن تطبيق تريده
2. الصق الرد في استيراد أكواد AI
3. اختر مجلد الهدف
4. استورد — ستحصل على مشروع كامل جاهز
5. عدّل الأكواد في المحرر المدمج
6. صدّر للـ AI للحصول على تحديثات

---

🤝 المساهمة

المساهمات مرحب بها! افتح Issue أو أرسل Pull Request.

---

👥 الفريق

HACKERS AH — فريق متخصص في تطوير التطبيقات والأدوات البرمجية.

---

📱 نسخة الجوال

متوفرة للأندرويد بنفس الميزات — تصفح، استيراد AI، Smart Import، دمج المشاريع.

---

📄 الرخصة

هذا المشروع مرخّص تحت MIT License — يمكنك استخدامه، تعديله، وتوزيعه بحرية.

---

<div align="center">

صُنع بـ ❤️ بواسطة HACKERS AH

© 2026 — جميع الحقوق محفوظة

</div>
