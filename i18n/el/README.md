<p align="center"><sub>
<a href="../../README_EN.md">English</a> · <a href="../../README.md">简体中文</a> · <a href="../zh-Hant/README.md">繁體中文</a> · <a href="../ja/README.md">日本語</a> · <a href="../ko/README.md">한국어</a> · <a href="../tr/README.md">Türkçe</a> · <a href="../ru/README.md">Русский</a> · <a href="../vi/README.md">Tiếng Việt</a> · <a href="../th/README.md">ไทย</a> · <a href="../de/README.md">Deutsch</a> · <a href="../es/README.md">Español</a> · <a href="../fr/README.md">Français</a>
<br>
<a href="../uk/README.md">Українська</a> · <a href="../pl/README.md">Polski</a> · <a href="../pt-BR/README.md">Português (Brasil)</a> · <a href="../ar/README.md">العربية</a> · <a href="../fa/README.md">فارسی</a> · <a href="../id/README.md">Bahasa Indonesia</a> · <a href="../it/README.md">Italiano</a> · <b>Ελληνικά</b> · <a href="../ml/README.md">മലയാളം</a> · <a href="../ur-Latn/README.md">Roman Urdu</a> · <a href="../ro/README.md">Română</a> · <a href="../bg/README.md">Български</a>
</sub></p>

<p align="center">
  <img src="../../assets/icon.png" width="160" alt="ZyNova">
</p>

# ZyNova

> ⭐ **Κυκλοφόρησε το ZyNova 28.1.0 (έκδοση με παγκόσμια συμμόρφωση).**
>
> 📌 **Current release: 28.2.1** · [Changelog](../../CHANGELOG.md) · [Releases](https://github.com/zzy89216-gif/ZyNova/releases/latest)


[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)](https://developer.android.com/)
[![Release](https://img.shields.io/badge/Release-28.2.1-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v28.2.1)

---

Ένας launcher για **Minecraft: Java Edition** σε Android, που συντηρείται ανεξάρτητα με βάση τον κώδικα του ZalithLauncher2. Δεν είναι επίσημη έκδοση.

Η ιδέα: **λιγότερα πατήματα**. Αντί να σου πετάξει είκοσι ρυθμίσεις, ο launcher αποφασίζει πρώτα και ρωτά μόνο όταν πραγματικά δεν μπορεί.

## ✨ Τι νέο στην 28.1.0

- **Ροή συμμόρφωσης στην πρώτη εκκίνηση**: τρία ενοποιημένα έγγραφα (① EULA + πολιτική απορρήτου, ② ειδοποίηση ανοιχτού κώδικα τρίτων + αποποιήσεις και κίνδυνοι, ③ οδηγός + περιοχές και δικαιοδοσία). Κάθε ένα πρέπει να **κυληθεί ως το τέλος και να επιβεβαιωθεί χωριστά**· μετά επιλέγετε ηλικιακή ομάδα (και οι υπάρχοντες χρήστες το βλέπουν μία φορά).
- **Χρονικό παράθυρο για ανηλίκους**: κάτω των 18, το παιχνίδι ξεκινά μόνο Παρασκευή, Σάββατο, Κυριακή και επίσημες αργίες, **20:00–21:00**. Οι αργίες του 2026 ακολουθούν το ημερολόγιο του Κρατικού Συμβουλίου της Κίνας· οι ημέρες αναπλήρωσης μετράνε ως εργάσιμες.
- **Αποποιήσεις γραμμένες αναλυτικά**: απώλεια save, υπερθέρμανση, αποκλεισμός από τρίτους, διακοπή υπηρεσίας — όλα δηλωμένα.
- **Επίσης**: η αρχική δεν κολλάει στη φόρτωση, η αναζήτηση ιστού της AI λειτουργεί, η εισαγωγή πηγαίνει στον φάκελο instance, αφαιρέθηκε η αυτόματη επιδιόρθωση μετά από crash.

## 🛠️ Τι κάνει

- **Πράκτορας AI**: DeepSeek προεπιλογή — εγκαθιστά παιχνίδι, modpacks και mods, αλλάζει ρυθμίσεις, διαβάζει logs, εκκινεί το παιχνίδι
- **Renderers**: Αυτόματο / MobileGlues / GL4ES / LTW (πειραματικό)
- **Πυρήνας πόρων**: αναζήτηση Modrinth / CurseForge, επίλυση εξαρτήσεων, λήψη, επαλήθευση, εγκατάσταση
- **Αγαπημένα και αρχείο**: διαδικτυακά αγαπημένα + ιδιωτικό αρχείο, αφαίρεση διπλότυπων με SHA-256
- **Προστασία Fabric API**: προστίθενται μόνο από τη ροή εγκατάστασης — χωρίς διπλά, χωρίς αναζήτηση, χωρίς διαγραφή

## 🚀 Γρήγορη εκκίνηση

1. Κατεβάστε το APK από [Releases](https://github.com/zzy89216-gif/ZyNova/releases) και εγκαταστήστε το (Android 8.0+)
2. Στην πρώτη εκκίνηση διαβάστε τα τρία έγγραφα ως το τέλος, τσεκάρετε το καθένα και επιλέξτε ηλικιακή ομάδα
3. Συνδεθείτε με λογαριασμό Microsoft, επιλέξτε ή εγκαταστήστε έκδοση, ξεκινήστε

## 📦 Λήψη και build

- Λήψη: <https://github.com/zzy89216-gif/ZyNova/releases> (πάντα την τελευταία έκδοση)
- Δικό σας build: JDK 17 + Android SDK / NDK, μετά `./gradlew ZalithLauncher:assembleRelease`
- Σημείωση: στην πρώτη εκκίνηση εμφανίζεται η ροή συμμόρφωσης· λεπτομέρειες στο [docs/en/licenses.md](../../docs/en/licenses.md)

## 📚 Τεκμηρίωση

> 📚 Η τεκμηρίωση υπάρχει στα κινεζικά και τα αγγλικά (77 άρθρα η καθεμία)· αυτή η σελίδα είναι η ελληνική εισαγωγή.

| Περιεχόμενο | Αρχείο |
|---|---|
| Ευρετήριο | [docs/en/README.md](../../docs/en/README.md) |
| Εγκατάσταση / χρήση / εξαγωγή | [docs/en/install.md](../../docs/en/install.md) · [docs/en/usage.md](../../docs/en/usage.md) |
| Rendering / σύνδεση / AI | [docs/en/renderers.md](../../docs/en/renderers.md) · [docs/en/login.md](../../docs/en/login.md) · [docs/en/ai-agent.md](../../docs/en/ai-agent.md) |
| Προβλήματα / FAQ / δεδομένα και απόρρητο | [docs/en/troubleshooting.md](../../docs/en/troubleshooting.md) · [docs/en/faq.md](../../docs/en/faq.md) · [docs/en/data-and-privacy.md](../../docs/en/data-and-privacy.md) |
| Build / αλλαγές | [docs/en/build.md](../../docs/en/build.md) · [CHANGELOG.md](../../CHANGELOG.md) |
| Παράδοση / άδειες τρίτων / άδεια | [HANDOFF.md](../../HANDOFF.md) · [THIRD_PARTY_NOTICES.md](../../THIRD_PARTY_NOTICES.md) · [LICENSE](../../LICENSE) |

## 💖 Υποστήριξη

- Αναφορά προβλήματος：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>
- Email：<ZyNovaLauncher@outlook.com>
- Επίσημο X：<https://x.com/ZyNovaProject>
- Στηρίξτε το έργο：<https://afdian.com/a/hizzy>

## ⚖️ Άδεια

GPL-3.0 — το έργο παραμένει ανοιχτού κώδικα. Τα στοιχεία τρίτων και οι άδειές τους είναι στα
[THIRD_PARTY_NOTICES.md](../../THIRD_PARTY_NOTICES.md) και [docs/en/licenses.md](../../docs/en/licenses.md).

> ⚠️ **Το Terracotta (multiplayer) είναι υπό AGPL-3.0**, αυστηρότερη από τα υπόλοιπα: διανέμεται χωρίς
> αλλαγές. Αν το τροποποιήσετε και το διανείμετε ξανά, το §13 της AGPL-3.0 σας υποχρεώνει να
> δημοσιεύσετε τον αντίστοιχο πηγαίο κώδικα.

---

**ZyNova — Context First. Less Steps.**
