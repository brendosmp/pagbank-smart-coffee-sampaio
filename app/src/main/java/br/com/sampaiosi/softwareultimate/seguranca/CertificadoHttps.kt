package br.com.sampaiosi.softwareultimate.seguranca

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import br.com.sampaiosi.softwareultimate.terminal.IdentificadorTerminal
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x509.BasicConstraints
import org.bouncycastle.asn1.x509.ExtendedKeyUsage
import org.bouncycastle.asn1.x509.Extension
import org.bouncycastle.asn1.x509.KeyPurposeId
import org.bouncycastle.asn1.x509.KeyUsage
import org.bouncycastle.cert.X509v3CertificateBuilder
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.math.BigInteger
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.Date
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object CertificadoHttps {

    private const val TAG = "CertificadoHttps"

    private const val NOME_ARQUIVO_ANTIGO =
        "software_ultimate_https.p12"

    private const val NOME_ARQUIVO_CRIPTOGRAFADO =
        "software_ultimate_https.p12.enc"

    private const val ALIAS_CERTIFICADO =
        "software_ultimate_https"

    private const val ALIAS_CHAVE_AES =
        "software_ultimate_https_aes"

    private const val ANDROID_KEYSTORE =
        "AndroidKeyStore"

    private const val ALGORITMO_ASSINATURA =
        "SHA256withRSA"

    private const val TRANSFORMACAO_AES =
        "AES/GCM/NoPadding"

    private const val TAMANHO_IV = 12

    /*
     * Essa senha não é mais a proteção principal.
     * O PKCS12 inteiro fica criptografado com AES-GCM.
     *
     * Mantemos a mesma senha para permitir migração
     * do PKCS12 criado anteriormente.
     */
    internal const val SENHA_KEYSTORE =
        "SoftwareUltimateHttps2026"

    private val provider =
        BouncyCastleProvider()

    fun obterKeyStore(
        context: Context
    ): KeyStore {

        Log.i(
            TAG,
            "Obtendo KeyStore HTTPS protegido"
        )

        migrarArquivoAntigoSeNecessario(
            context
        )

        val arquivo =
            obterArquivoCriptografado(
                context
            )

        if (!arquivo.exists()) {
            Log.i(
                TAG,
                "Certificado protegido ainda não existe. Gerando..."
            )

            gerar(context)
        }

        return try {
            carregarCriptografado(
                context
            )
        } catch (e: Exception) {
            Log.e(
                TAG,
                "Erro ao carregar certificado HTTPS protegido",
                e
            )

            throw IllegalStateException(
                "Não foi possível carregar o certificado HTTPS.",
                e
            )
        }
    }

    fun obterCertificado(
        context: Context
    ): X509Certificate {

        Log.i(
            TAG,
            "Obtendo certificado X509"
        )

        val keyStore =
            obterKeyStore(context)

        val certificado =
            keyStore.getCertificate(
                ALIAS_CERTIFICADO
            ) as? X509Certificate
                ?: throw IllegalStateException(
                    "Não foi possível obter o certificado HTTPS."
                )

        Log.i(
            TAG,
            "Certificado obtido com sucesso"
        )

        Log.i(
            TAG,
            "Subject: ${certificado.subjectX500Principal}"
        )

        Log.i(
            TAG,
            "Validade inicial: ${certificado.notBefore}"
        )

        Log.i(
            TAG,
            "Validade final: ${certificado.notAfter}"
        )

        return certificado
    }

    private fun gerar(
        context: Context
    ) {
        try {
            Log.i(
                TAG,
                "Iniciando geração do certificado HTTPS"
            )

            val terminalId =
                IdentificadorTerminal
                    .obterOuCriarTerminalId(context)

            Log.i(
                TAG,
                "Terminal ID utilizado: $terminalId"
            )

            Log.i(
                TAG,
                "Gerando chave RSA 2048 bits em software"
            )

            val geradorChaves =
                KeyPairGenerator.getInstance(
                    "RSA"
                )

            geradorChaves.initialize(
                2048,
                SecureRandom()
            )

            val parChaves =
                geradorChaves.generateKeyPair()

            Log.i(
                TAG,
                "Par de chaves RSA gerado"
            )

            val agora =
                System.currentTimeMillis()

            val inicio =
                Date(
                    agora -
                            24L * 60L * 60L * 1000L
                )

            val fim =
                Date(
                    agora +
                            3650L * 24L * 60L * 60L * 1000L
                )

            val serial =
                BigInteger(
                    160,
                    SecureRandom()
                ).abs()

            val nome =
                X500Name(
                    "CN=$terminalId,O=Sampaio SI,C=BR"
                )

            val builder: X509v3CertificateBuilder =
                JcaX509v3CertificateBuilder(
                    nome,
                    serial,
                    inicio,
                    fim,
                    nome,
                    parChaves.public
                )

            builder.addExtension(
                Extension.basicConstraints,
                true,
                BasicConstraints(false)
            )

            builder.addExtension(
                Extension.keyUsage,
                true,
                KeyUsage(
                    KeyUsage.digitalSignature or
                            KeyUsage.keyEncipherment
                )
            )

            builder.addExtension(
                Extension.extendedKeyUsage,
                false,
                ExtendedKeyUsage(
                    KeyPurposeId.id_kp_serverAuth
                )
            )

            Log.i(
                TAG,
                "Assinando certificado com $ALGORITMO_ASSINATURA"
            )

            val assinador =
                JcaContentSignerBuilder(
                    ALGORITMO_ASSINATURA
                )
                    .setProvider(provider)
                    .build(
                        parChaves.private
                    )

            val certificado =
                JcaX509CertificateConverter()
                    .setProvider(provider)
                    .getCertificate(
                        builder.build(
                            assinador
                        )
                    )

            certificado.checkValidity()
            certificado.verify(
                parChaves.public
            )

            val keyStore =
                KeyStore.getInstance(
                    "PKCS12"
                )

            keyStore.load(
                null,
                SENHA_KEYSTORE.toCharArray()
            )

            keyStore.setKeyEntry(
                ALIAS_CERTIFICADO,
                parChaves.private,
                SENHA_KEYSTORE.toCharArray(),
                arrayOf(certificado)
            )

            val dadosPkcs12 =
                serializarKeyStore(
                    keyStore
                )

            Log.i(
                TAG,
                "PKCS12 criado em memória. Criptografando com AES-GCM..."
            )

            salvarCriptografado(
                context,
                dadosPkcs12
            )

            dadosPkcs12.fill(0)

            Log.i(
                TAG,
                "Certificado HTTPS gerado e armazenado de forma protegida"
            )

        } catch (e: Exception) {
            Log.e(
                TAG,
                "Falha ao gerar certificado HTTPS",
                e
            )

            throw e
        }
    }

    private fun carregarCriptografado(
        context: Context
    ): KeyStore {

        val arquivo =
            obterArquivoCriptografado(
                context
            )

        Log.i(
            TAG,
            "Lendo certificado HTTPS criptografado"
        )

        val conteudo =
            arquivo.readBytes()

        if (conteudo.size <= TAMANHO_IV) {
            throw IllegalStateException(
                "Arquivo do certificado HTTPS protegido está inválido."
            )
        }

        val iv =
            conteudo.copyOfRange(
                0,
                TAMANHO_IV
            )

        val dadosCriptografados =
            conteudo.copyOfRange(
                TAMANHO_IV,
                conteudo.size
            )

        val chave =
            obterOuCriarChaveAes()

        val cipher =
            Cipher.getInstance(
                TRANSFORMACAO_AES
            )

        cipher.init(
            Cipher.DECRYPT_MODE,
            chave,
            GCMParameterSpec(
                128,
                iv
            )
        )

        val dadosPkcs12 =
            cipher.doFinal(
                dadosCriptografados
            )

        Log.i(
            TAG,
            "PKCS12 descriptografado em memória"
        )

        try {
            val keyStore =
                KeyStore.getInstance(
                    "PKCS12"
                )

            ByteArrayInputStream(
                dadosPkcs12
            ).use {
                keyStore.load(
                    it,
                    SENHA_KEYSTORE.toCharArray()
                )
            }

            Log.i(
                TAG,
                "PKCS12 carregado com sucesso"
            )

            Log.i(
                TAG,
                "Alias encontrado: ${
                    keyStore.containsAlias(
                        ALIAS_CERTIFICADO
                    )
                }"
            )

            return keyStore

        } finally {
            dadosPkcs12.fill(0)
        }
    }

    private fun salvarCriptografado(
        context: Context,
        dadosPkcs12: ByteArray
    ) {
        val chave =
            obterOuCriarChaveAes()

        val cipher =
            Cipher.getInstance(
                TRANSFORMACAO_AES
            )

        cipher.init(
            Cipher.ENCRYPT_MODE,
            chave
        )

        val iv =
            cipher.iv

        if (iv.size != TAMANHO_IV) {
            throw IllegalStateException(
                "IV AES-GCM inválido."
            )
        }

        val dadosCriptografados =
            cipher.doFinal(
                dadosPkcs12
            )

        val arquivo =
            obterArquivoCriptografado(
                context
            )

        arquivo.outputStream().use {
            it.write(iv)
            it.write(
                dadosCriptografados
            )
        }

        Log.i(
            TAG,
            "Arquivo HTTPS protegido salvo em ${arquivo.absolutePath}"
        )

        Log.i(
            TAG,
            "Tamanho do arquivo criptografado: ${arquivo.length()} bytes"
        )
    }

    private fun obterOuCriarChaveAes(): SecretKey {
        val androidKeyStore =
            KeyStore.getInstance(
                ANDROID_KEYSTORE
            )

        androidKeyStore.load(null)

        val chaveExistente =
            androidKeyStore.getKey(
                ALIAS_CHAVE_AES,
                null
            ) as? SecretKey

        if (chaveExistente != null) {
            Log.d(
                TAG,
                "Chave AES encontrada no Android Keystore"
            )

            return chaveExistente
        }

        Log.i(
            TAG,
            "Gerando chave AES no Android Keystore"
        )

        val gerador =
            KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                ANDROID_KEYSTORE
            )

        val especificacao =
            KeyGenParameterSpec.Builder(
                ALIAS_CHAVE_AES,
                KeyProperties.PURPOSE_ENCRYPT or
                        KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(
                    KeyProperties.BLOCK_MODE_GCM
                )
                .setEncryptionPaddings(
                    KeyProperties.ENCRYPTION_PADDING_NONE
                )
                .setKeySize(256)
                .setRandomizedEncryptionRequired(true)
                .build()

        gerador.init(
            especificacao
        )

        val chave =
            gerador.generateKey()

        Log.i(
            TAG,
            "Chave AES criada no Android Keystore"
        )

        return chave
    }

    private fun serializarKeyStore(
        keyStore: KeyStore
    ): ByteArray {

        val output =
            ByteArrayOutputStream()

        output.use {
            keyStore.store(
                it,
                SENHA_KEYSTORE.toCharArray()
            )

            return it.toByteArray()
        }
    }

    private fun migrarArquivoAntigoSeNecessario(
        context: Context
    ) {
        val antigo =
            obterArquivoAntigo(
                context
            )

        val novo =
            obterArquivoCriptografado(
                context
            )

        if (!antigo.exists() || novo.exists())
            return

        Log.w(
            TAG,
            "PKCS12 antigo encontrado. Iniciando migração para armazenamento protegido."
        )

        try {
            val keyStore =
                KeyStore.getInstance(
                    "PKCS12"
                )

            antigo.inputStream().use {
                keyStore.load(
                    it,
                    SENHA_KEYSTORE.toCharArray()
                )
            }

            if (!keyStore.containsAlias(
                    ALIAS_CERTIFICADO
                )
            ) {
                throw IllegalStateException(
                    "Alias do certificado não encontrado no PKCS12 antigo."
                )
            }

            val dadosPkcs12 =
                serializarKeyStore(
                    keyStore
                )

            try {
                salvarCriptografado(
                    context,
                    dadosPkcs12
                )
            } finally {
                dadosPkcs12.fill(0)
            }

            val removido =
                antigo.delete()

            if (!removido) {
                throw IllegalStateException(
                    "O PKCS12 antigo foi migrado, mas não pôde ser removido."
                )
            }

            Log.i(
                TAG,
                "Migração do PKCS12 concluída com sucesso"
            )

        } catch (e: Exception) {
            Log.e(
                TAG,
                "Falha ao migrar PKCS12 antigo",
                e
            )

            throw e
        }
    }

    private fun obterArquivoAntigo(
        context: Context
    ): File {
        return File(
            context.filesDir,
            NOME_ARQUIVO_ANTIGO
        )
    }

    private fun obterArquivoCriptografado(
        context: Context
    ): File {
        return File(
            context.filesDir,
            NOME_ARQUIVO_CRIPTOGRAFADO
        )
    }
}
