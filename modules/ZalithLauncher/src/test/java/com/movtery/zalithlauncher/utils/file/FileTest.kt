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

package com.movtery.zalithlauncher.utils.file

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.jackhuang.hmcl.util.DigestUtils
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class FileTest {

    /**
     * ⚠️ 以前这里写死了一个开发者本机的 Windows 路径（`F:\Download\...`），
     * 在 CI / 任何别的机器上必然抛 IllegalArgumentException，等于没有测试。
     * 现在用临时文件，断言两条 SHA-1 实现结果一致。
     */
    @Test
    fun testCalculateFileSha1() {
        val file = File.createTempFile("sha1-test", ".bin").apply {
            deleteOnExit()
            writeBytes(ByteArray(64 * 1024) { index -> (index * 31 + 7).toByte() })
        }

        runBlocking(Dispatchers.IO) {
            val expected = DigestUtils.digestToString("SHA-1", file.toPath())
            val actual = calculateFileSha1(file)
            println("sha1 (DigestUtils) = $expected")
            println("sha1 (FileUtils)   = $actual")
            assertTrue("SHA-1 should match, expected=$expected actual=$actual", expected.equals(actual, ignoreCase = true))
        }
    }
}