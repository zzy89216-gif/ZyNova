<p align="center"><sub>
<a href="../../README_EN.md">English</a> · <a href="../../README.md">简体中文</a> · <a href="../zh-Hant/README.md">繁體中文</a> · <a href="../ja/README.md">日本語</a> · <a href="../ko/README.md">한국어</a> · <a href="../tr/README.md">Türkçe</a> · <a href="../ru/README.md">Русский</a> · <a href="../vi/README.md">Tiếng Việt</a> · <a href="../th/README.md">ไทย</a> · <a href="../de/README.md">Deutsch</a> · <a href="../es/README.md">Español</a> · <a href="../fr/README.md">Français</a>
<br>
<a href="../uk/README.md">Українська</a> · <a href="../pl/README.md">Polski</a> · <a href="../pt-BR/README.md">Português (Brasil)</a> · <a href="../ar/README.md">العربية</a> · <a href="../fa/README.md">فارسی</a> · <a href="../id/README.md">Bahasa Indonesia</a> · <a href="../it/README.md">Italiano</a> · <a href="../el/README.md">Ελληνικά</a> · <b>മലയാളം</b> · <a href="../ur-Latn/README.md">Roman Urdu</a> · <a href="../ro/README.md">Română</a> · <a href="../bg/README.md">Български</a>
</sub></p>

<p align="center">
  <img src="../../assets/icon.png" width="160" alt="ZyNova">
</p>

# ZyNova

> ⭐ **ZyNova 28.1.0 പുറത്തിറങ്ങി (ആഗോള അനുസരണ പതിപ്പ്).**
>
> 📌 **Current release: 28.2.0** · [Changelog](../../CHANGELOG.md) · [Releases](https://github.com/zzy89216-gif/ZyNova/releases/latest)


[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)](https://developer.android.com/)
[![Release](https://img.shields.io/badge/Release-28.2.0-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v28.2.0)

---

Android-നുള്ള **Minecraft: Java Edition** ലോഞ്ചർ. ZalithLauncher2-ന്റെ കോഡിൽ നിന്ന് സ്വതന്ത്രമായി വികസിപ്പിച്ച് പ്രസിദ്ധീകരിക്കുന്നു; ഇത് ഔദ്യോഗിക ബിൽഡ് അല്ല.

ആശയം ഒന്നേയുള്ളൂ: **കുറച്ച് സ്പർശനങ്ങൾ**. ഇരുപത് ക്രമീകരണങ്ങൾ നിരത്തിവയ്ക്കുന്നതിനു പകരം, ലോഞ്ചർ ആദ്യം തീരുമാനിക്കും — കഴിയാത്തപ്പോൾ മാത്രം ചോദിക്കും.

## ✨ 28.1.0-ലെ പുതിയത്

- **ആദ്യ തുടക്കത്തിലെ അനുസരണ നടപടിക്രമം**: മൂന്ന് രേഖകൾ (① EULA + സ്വകാര്യതാ നയം, ② മൂന്നാം കക്ഷി ഓപ്പൺ സോഴ്സ് അറിയിപ്പ് + ഉത്തരവാദിത്വ നിരാകരണവും അപകടസാധ്യതകളും, ③ ഉപയോഗ പരിശീലനം + പ്രദേശങ്ങളും നിയമ അധികാരവും). ഓരോ രേഖയും **അവസാനം വരെ സ്ക്രോൾ ചെയ്ത് പ്രത്യേകം ടിക്ക് ചെയ്യണം**; ശേഷം പ്രായ വിഭാഗം തിരഞ്ഞെടുക്കണം (നിലവിലുള്ള ഉപയോക്താക്കളും ഒരിക്കൽ കാണും).
- **പ്രായപൂർത്തിയാകാത്തവർക്കുള്ള സമയപരിധി**: 18 വയസ്സിൽ താഴെയുള്ളവർക്ക് വെള്ളി, ശനി, ഞായർ, ഔദ്യോഗിക അവധികളിൽ **20:00–21:00** മാത്രം തുടങ്ങാം; 2026-ലെ അവധികൾ ചൈനീസ് സ്റ്റേറ്റ് കൗൺസിൽ കലണ്ടർ അനുസരിച്ച് ദിവസം തിരിച്ച് നൽകിയിരിക്കുന്നു; പകരം ജോലിദിനങ്ങൾ ജോലിദിനമായി കണക്കാക്കും.
- **ഉത്തരവാദിത്വ നിരാകരണം പൂർണമായി**: സേവ് നഷ്ടം, ഉപകരണം ചൂടാകൽ, മൂന്നാം കക്ഷി ബാൻ, സേവനം നിർത്തൽ — എല്ലാം വ്യക്തമായി അറിയിക്കുന്നു.
- **കൂടാതെ**: ഹോം കാർഡുകൾ ലോഡിംഗിൽ കുടുങ്ങുന്നില്ല, AI വെബ് തിരച്ചിൽ പ്രവർത്തിക്കുന്നു, ഇംപോർട്ട് instance ഫോൾഡറിലേക്ക് പോകുന്നു, ക്രാഷിന് ശേഷമുള്ള യാന്ത്രിക അറ്റകുറ്റപ്പണി നീക്കി.

## 🛠️ എന്തു ചെയ്യും

- **AI ഏജന്റ്**: സ്ഥിരസ്ഥിതി DeepSeek — ഗെയിം, മോഡ്പാക്കുകൾ, മോഡുകൾ ഇൻസ്റ്റാൾ ചെയ്യുന്നു, ക്രമീകരണങ്ങൾ മാറ്റുന്നു, ലോഗുകൾ വായിക്കുന്നു, ഗെയിം തുടങ്ങുന്നു
- **റെൻഡററുകൾ**: ഓട്ടോ / MobileGlues / GL4ES / LTW (പരീക്ഷണാത്മകം)
- **റിസോഴ്സ് കോർ**: Modrinth / CurseForge തിരച്ചിൽ, ആശ്രിതത്വ പരിഹാരം, ഡൗൺലോഡ്, പരിശോധന, ഇൻസ്റ്റാൾ
- **പ്രിയപ്പെട്ടവയും ആർക്കൈവും**: ഓൺലൈൻ പ്രിയപ്പെട്ടവ + സ്വകാര്യ ആർക്കൈവ്, SHA-256 ആവർത്തന ഒഴിവാക്കൽ
- **Fabric API സംരക്ഷണം**: ഇൻസ്റ്റാൾ ഫ്ലോ വഴി മാത്രം ചേർക്കുന്നു — ആവർത്തിക്കാനോ തിരയാനോ ഒഴിവാക്കാനോ കഴിയില്ല

## 🚀 3 ഘട്ടത്തിൽ തുടങ്ങാം

1. [Releases](https://github.com/zzy89216-gif/ZyNova/releases)-ൽ നിന്ന് APK ഡൗൺലോഡ് ചെയ്ത് ഇൻസ്റ്റാൾ ചെയ്യുക (Android 8.0+)
2. ആദ്യ തുടക്കത്തിൽ മൂന്ന് രേഖകളും അവസാനം വരെ വായിച്ച്, ഓരോന്നും ടിക്ക് ചെയ്ത് പ്രായ വിഭാഗം തിരഞ്ഞെടുക്കുക
3. Microsoft അക്കൗണ്ടിൽ പ്രവേശിച്ച് പതിപ്പ് തിരഞ്ഞെടുക്കുകയോ ഇൻസ്റ്റാൾ ചെയ്യുകയോ ചെയ്ത് തുടങ്ങുക

## 📖 പദങ്ങൾ

- **മോഡ്പാക്ക്**: ഒരുമിച്ച് ഇൻസ്റ്റാൾ ചെയ്യുന്ന മോഡുകളുടെ കൂട്ടം
- **ഷേഡർ**: മെച്ചപ്പെട്ട വെളിച്ചവും വെള്ളവും, ഗ്രാഫിക്സ് ക്രമീകരണത്തിൽ ഓൺ ചെയ്യാം
- **ഇൻസ്റ്റൻസ്**: പതിപ്പും മോഡുകളും ക്രമീകരണങ്ങളും അടങ്ങിയ വേറിട്ട ഫോൾഡർ

## 📦 ഡൗൺലോഡും ബിൽഡും

- ഡൗൺലോഡ്: <https://github.com/zzy89216-gif/ZyNova/releases> (എപ്പോഴും ഏറ്റവും പുതിയ പതിപ്പ്)
- സ്വയം ബിൽഡ്: JDK 17 + Android SDK / NDK, ശേഷം `./gradlew ZalithLauncher:assembleRelease`
- ശ്രദ്ധിക്കുക: ആദ്യ തുടക്കത്തിൽ അനുസരണ നടപടിക്രമം വരും; വിശദാംശങ്ങൾ [docs/en/licenses.md](../../docs/en/licenses.md)-ൽ

## 📚 ഡോക്യുമെന്റേഷൻ

> 📚 ഡോക്യുമെന്റേഷൻ ഇപ്പോൾ ചൈനീസിലും ഇംഗ്ലീഷിലും (ഓരോന്നിലും 77 ലേഖനങ്ങൾ); ഈ പേജ് മലയാളം പരിചയം.

| ഉള്ളടക്കം | ഫയൽ |
|---|---|
| സൂചിക | [docs/en/README.md](../../docs/en/README.md) |
| ഇൻസ്റ്റാൾ / ഉപയോഗം / എക്സ്പോർട്ട് | [docs/en/install.md](../../docs/en/install.md) · [docs/en/usage.md](../../docs/en/usage.md) |
| റെൻഡറിംഗ് / ലോഗിൻ / AI | [docs/en/renderers.md](../../docs/en/renderers.md) · [docs/en/login.md](../../docs/en/login.md) · [docs/en/ai-agent.md](../../docs/en/ai-agent.md) |
| പ്രശ്ന പരിഹാരം / FAQ / സ്വകാര്യത | [docs/en/troubleshooting.md](../../docs/en/troubleshooting.md) · [docs/en/faq.md](../../docs/en/faq.md) · [docs/en/data-and-privacy.md](../../docs/en/data-and-privacy.md) |
| ബിൽഡ് / മാറ്റങ്ങൾ | [docs/en/build.md](../../docs/en/build.md) · [CHANGELOG.md](../../CHANGELOG.md) |
| കൈമാറ്റം / മൂന്നാം കക്ഷി ലൈസൻസുകൾ / ലൈസൻസ് | [HANDOFF.md](../../HANDOFF.md) · [THIRD_PARTY_NOTICES.md](../../THIRD_PARTY_NOTICES.md) · [LICENSE](../../LICENSE) |

## 💖 പിന്തുണ

- പ്രശ്നം അറിയിക്കുക：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>
- ഇമെയിൽ：<ZyNovaLauncher@outlook.com>
- ഔദ്യോഗിക X：<https://x.com/ZyNovaProject>
- പ്രോജക്റ്റിനെ പിന്തുണയ്ക്കുക：<https://afdian.com/a/hizzy>

## ⚖️ ലൈസൻസ്

GPL-3.0 — പ്രോജക്റ്റ് ഓപ്പൺ സോഴ്സായി തുടരുന്നു. മൂന്നാം കക്ഷി ഘടകങ്ങളും ലൈസൻസുകളും
[THIRD_PARTY_NOTICES.md](../../THIRD_PARTY_NOTICES.md), [docs/en/licenses.md](../../docs/en/licenses.md) എന്നിവയിൽ.

> ⚠️ **Terracotta (മൾട്ടിപ്ലെയർ) AGPL-3.0-ൽ ആണ്**, മറ്റുള്ളവയേക്കാൾ കർശനം: മാറ്റമില്ലാതെ വിതരണം
> ചെയ്യുന്നു. മാറ്റി വീണ്ടും വിതരണം ചെയ്താൽ, AGPL-3.0 §13 പ്രകാരം ബന്ധപ്പെട്ട സോഴ്സ് കോഡ്
> പ്രസിദ്ധീകരിക്കണം.

---

**ZyNova — Context First. Less Steps.**
