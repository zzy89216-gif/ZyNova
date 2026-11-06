# PCL-FE 陶瓦联机流程（移植参考）

> 来源：`PCLPro/Pages/PageLink/PageLinkMain.xaml.vb`（1393 行）与 `PageLinkMain.xaml`
> 用途：27.10.2「陶瓦流程移植（A 方案）」。
> **只搬交互与边界处理，不搬代码结构**：ZyNova 已有原生 Terracotta（`terracotta/` + VPN 服务），
> 不需要再调 `terracotta.exe`。

## 1. 房间码算法

字符表（34 个，去掉了容易混淆的 I、O 大写形式）：

```
0123456789ABCDEFGHJKLMNPQRSTUVWXYZ
```

一个房间码 = **16 位**，展示格式：

```
U/XXXX-XXXX-XXXX-XXXX
```

生成（`TcRandomRoom`）：

1. 取 16 个随机字节（`RandomNumberGenerator`）；
2. 当作大端 BigInteger，`mod 34^16`；
3. **`value -= value mod 7`**（保证能被 7 整除，用于校验/兼容）；
4. 按 16 位 34 进制格式化。

同时派生两个值：

- `NetworkName = "scaffolding-mc-" + 第 1~2 组`（8 位）；
- `NetworkSecret = 第 3~4 组`（8 位，带连字符）。

解析（`TcParse`）：

- 允许 `U/` 前缀、`【】`/`[]` 包裹、空格与换行；
- 全角转半角，`I → 1`、`O → 0`，各种连字符归一成 `-`；
- 先按「高位在前」转 BigInteger，`mod 7 == 0` 判定成功；
- 否则按「低位在前」再试一次（兼容旧版 PCL-FE 生成的房间码）；
- 都失败 → 提示「房间码有误，请让房主重新发送」。

> Android 端实现要点：`BigInteger` 在 Kotlin 里可用 `java.math.BigInteger`；
> 34 进制转换一定要用 BigInteger，不能用 Long。

## 2. 状态机

```
Waiting → Loading → Finished
              ↘ Failed
```

- `Waiting`：选择「创建房间 / 加入房间」；
- `Loading`：四段加载任务，带进度：
  1. 获取配置（`InitConfig`，失败可忽略，直接用默认）；
  2. 准备陶瓦模块（`InitPrepare`）；
  3. 启动陶瓦模块（`InitLaunch`，超时 **60s**）；
  4. 进入房间（`InitEnterRoom`，超时 **180s**）；
- `Finished`：展示房间码 / 连接地址，可复制、可退出；
- `Failed`：给出失败摘要 + 详情，允许重试。

退出时要处理：`TryExit(Silent, Closing)`，确保子进程/状态被复位，避免下次建房卡死。

## 3. 房主流程（PCL-FE 的边界处理）

1. **先检查游戏是否在运行**：没检测到就弹确认框，说明要先「对局域网开放」；
2. **手动输入游戏端口**（`MyMsgBoxInput`，1~65535）：
   - 默认填上次端口或 25565；
   - 用 Socket 探测端口是否在监听，未监听再弹一次确认；
3. **选择节点**（见第 5 节）；
4. **检查防火墙**：`ModFirewall.CheckTerracottaFirewall()`；
5. 生成房间码 → 进入 `Loading` → 成功后展示「复制房间码 / 邀请」。

## 4. 房客流程

1. 输入房间码 → 先做**格式校验**（`ValidateCodeFormat`），不合格直接红字提示；
2. `FixCodeFormat` 归一化（全角/空格/前缀）；
3. 解析出 `房间码 / 网络名 / 密钥`；
4. 触发防火墙检查；
5. 进入 `Loading`，加入成功展示连接状态；
6. 记住上一次成功的房间码，作为下次输入提示。

## 5. 节点选择

- 拉取公共节点列表；
- 对每个节点做 **ping 测速**；
- 下拉列表显示「节点名 + 延迟」，按延迟排序并默认选最优；
- 全部不可用时提示「没有可用的公共节点」；
- 节点结果缓存 **10 分钟**（PCL-FE 的 `TcNodeCacheMs`）。

> ZyNova 现状：已经有 `TerracottaNodeList`（从 `terracotta.glavo.site/nodes` 拉取），
> 需要补的是「测速 + 默认选最优 + 10 分钟缓存 + 手动切换」。

## 6. 常见失败与引导（PCL-FE 踩过的坑）

| 现象 | 根因 | 引导 |
|---|---|---|
| 房间码加入失败 | 没补 `U/` 前缀、没复位到 Waiting、单实例残留状态卡死 | 归一化 + 重置状态 + 杀掉残留进程 |
| 房主建房成功但没端口 | 启动器占用 UDP 4445，抢走陶瓦的局域网广播 | 启动前检查/让出 4445 |
| 客人始终连不上 | 内置公共节点失效 | 显式指定当前可用节点（见第 5 节） |
| 端口填错 | 游戏实际端口与输入不一致 | Socket 探测 + 二次确认 |

## 7. 移植到 ZyNova 的落点

| 能力 | 落点 |
|---|---|
| 建房前检查游戏运行 | `MultiplayerScreen` / `TerracottaViewModel` |
| 端口输入 + 探测 | 新增对话框 + `ServerSocket` 绑定检测 |
| 房间码生成/解析/复制 | 新增 `terracotta/RoomCode.kt`（34 进制 + mod 7） |
| 节点测速/缓存 | `terracotta/TerracottaNodeList.kt` 扩展 |
| 失败引导 | `TerracottaState.Exception` → UI 文案映射 |
| 教程页 | `MultiplayerScreen` 的 TutorialMenu（已有三页，补内容即可） |

⚠️ **不要改** `libterracotta.so`、`TerracottaVPNService` 与现有状态机语义。
