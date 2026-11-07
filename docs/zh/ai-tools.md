# AI 能做什么

- 装游戏本体 / 整合包 / 模组 / 资源包 / 光影 / 存档
- 改实例配置（渲染器、Java、内存、JVM 参数）
- 读写 `options.txt`
- 查收藏 / 取消收藏
- 读日志、启动游戏（受工具能力限制）
- **联机全自动**（28.0.1 起）：
  - `multiplayer_status` 查陶瓦联机开关、自定义节点、樱花登录状态
  - `frp_status` 查樱花账号（用户组 / 额度 / 流量）、隧道列表、frpc 运行状态
  - `frp_create_tunnel` 建隧道：**类型自动固定 TCP、本机端口自动识别、节点自动挑**
  - `frp_start_tunnel` / `frp_stop_tunnel` 启停隧道

所以像「帮我把联机开起来」这种话，它可以一路做完，中途不需要你告诉它端口是多少。

- **联网**：`web_search` 搜索、`fetch_webpage` 读网页正文、`search_mcmod` 查 MC 百科（设置里可关；禁止访问内网）

它不会凭空获得未提供的权限。

## 联网与引擎（28.0.5）

- 开关：设置 → AI 设置 → 网络搜索 → 允许 AI 联网
- 引擎四选一：`DuckDuckGo`（免 Key）/ `百度千帆` / `Bing(Azure)` / `自建接口`
- 选需要 Key 的引擎后，Key 输入框右侧有「**创建 API**」按钮，点了直接跳官方创建页；
  自建接口还需填接口地址
- 任何一家失败都会**自动回退 DuckDuckGo**，不会出现「搜不了」
- 直接把 Key 发在聊天里也行：「我的搜索 Key 是 xxx，帮我配上」→ AI 调 `update_ai_setting` 自己写进本地存储，
  界面里这句密钥会显示成打码形式

## 文件能力（28.0.5）

| 工具 | 作用 |
|---|---|
| `list_files` / `read_file` / `write_file` / `delete_file` / `copy_file` | App 内（游戏目录）读写 |
| `public_list` / `public_read` / `public_write` / `public_mkdir` | 公有存储（如 Download；需「所有文件访问权限」） |
| `public_move_to_recycle_bin` | 清理文件，**移动到回收站**（App 私有 `.recycle_bin/`），不会真删 |
| `import_from_public` | 把 Download 里的 jar/整合包**复制进游戏目录** |

边界：只在 `/storage/emulated/0/` 下；**禁止** `Android/data` 与 `Android/obb`。

## 崩溃自愈（28.0.5）

- 游戏以非 0 退出码结束后，会记录版本 / 退出码 / 时间；
- 回到启动器弹「自动修复 / 看日志 / 知道了」；
- 设置里可开「崩溃后自动修复」：不弹框，直接交给 AI；**默认关闭**（会消耗额度）。
