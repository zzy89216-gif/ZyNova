#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
ZyNova 文档一致性自检脚本。

每次改代码 / 改文档 / 发版前后跑一遍（CI 也可以直接跑）：

    python3 tools/check_docs.py

检查项：
  1. 版本号单一来源：ZalithLauncher/gradle.properties 的
     launcher_version_code / launcher_version_name，README / CHANGELOG / HANDOFF 必须一致。
  2. 六个 README 必须有 Release 徽章与版本横幅，且语言互链完整。
  3. CHANGELOG 第一条必须是当前版本，且不得出现重复的版本小标题。
  4. HANDOFF 的当前版本与 versionCode 必须与 gradle.properties 一致。
  5. docs/zh 与 docs/en 文件集必须完全对应；两侧索引必须登记全部文件。
  6. 全仓库 Markdown 的相对链接 / 图片必须真实存在（坏链为 0）。
  7. 第三方声明：根目录 THIRD_PARTY_NOTICES.md 与 APK 内 assets 副本必须一字不差。
  8. 禁止出现「只提供 LTS」「只保留一个 Release」这类一次性表述。

退出码：0 全部通过；1 有错误（错误明细直接打印）。
"""

from __future__ import annotations

import os
import re
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

README_LANGS = [
    "README.md",
    "README_EN.md",
    "README_ZH_TW.md",
    "README_JA_JP.md",
    "README_KO.md",
    "README_RU.md",
]

REQUIRED_DOC_GLOB_DIRS = ["docs/zh", "docs/en"]

# 一次性表述黑名单：这些话说一次就过期，禁止写进长期文档
BANNED_PHRASES = [
    "仅提供长期支持",
    "仅提供 LTS",
    "只提供 LTS",
    "只保留一个 Release",
    "唯一保留的 Release",
    "最后一个版本",
    "此后不再更新",
    "不会再发布新版本",
]

CHANGELOG_HEADING_RE = re.compile(r"^##\s*\[([^\]]+)\]", re.M)


class Report:
    def __init__(self) -> None:
        self.errors: list[str] = []
        self.notes: list[str] = []

    def error(self, msg: str) -> None:
        self.errors.append(msg)

    def note(self, msg: str) -> None:
        self.notes.append(msg)

    def ok(self, msg: str) -> None:
        self.notes.append("OK  " + msg)


def rel(path: str) -> str:
    return os.path.relpath(path, ROOT).replace(os.sep, "/")


def read(path: str) -> str:
    with open(os.path.join(ROOT, path), encoding="utf-8") as fh:
        return fh.read()


def load_version(rep: Report) -> tuple[str, str]:
    prop_path = "ZalithLauncher/gradle.properties"
    if not os.path.exists(os.path.join(ROOT, prop_path)):
        rep.error(f"缺少 {prop_path}")
        return "", ""
    props: dict[str, str] = {}
    for line in read(prop_path).splitlines():
        line = line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        k, v = line.split("=", 1)
        props[k.strip()] = v.strip()
    code = props.get("launcher_version_code", "")
    name = props.get("launcher_version_name", "")
    if not code:
        rep.error(f"{prop_path} 缺少 launcher_version_code")
    if not name:
        rep.error(f"{prop_path} 缺少 launcher_version_name")
    if code and not code.isdigit():
        rep.error(f"launcher_version_code 必须是纯数字，当前为 {code!r}")
    if name and not re.fullmatch(r"\d+\.\d+\.\d+(\s+\S+)?", name):
        rep.error(f"launcher_version_name 格式异常：{name!r}（应为 28.1.0 或 28.1.0 LTS）")
    rep.ok(f"版本来源：{prop_path} → {name} (code {code})")
    return code, name


def check_changelog(rep: Report, version: str) -> None:
    if not os.path.exists(os.path.join(ROOT, "CHANGELOG.md")):
        rep.error("缺少 CHANGELOG.md")
        return
    text = read("CHANGELOG.md")
    heads = CHANGELOG_HEADING_RE.findall(text)
    if not heads:
        rep.error("CHANGELOG.md 没有任何 `## [x.y.z]` 标题")
        return
    if heads[0] != version:
        rep.error(f"CHANGELOG 第一条应为 {version}，实际为 {heads[0]}")
    dup = {h for h in heads if heads.count(h) > 1}
    if dup:
        rep.error(f"CHANGELOG 存在重复版本小标题：{', '.join(sorted(dup))}")
    if "历史归档" not in text:
        rep.error("CHANGELOG 缺少「历史归档」章节")
    rep.ok(f"CHANGELOG：{len(heads)} 个版本条目，首条 {heads[0]}")


def check_readmes(rep: Report, version: str) -> None:
    for name in README_LANGS:
        if not os.path.exists(os.path.join(ROOT, name)):
            rep.error(f"缺少 {name}")
            continue
        text = read(name)
        if f"Release-{version}-" not in text:
            rep.error(f"{name} 缺少 Release-{version} 徽章")
        if version not in text:
            rep.error(f"{name} 正文没有出现版本号 {version}")
        # 语言互链：除自己外，另外 5 个 README 都要被链接到
        for other in README_LANGS:
            if other == name:
                continue
            if other not in text:
                rep.error(f"{name} 缺少指向 {other} 的语言互链")
    rep.ok(f"README 六语种自检完成（{len(README_LANGS)} 个文件）")


def check_handoff(rep: Report, code: str, version: str) -> None:
    path = "HANDOFF.md"
    if not os.path.exists(os.path.join(ROOT, path)):
        rep.error("缺少 HANDOFF.md")
        return
    text = read(path)
    if code and f"launcher_version_code={code}" not in text:
        rep.error(f"HANDOFF.md 未标注 launcher_version_code={code}")
    if f"**当前版本：{version}" not in text and f"当前版本：{version}" not in text:
        rep.error(f"HANDOFF.md 的「当前版本」不是 {version}")
    rep.ok(f"HANDOFF：当前版本 {version} / code {code}")


def check_docs_parity(rep: Report) -> None:
    file_sets: dict[str, set[str]] = {}
    for d in REQUIRED_DOC_GLOB_DIRS:
        full = os.path.join(ROOT, d)
        if not os.path.isdir(full):
            rep.error(f"缺少目录 {d}")
            continue
        files = {f for f in os.listdir(full) if f.endswith(".md")}
        file_sets[d] = files
        index = os.path.join(full, "README.md")
        if not os.path.exists(index):
            rep.error(f"{d} 缺少索引 README.md")
            continue
        listed = {
            m.split("/")[-1].split("#")[0]
            for m in re.findall(r"\]\(([^)]+\.md)\)", read(os.path.join(d, "README.md")))
        }
        missing = files - listed
        extra = listed - files
        if missing:
            rep.error(f"{d}/README.md 未登记：{', '.join(sorted(missing))}")
        if extra:
            rep.error(f"{d}/README.md 登记了不存在的文件：{', '.join(sorted(extra))}")
        rep.ok(f"{d}：{len(files)} 篇，索引已登记 {len(listed)} 篇")

    if len(file_sets) == 2:
        zh, en = file_sets["docs/zh"], file_sets["docs/en"]
        only_zh = zh - en
        only_en = en - zh
        if only_zh:
            rep.error(f"中文独有（英文缺）：{', '.join(sorted(only_zh))}")
        if only_en:
            rep.error(f"英文独有（中文缺）：{', '.join(sorted(only_en))}")
        if not only_zh and not only_en:
            rep.ok(f"中英文档一对一：各 {len(zh)} 篇")


def iter_markdown() -> list[str]:
    skip_dirs = {".git", "build", ".gradle", "node_modules", ".idea", "Pods"}
    out: list[str] = []
    for base, dirs, files in os.walk(ROOT):
        dirs[:] = [d for d in dirs if d not in skip_dirs]
        for f in files:
            if f.endswith(".md"):
                out.append(os.path.join(base, f))
    return out


def check_links(rep: Report) -> None:
    bad = 0
    for path in iter_markdown():
        text = open(path, encoding="utf-8").read()
        base = os.path.dirname(path)
        targets = re.findall(r"!?\[[^\]]*\]\(([^)\s]+)\)", text)
        targets += re.findall(r'(?:src|href)="([^"]+)"', text)
        for url in targets:
            if url.startswith(("http://", "https://", "mailto:", "#", "tel:")):
                continue
            clean = url.split("#")[0].split("?")[0]
            if not clean:
                continue
            if not os.path.exists(os.path.normpath(os.path.join(base, clean))):
                rep.error(f"坏链：{rel(path)} → {url}")
                bad += 1
    if bad == 0:
        rep.ok(f"坏链检查：{len(iter_markdown())} 个 md 文件，0 个坏链")


def check_notices(rep: Report) -> None:
    a = "THIRD_PARTY_NOTICES.md"
    b = "ZalithLauncher/src/main/assets/licenses/THIRD_PARTY_NOTICES.md"
    for p in (a, b):
        if not os.path.exists(os.path.join(ROOT, p)):
            rep.error(f"缺少 {p}")
            return
    if read(a) != read(b):
        rep.error(f"{a} 与 {b} 内容不一致（APK 内声明必须与仓库一字不差）")
    else:
        rep.ok("第三方声明：仓库与 APK assets 副本一致")


def check_banned_phrases(rep: Report) -> None:
    for path in iter_markdown():
        text = open(path, encoding="utf-8").read()
        for phrase in BANNED_PHRASES:
            if phrase in text:
                rep.error(f"一次性表述「{phrase}」出现在 {rel(path)}")
    rep.ok("一次性表述检查完成")


def main() -> int:
    rep = Report()
    code, name = load_version(rep)
    version = name.split()[0] if name else ""
    if version:
        check_changelog(rep, version)
        check_readmes(rep, version)
        check_handoff(rep, code, version)
    check_docs_parity(rep)
    check_links(rep)
    check_notices(rep)
    check_banned_phrases(rep)

    print("=" * 60)
    print("ZyNova 文档自检")
    print("=" * 60)
    for line in rep.notes:
        print(" " + line)
    if rep.errors:
        print("-" * 60)
        print(f"发现 {len(rep.errors)} 个问题：")
        for i, e in enumerate(rep.errors, 1):
            print(f"  {i}. {e}")
        print("=" * 60)
        return 1
    print("-" * 60)
    print("全部通过 ✅")
    return 0


if __name__ == "__main__":
    sys.exit(main())
