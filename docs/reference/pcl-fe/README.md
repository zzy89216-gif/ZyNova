# PCL-FE 参考实现归档

这里保存的是 **PCL-FE**（`zzy89216-gif/PCL-FE`）里对 ZyNova 仍然有价值的两块实现，
用于 27.10.2 的移植工作。原仓库删除后，这些资料仍然可用。

## 为什么只搬这些

PCL-FE 是 Windows 桌面端（VB.NET + WPF），与 ZyNova（Android / Kotlin / Compose）**不能共用代码**。
真正需要保留的只有「逻辑与视觉配方」：

| 内容 | 用途 | 文件 |
|---|---|---|
| 陶瓦联机完整交互 | 移植「建房前检查 / 端口探测 / 房间码 / 节点选择 / 失败引导」 | `terracotta-flow.md` |
| 液态玻璃完整实现 | 27.10.2 玻璃地基的参考参数与做法 | `liquid-glass.md` |
| 两份原始源码 | 需要核对细节时直接查 | `source/*.txt` |

**没有搬**：主程序、EXE、MeloongCore、图片资源、更新日志——这些对 Android 端没有意义。

## 许可与归属

- 上游是 PCL2（作者：龙腾猫跃），PCL-FE 是第三方修改版，继续沿用
  [《PCL 存储库合理使用指南》](https://github.com/Meloong-Git/PCL/blob/main/LICENCE)；
- `source/` 里的文件**只作参考、不参与编译**，因此统一加了 `.txt` 后缀；
- ZyNova 自身仍是 GPL-3.0；这里不复制、也不重新分发 PCL 的任何二进制。

## 使用方式

27.10.2 的移植工作请配合 **Issue #42** 一起看：

1. 先读本目录两份分析文档（结论已经提炼好，不用再啃 2000 行 VB）；
2. 需要核对算法细节时再翻 `source/`；
3. 移植时**不要照抄代码结构**——Windows 的进程/窗口模型在 Android 上不成立。
