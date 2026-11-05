/*
 * ZyNova Launcher
 * Copyright (C) 2026 zzy89216-gif and contributors
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

package com.movtery.zalithlauncher.frp

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * 樱花内网穿透访问密钥的本地加密存储。
 *
 * 为什么不能直接存明文：
 * - 访问密钥等同于账号控制权，能创建 / 删除隧道；
 * - 它会随「最近一次成功登录」长期留在本机，一旦被别的 App 读到，
 *   对方可以直接接管用户的穿透隧道。
 *
 * 实现：
 * - 密钥本身由 **Android Keystore** 生成并保管（不出硬件 / 不出应用进程）；
 * - 落盘内容仅为 `Base64(IV || AES-GCM 密文)`，写入 MMKV（应用私有目录）；
 * - 其他应用没有本应用的 Keystore 别名，即使拿到 MMKV 文件也解不开；
 * - 解密失败（换机、清除数据、Keystore 失效）时返回 null，由上层要求重新输入，
 *   不会回退成明文保存。
 */
object FrpSecret {
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "zynova_frp_token"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val IV_SIZE = 12
    private const val TAG_BITS = 128

    /**
     * 加密访问密钥；失败返回 null（调用方应当拒绝保存，而不是退化为明文）。
     */
    fun encrypt(plain: String): String? = runCatching {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val encrypted = cipher.doFinal(plain.toByteArray(Charsets.UTF_8))
        val iv = cipher.iv
        val output = ByteArray(iv.size + encrypted.size)
        System.arraycopy(iv, 0, output, 0, iv.size)
        System.arraycopy(encrypted, 0, output, iv.size, encrypted.size)
        Base64.encodeToString(output, Base64.NO_WRAP)
    }.getOrNull()

    /**
     * 解密访问密钥；失败返回 null。
     */
    fun decrypt(stored: String): String? = runCatching {
        if (stored.isBlank()) return null
        val bytes = Base64.decode(stored, Base64.NO_WRAP)
        if (bytes.size <= IV_SIZE) return null
        val iv = bytes.copyOfRange(0, IV_SIZE)
        val data = bytes.copyOfRange(IV_SIZE, bytes.size)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(TAG_BITS, iv))
        String(cipher.doFinal(data), Charsets.UTF_8)
    }.getOrNull()

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry)?.let {
            return it.secretKey
        }

        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build()
        )
        return generator.generateKey()
    }
}
