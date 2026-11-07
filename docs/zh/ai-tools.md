# AI 能做什么

- 装游戏本体 / 整合包 / 模组 / 资源包 / 光影 / 存档
- 改实例配置（渲染器、Java、内存、JVM 参数）
- 读写 `options.txt`
- 查收藏 / 取消收藏
- 读日志、启动游戏（受工具能力限制）
- **联机全自动**：
  - `multiplayer_status` 查陶瓦联机开关、自定义节点、樱花登录状态
  - `frp_status` 查樱花账号（用户组 / 额度 / 流量）、隧道列表、frpc 运行状态
  - `frp_create_tunnel` 建隧道：**类型自动固定 TCP、本机端口自动识别、节点自动挑**
  - `frp_start_tunnel` / `frp_stop_tunnel` 启停隧道

所以像「帮我把联机开起来」这种话，它可以一路做完，中途不需要你告诉它端口是多少。

- **联网**：`web_search` 搜索、`fetch_webpage` 读网页正文、`search_mcmod` 查 MC 百科（设置里可关；禁止访问内网）

它不会凭空获得未提供的权限。


## 文件能力

| 工具 | 作用 |
|---|---|
| `list_files` / `read_file` / `write_file` / `delete_file` / `copy_file` | App 内（游戏目录）读写 |
| `public_list` / `public_read` / `public_write` / `public_mkdir` | 公有存储（如 Download；需「所有文件访问权限」） |
| `public_move_to_recycle_bin` | 清理文件，**移动到回收站**（App 私有 `.recycle_bin/`），不会真删 |
| `import_from_public` | 把 Download 里的 jar/整合包**复制进游戏目录** |

边界：只在 `/storage/emulated/0/` 下；**禁止** `Android/data` 与 `Android/obb`。


## 文档生成

| 工具 | 作用 |
|---|---|
| `create_docx` | 生成 Word 文档（标题 + 正文/要点），默认存 `Download/` |
| `create_pptx` | 生成 PPT，`slides` 传 JSON 数组 `[{"title":"…","points":["…"]}]`，默认存 `Download/` |

两者都是**手写 OOXML + zip**（不引 Apache POI，包体不涨），需要「所有文件访问权限」；
没授权时工具会直接告诉你先去哪个开关打开。直接说「帮我做一份关于 X 的 PPT」即可。

## 游戏设置的真实键名

`options.txt` 里这些不是人话字符串，工具读写时已做映射：

| 人话 | 真实键 | 真实取值 |
|---|---|---|
| 图形模式 | `graphicsMode` | 0=fancy、1=fast、2=fabulous |
| 粒子效果 | `particles` | 0=all、1=decreased、2=minimal |
| 视场角 | `fov` | 归一化浮点，1.0 = 默认 70° |


## 联网

- 只有一个开关：设置 → AI 设置 → 网络搜索 → 允许 AI 联网
- 固定使用 **DuckDuckGo**（免 Key），不再需要配置任何引擎或 API Key
- 搜索解析失败时，AI 会自动改用 `fetch_webpage` 抓结果页


## 全部工具（51 个）

| 分类 | 工具 |
|---|---|
| 安装与启动 | `list_downloadable_versions` `install_game` `install_status` `launch_game` `get_device_info` |
| 资源与整合包 | `search_resources` `install_resource` `install_modpack` `list_mods` `set_mod_enabled` `delete_mod` `delete_resource` `list_folder` |
| 实例 | `list_instances` `get_instance_detail` `get_instance_config` `set_instance_config` `set_game_option` |
| 文件 | `list_files` `read_file` `search_in_file` `write_file` `delete_file` `copy_file` |
| 公有存储 | `public_list` `public_read` `public_write` `public_mkdir` `public_move_to_recycle_bin` `import_from_public` |
| 文档生成 | `create_docx` `create_pptx` |
| 日志与崩溃 | `list_logs` `read_log` `summarize_log` `read_crash_report` `read_crash_report_legacy` |
| 联网 | `web_search` `fetch_webpage` `search_mcmod` |
| 联机 | `multiplayer_status` `frp_status` `frp_create_tunnel` `frp_start_tunnel` `frp_stop_tunnel` |
| 收藏 | `list_favorites` `remove_favorite` |
| 设置 | `list_settings` `get_setting` `set_setting` `update_ai_setting` |
