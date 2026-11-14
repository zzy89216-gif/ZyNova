<p align="center"><sub>
<a href="../../README_EN.md">English</a> · <a href="../../README.md">简体中文</a> · <a href="../zh-Hant/README.md">繁體中文</a> · <a href="../ja/README.md">日本語</a> · <a href="../ko/README.md">한국어</a> · <a href="../tr/README.md">Türkçe</a> · <a href="../ru/README.md">Русский</a> · <a href="../vi/README.md">Tiếng Việt</a> · <b>ไทย</b> · <a href="../de/README.md">Deutsch</a> · <a href="../es/README.md">Español</a> · <a href="../fr/README.md">Français</a>
<br>
<a href="../uk/README.md">Українська</a> · <a href="../pl/README.md">Polski</a> · <a href="../pt-BR/README.md">Português (Brasil)</a> · <a href="../ar/README.md">العربية</a> · <a href="../fa/README.md">فارسی</a> · <a href="../id/README.md">Bahasa Indonesia</a> · <a href="../it/README.md">Italiano</a> · <a href="../el/README.md">Ελληνικά</a> · <a href="../ml/README.md">മലയാളം</a> · <a href="../ur-Latn/README.md">Roman Urdu</a> · <a href="../ro/README.md">Română</a> · <a href="../bg/README.md">Български</a>
</sub></p>

<p align="center">
  <img src="../../assets/icon.png" width="160" alt="ZyNova">
</p>

# ZyNova

> ⭐ **ZyNova 28.1.0 เปิดให้ดาวน์โหลดแล้ว (เวอร์ชันที่สอดคล้องข้อกำหนดทั่วโลก)**
>
> 📌 **Current release: 28.2.1** · [Changelog](../../CHANGELOG.md) · [Releases](https://github.com/zzy89216-gif/ZyNova/releases/latest)


[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)](https://developer.android.com/)
[![Release](https://img.shields.io/badge/Release-28.2.1-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v28.2.1)

---

ตัวเปิดเกม **Minecraft: Java Edition** บน Android พัฒนาแยกจากซอร์สโค้ด ZalithLauncher2 ด้วยตนเอง ไม่ใช่บิลด์ทางการ

แนวคิดมีข้อเดียว: **แตะน้อยลง** แทนที่จะกางการตั้งค่ายอกมาให้เลือกเอง ตัวเปิดเกมจะตัดสินใจให้ก่อน และถามเฉพาะตอนที่ตัดสินใจไม่ได้จริง ๆ

## ✨ สิ่งใหม่ใน 28.1.0

- **ขั้นตอนข้อกำหนดตอนเปิดครั้งแรก**: เอกสารรวมสามฉบับ (① EULA + นโยบายความเป็นส่วนตัว, ② ประกาศโอเพนซอร์สของบุคคลที่สาม + ข้อจำกัดความรับผิดและความเสี่ยง, ③ คู่มือการใช้งาน + พื้นที่และเขตอำนาจทางกฎหมาย) ต้อง**เลื่อนจนสุดและติ๊กแยกทีละฉบับ** จากนั้นเลือกช่วงอายุ (ผู้ใช้เดิมก็เจอหนึ่งครั้งหลังอัปเดต)
- **ช่วงเวลาสำหรับผู้เยาว์**: อายุต่ำกว่า 18 ปี เปิดเกมได้เฉพาะวันศุกร์ เสาร์ อาทิตย์ และวันหยุดราชการ เวลา **20:00–21:00** โดยวันหยุดปี 2026 อ้างตามปฏิทินคณะรัฐมนตรีจีน และวันหยุดชดเชยที่ต้องทำงานถือเป็นวันทำงาน
- **ข้อจำกัดความรับผิดเขียนครบ**: ข้อมูลเซฟหาย เครื่องร้อน บัญชีถูกบุคคลที่สามแบน บริการปิดตัว ระบุไว้ชัดเจน
- **นอกจากนี้**: การ์ดหน้าแรกไม่ค้างที่สถานะโหลด, ค้นหาเว็บของ AI ใช้ได้, นำเข้าไปยังโฟลเดอร์อินสแตนซ์, ถอดระบบซ่อมแซมหลังแครชออกแล้ว

## 🛠️ ความสามารถ

- **เอเจนต์ AI**: ค่าเริ่มต้น DeepSeek — ติดตั้งเกม ม็อดแพ็ก และม็อด เปลี่ยนการตั้งค่า อ่านบันทึก เปิดเกม
- **ตัวเรนเดอร์**: อัตโนมัติ / MobileGlues / GL4ES / LTW (ทดลอง)
- **แกนทรัพยากร**: ค้นหาใน Modrinth / CurseForge แก้ไข dependency ดาวน์โหลด ตรวจสอบ ติดตั้ง
- **รายการโปรดและคลังเก็บ**: รายการโปรดออนไลน์ + คลังส่วนตัว กันซ้ำด้วย SHA-256
- **การป้องกัน Fabric API**: เพิ่มโดยขั้นตอนติดตั้งเท่านั้น — ติดตั้งซ้ำไม่ได้ ค้นไม่เจอ ลบไม่ได้

## 🚀 เริ่มใน 3 ขั้น

1. ดาวน์โหลด APK จาก [Releases](https://github.com/zzy89216-gif/ZyNova/releases) แล้วติดตั้ง (Android 8.0+)
2. เปิดครั้งแรก อ่านเอกสารทั้งสามจนจบ ติ๊กทีละฉบับ แล้วเลือกช่วงอายุ
3. เข้าสู่ระบบด้วยบัญชี Microsoft เลือกหรือติดตั้งเวอร์ชัน แล้วกดเล่น

## 📦 ดาวน์โหลดและคอมไพล์เอง

- ดาวน์โหลด: <https://github.com/zzy89216-gif/ZyNova/releases> (ใช้เวอร์ชันล่าสุดเสมอ)
- คอมไพล์เอง: JDK 17 + Android SDK / NDK แล้วรัน `./gradlew ZalithLauncher:assembleRelease`
- หมายเหตุ: เปิดครั้งแรกจะมีขั้นตอนข้อกำหนด รายละเอียดอยู่ใน [docs/en/licenses.md](../../docs/en/licenses.md)

## 📚 เอกสาร

> 📚 เอกสารมีฉบับภาษาจีนและอังกฤษ (ชุดละ 77 บทความ) หน้านี้เป็นคำแนะนำฉบับภาษาไทย

| เนื้อหา | ไฟล์ |
|---|---|
| สารบัญเอกสาร | [docs/en/README.md](../../docs/en/README.md) |
| ติดตั้ง / ใช้งาน / ส่งออก | [docs/en/install.md](../../docs/en/install.md) · [docs/en/usage.md](../../docs/en/usage.md) |
| ตัวเรนเดอร์ / เข้าสู่ระบบ / AI | [docs/en/renderers.md](../../docs/en/renderers.md) · [docs/en/login.md](../../docs/en/login.md) · [docs/en/ai-agent.md](../../docs/en/ai-agent.md) |
| แก้ปัญหา / คำถามที่พบบ่อย / ข้อมูลและความเป็นส่วนตัว | [docs/en/troubleshooting.md](../../docs/en/troubleshooting.md) · [docs/en/faq.md](../../docs/en/faq.md) · [docs/en/data-and-privacy.md](../../docs/en/data-and-privacy.md) |
| คอมไพล์เอง / บันทึกการเปลี่ยนแปลง | [docs/en/build.md](../../docs/en/build.md) · [CHANGELOG.md](../../CHANGELOG.md) |
| การส่งต่องาน / สัญญาอนุญาตบุคคลที่สาม / สัญญาอนุญาต | [HANDOFF.md](../../HANDOFF.md) · [THIRD_PARTY_NOTICES.md](../../THIRD_PARTY_NOTICES.md) · [LICENSE](../../LICENSE) |

## 💖 สนับสนุน

- แจ้งปัญหา：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>
- อีเมล：<ZyNovaLauncher@outlook.com>
- X อย่างเป็นทางการ：<https://x.com/ZyNovaProject>
- สนับสนุนโครงการ：<https://afdian.com/a/hizzy>

## ⚖️ สัญญาอนุญาต

GPL-3.0 — โครงการยังคงเป็นโอเพนซอร์ส คอมโพเนนต์ของบุคคลที่สามและสัญญาอนุญาตอยู่ใน
[THIRD_PARTY_NOTICES.md](../../THIRD_PARTY_NOTICES.md) และ [docs/en/licenses.md](../../docs/en/licenses.md)

> ⚠️ **Terracotta (เล่นหลายคน) ใช้ AGPL-3.0** ซึ่งเข้มงวดกว่าส่วนอื่น: แจกจ่ายแบบไม่แก้ไข
> หากคุณแก้ไขแล้วเผยแพร่ต่อ มาตรา 13 ของ AGPL-3.0 กำหนดให้เปิดเผยซอร์สโค้ดที่เกี่ยวข้อง

---

**ZyNova — Context First. Less Steps.**
