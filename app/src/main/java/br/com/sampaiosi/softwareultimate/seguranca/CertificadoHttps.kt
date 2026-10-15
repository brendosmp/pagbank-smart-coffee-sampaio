package br.com.sampaiosi.softwareultimate.seguranca

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.math.BigInteger
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.util.Calendar
import javax.security.auth.x500.X500Principal

object CertificadoHttps {
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val ALIAS = "software_ultimate_https"

    fun obterKeyStore(): KeyStore {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
            load(null)
        }

        if (!keyStore.containsAlias(ALIAS))
            gerarCertificado()

        return KeyStore.getInstance(ANDROID_KEYSTORE).apply {
            load(null)
        }
    }

    private fun gerarCertificado() {
        val inicio = Calendar.getInstance()

        val fim = Calendar.getInstance().apply {
            add(Calendar.YEAR, 20)
        }

        val gerador = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_RSA,
            ANDROID_KEYSTORE
        )

        val configuracao = KeyGenParameterSpec.Builder(
            ALIAS,
            KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
        )
            .setKeySize(2048)
            .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
            .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
            .setCertificateSubject(X500Principal("CN=Software Ultimate, O=Sampaio SI, C=BR"))
            .setCertificateSerialNumber(BigInteger.ONE)
            .setCertificateNotBefore(inicio.time)
            .setCertificateNotAfter(fim.time)
            .build()

        gerador.initialize(configuracao)
        gerador.generateKeyPair()
    }

    fun obterCertificado() =
        obterKeyStore().getCertificate(ALIAS)
}
