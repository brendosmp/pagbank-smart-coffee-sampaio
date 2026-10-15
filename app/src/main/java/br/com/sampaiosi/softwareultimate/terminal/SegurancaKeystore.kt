package br.com.sampaiosi.softwareultimate.terminal

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object SegurancaKeystore {
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val ALIAS = "software_ultimate_terminal_key"
    private const val TRANSFORMACAO = "AES/GCM/NoPadding"

    data class ResultadoCriptografia(
        val dados: ByteArray,
        val iv: ByteArray
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ResultadoCriptografia

            if (!dados.contentEquals(other.dados)) return false
            if (!iv.contentEquals(other.iv)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = dados.contentHashCode()
            result = 31 * result + iv.contentHashCode()
            return result
        }
    }

    private fun obterOuCriarChave(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
            load(null)
        }

        val existente = keyStore.getKey(ALIAS, null)

        if (existente is SecretKey)
            return existente

        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )

        val configuracao = KeyGenParameterSpec.Builder(
            ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()

        keyGenerator.init(configuracao)

        return keyGenerator.generateKey()
    }

    fun criptografar(valor: String): ResultadoCriptografia {
        val cipher = Cipher.getInstance(TRANSFORMACAO)
        cipher.init(Cipher.ENCRYPT_MODE, obterOuCriarChave())

        return ResultadoCriptografia(
            dados = cipher.doFinal(valor.toByteArray(Charsets.UTF_8)),
            iv = cipher.iv
        )
    }

    fun descriptografar(dados: ByteArray, iv: ByteArray): String {
        val cipher = Cipher.getInstance(TRANSFORMACAO)

        cipher.init(
            Cipher.DECRYPT_MODE,
            obterOuCriarChave(),
            GCMParameterSpec(128, iv)
        )

        return String(cipher.doFinal(dados), Charsets.UTF_8)
    }
}
