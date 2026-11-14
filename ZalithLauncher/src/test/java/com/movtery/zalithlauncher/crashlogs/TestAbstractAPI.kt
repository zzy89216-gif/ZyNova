/*
 * Zalith Launcher 2
 * Copyright (C) 2025 MovTery <movtery228@qq.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.movtery.zalithlauncher.crashlogs

import com.movtery.zalithlauncher.crashlogs.platform.MCLogsAPI
import com.movtery.zalithlauncher.crashlogs.platform.MirroredAPI
import kotlinx.coroutines.runBlocking
import org.junit.Ignore
import org.junit.Test

/*
 * ⚠️ 这是**手动**集成测试：会真的把内容上传到 logshare.cn / mclo.gs。
 * 放进 CI 会因为外网、限流、服务端抽风随机变红，所以默认忽略；要验证上传能力时本地去掉注解跑一次。
 */
@Ignore("手动运行：需要真实网络上传（logshare / mclo.gs），不进 CI")
class TestAbstractAPI {
    @Test
    fun testMirroredAPI() {
        runBlocking {
            MirroredAPI.onUpload("TEST CONTENT")
        }
    }

    @Test
    fun testMCLogsAPI() {
        runBlocking {
            MCLogsAPI.onUpload("TEST CONTENT")
        }
    }
}