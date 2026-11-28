<p align="center"><sub>
<a href="../../README_EN.md">English</a> · <a href="../../README.md">简体中文</a> · <a href="../zh-Hant/README.md">繁體中文</a> · <a href="../ja/README.md">日本語</a> · <a href="../ko/README.md">한국어</a> · <a href="../tr/README.md">Türkçe</a> · <a href="../ru/README.md">Русский</a> · <a href="../vi/README.md">Tiếng Việt</a> · <a href="../th/README.md">ไทย</a> · <a href="../de/README.md">Deutsch</a> · <a href="../es/README.md">Español</a> · <a href="../fr/README.md">Français</a>
<br>
<a href="../uk/README.md">Українська</a> · <a href="../pl/README.md">Polski</a> · <a href="../pt-BR/README.md">Português (Brasil)</a> · <b>العربية</b> · <a href="../fa/README.md">فارسی</a> · <a href="../id/README.md">Bahasa Indonesia</a> · <a href="../it/README.md">Italiano</a> · <a href="../el/README.md">Ελληνικά</a> · <a href="../ml/README.md">മലയാളം</a> · <a href="../ur-Latn/README.md">Roman Urdu</a> · <a href="../ro/README.md">Română</a> · <a href="../bg/README.md">Български</a> · <a href="../lzh/README.md">文言</a>
</sub></p>

<p align="center">
  <img src="../../assets/icon.png" width="160" alt="ZyNova">
</p>

<div dir="rtl" align="right">

# ZyNova

> 📌 **Current release: 28.3.2** · [Changelog](../../CHANGELOG.md) · [Releases](https://github.com/zzy89216-gif/ZyNova/releases/latest)

[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)](https://developer.android.com/)
[![Release](https://img.shields.io/badge/Release-28.3.2-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v28.3.2)

---

مشغّل **Minecraft: Java Edition** لأندرويد، يُطوَّر ويُنشر باستقلال انطلاقًا من شيفرة ZalithLauncher2. ليس إصدارًا رسميًا.

الفكرة في سطر واحد: **خطوات أقل**. بدل أن يعرض عليك عشرين إعدادًا، يقرّر المشغّل أولًا ويسأل فقط حين يعجز عن القرار.

## ✨ أحدث التغييرات

**28.3.2** (أحدث إصدار): إعادة تصميم الصفحة الرئيسية (آخر العوالم + قائمة العوالم الكاملة + موجز رئيسية مخصص)، وإزالة صفحة «التخصيص»، وإصلاح عنوان تفاصيل العارض، وتقدّم ملء الشاشة عند حذف إصدار، وتحسين الأداء والتقطيع، وتحديث رابط Discord.

> المظهر البصري كما في 28.3.0 — بدون زجاج ديناميكي أو انكسار أو تأثيرات مغناطيسية.

**28.3.0** (إصدار مستقر): أبرز التغييرات.

يجمع هذا الإصدار المستقر كل محتوى الإصدارات التجريبية السابقة (تمت إزالة تلك الإصدارات).

- **تنزيل وتثبيت عدة عناصر في وقت واحد**: يمكن بدء إصدارات اللعبة وحزم المودات والموارد معًا، ولكل مهمة تقدّمها وسرعتها وخيار إلغائها. وإذا ضغطت على العنصر نفسه مرة أخرى فسيظهر تنبيه بدلًا من ألّا يحدث شيء.
- **ترتيب بطاقات الشاشة الرئيسية أصبح أكثر استقرارًا**: السحب بالضغط المطوّل لم يعد يومض أو يُغلق التطبيق؛ وعند السحب السريع تتبادل البطاقات واحدة تلو الأخرى، ويدعم السحب الأفقي أيضًا.
- **تحسين مشغّل الموسيقى**: تُحمَّل ملفات `.lrc` التي تحمل الاسم نفسه تلقائيًا، ويمكن استيراد الكلمات من الصفحة؛ ويعرض الإشعار السطر السابق والسطر الحالي. وأُضيف مشغّل مصغّر في الشريط العلوي (اسم المقطع + تشغيل/إيقاف مؤقت + تبديل المقطع). ويظهر الموضع والمدة بشكل صحيح حتى عند الإيقاف المؤقت، ولم تعد الواجهة تتجاوز الشاشة في الوضع الأفقي أو الشاشات المنخفضة.
- **إصلاح «العودة إلى اللعبة»**: يختفي العنصر فور إغلاق اللعبة، والضغط عليه قبل اختفائه لم يعد يسبب انهيارًا.
- **تعزيز الذكاء الاصطناعي**: يمكن تثبيت عدة إصدارات من اللعبة في وقت واحد؛ وأُضيفت أدوات للتحكم بالموسيقى (53 أداة إجمالًا).
- **إضافة «اللعب الجماعي» إلى قائمة اللعبة**: تُغلق القائمة ويفتح المشغّل صفحة اللعب الجماعي مباشرة، واللعبة لا تُغلق بل تبقى تعمل في الخلفية.

التغييرات الكاملة: [CHANGELOG.md](../../CHANGELOG.md).

## 🛠️ ما يستطيعه

- **وكيل الذكاء الاصطناعي**: DeepSeek افتراضيًا — يثبّت اللعبة وحزم المودات والمودات، ويعدّل الإعدادات، ويقرأ السجلات، ويشغّل اللعبة
- **أنظمة العرض**: تلقائي / MobileGlues / Ironized Zink / GL4ES / LTW (تجريبي)
- **نواة الموارد**: البحث في Modrinth / CurseForge، وحل الاعتماديات، والتنزيل، والتحقق، والتثبيت
- **المفضلة والأرشيف**: مفضلة عبر الإنترنت + أرشيف خاص، مع إزالة التكرار بـ SHA-256
- **حماية Fabric API**: تُضاف Fabric API / Quilted Fabric API / Legacy Fabric API عبر مسار التثبيت فقط — لا تكرار ولا بحث ولا حذف

## 🚀 ثلاث خطوات للعب

1. نزّل ملف APK من [Releases](https://github.com/zzy89216-gif/ZyNova/releases) وثبّته (أندرويد 8.0 فأحدث)
2. عند أول تشغيل: اقرأ المستندات الثلاثة حتى النهاية، أكّد كلًا منها، واختر الفئة العمرية
3. سجّل الدخول بحساب Microsoft، اختر الإصدار أو ثبّته، ثم شغّل اللعبة

## 📖 المصطلحات

- **حزمة المودات (Modpack)**: مجموعة مودات تُثبّت دفعة واحدة
- **الشيدر (Shader)**: إضاءة ومياه أفضل، تُفعّل من إعدادات الرسوميات
- **النسخة (Instance)**: مجلد مستقل فيه الإصدار والمودات والإعدادات

## 📦 التنزيل والبناء

- التنزيل: <https://github.com/zzy89216-gif/ZyNova/releases> (استخدم أحدث إصدار دائمًا)
- البناء بنفسك: JDK 17 + Android SDK / NDK ثم `./gradlew ZalithLauncher:assembleRelease`
- ملاحظة: يظهر إجراء الامتثال عند أول تشغيل؛ التفاصيل في [docs/en/licenses.md](../../docs/en/licenses.md)

## 📚 التوثيق

> 📚 التوثيق متوفر بالصينية والإنجليزية (78 مقالًا لكل لغة)؛ وهذه الصفحة هي المقدمة بالعربية.

| المحتوى | الملف |
|---|---|
| فهرس التوثيق | [docs/en/README.md](../../docs/en/README.md) |
| التثبيت / الاستخدام / التصدير | [docs/en/install.md](../../docs/en/install.md) · [docs/en/usage.md](../../docs/en/usage.md) |
| العرض / الدخول / الذكاء الاصطناعي | [docs/en/renderers.md](../../docs/en/renderers.md) · [docs/en/login.md](../../docs/en/login.md) · [docs/en/ai-agent.md](../../docs/en/ai-agent.md) |
| حل المشكلات / الأسئلة الشائعة / الخصوصية | [docs/en/troubleshooting.md](../../docs/en/troubleshooting.md) · [docs/en/faq.md](../../docs/en/faq.md) · [docs/en/data-and-privacy.md](../../docs/en/data-and-privacy.md) |
| البناء / سجل التغييرات | [docs/en/build.md](../../docs/en/build.md) · [CHANGELOG.md](../../CHANGELOG.md) |
| التسليم / تراخيص الأطراف الثالثة / الرخصة | [HANDOFF.md](../../HANDOFF.md) · [THIRD_PARTY_NOTICES.md](../../THIRD_PARTY_NOTICES.md) · [LICENSE](../../LICENSE) |

## 💖 الدعم

- الإبلاغ عن مشكلة：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/dbHTApYNYJ>
- البريد：<ZyNovaLauncher@outlook.com>
- X الرسمي：<https://x.com/ZyNovaProject>
- ادعم المشروع：<https://afdian.com/a/hizzy>

## ⚖️ الرخصة

GPL-3.0 — يبقى المشروع مفتوح المصدر. مكوّنات الأطراف الثالثة وتراخيصها في
[THIRD_PARTY_NOTICES.md](../../THIRD_PARTY_NOTICES.md) و[docs/en/licenses.md](../../docs/en/licenses.md).

> ⚠️ **Terracotta (اللعب الجماعي) تحت AGPL-3.0**، وهي أشد من البقية: تُوزَّع دون تعديل. إذا عدّلتها
> وأعدت توزيعها، فالمادة 13 من AGPL-3.0 تُلزمك بنشر الشيفرة المصدرية المقابلة.

</div>

---

**ZyNova — Context First. Less Steps.**
