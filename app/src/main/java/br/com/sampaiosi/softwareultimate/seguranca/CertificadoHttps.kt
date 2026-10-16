package br.com.sampaiosi.softwareultimate.seguranca

import android.content.Context
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
import java.io.File
import java.math.BigInteger
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.Date

object CertificadoHttps {

    private const val TAG = "CertificadoHttps"

    private const val NOME_ARQUIVO = "software_ultimate_https.p12"
    private const val ALIAS = "software_ultimate_https"
    private const val ALGORITMO_ASSINATURA = "SHA256withRSA"

    internal const val SENHA_KEYSTORE = "SoftwareUltimateHttps2026"

    private val provider = BouncyCastleProvider()

    fun obterKeyStore(context: Context): KeyStore {
        val arquivo = obterArquivo(context)

        Log.i(TAG, "Obtendo KeyStore HTTPS")
        Log.i(TAG, "Arquivo: ${arquivo.absolutePath}")
        Log.i(TAG, "Arquivo existe: ${arquivo.exists()}")

        if (!arquivo.exists()) {
            Log.i(TAG, "Certificado ainda não existe. Gerando novo certificado HTTPS...")
            gerar(context, arquivo)
        }

        return try {
            Log.i(TAG, "Carregando PKCS12...")
            val keyStore = carregar(arquivo)

            Log.i(TAG, "PKCS12 carregado com sucesso")
            Log.i(TAG, "Alias encontrado: ${keyStore.containsAlias(ALIAS)}")

            keyStore
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao carregar PKCS12. O arquivo será recriado.", e)

            if (arquivo.exists()) {
                val deletado = arquivo.delete()
                Log.w(TAG, "Arquivo PKCS12 removido: $deletado")
            }

            gerar(context, arquivo)

            Log.i(TAG, "Tentando carregar novamente o PKCS12 recriado")

            carregar(arquivo).also {
                Log.i(TAG, "PKCS12 recriado e carregado com sucesso")
            }
        }
    }

    fun obterCertificado(context: Context): X509Certificate {
        Log.i(TAG, "Obtendo certificado X509")

        val keyStore = obterKeyStore(context)

        val certificado = keyStore.getCertificate(ALIAS) as? X509Certificate
            ?: throw IllegalStateException("Não foi possível obter o certificado HTTPS.")

        Log.i(TAG, "Certificado obtido com sucesso")
        Log.i(TAG, "Subject: ${certificado.subjectX500Principal}")
        Log.i(TAG, "Validade inicial: ${certificado.notBefore}")
        Log.i(TAG, "Validade final: ${certificado.notAfter}")

        return certificado
    }

    private fun gerar(context: Context, arquivo: File) {
        try {
            Log.i(TAG, "Iniciando geração do certificado HTTPS")

            val terminalId = IdentificadorTerminal.obterOuCriarTerminalId(context)

            Log.i(TAG, "Terminal ID utilizado: $terminalId")
            Log.i(TAG, "Gerando chave RSA 2048 bits")

            val geradorChaves = KeyPairGenerator.getInstance("RSA")

            geradorChaves.initialize(2048, SecureRandom())

            val parChaves = geradorChaves.generateKeyPair()

            Log.i(TAG, "Par de chaves RSA gerado")

            val agora = System.currentTimeMillis()

            val inicio = Date(agora - 24L * 60L * 60L * 1000L)
            val fim = Date(agora + 3650L * 24L * 60L * 60L * 1000L)

            val serial = BigInteger(160, SecureRandom()).abs()

            val nome = X500Name("CN=$terminalId,O=Sampaio SI,C=BR")

            Log.i(TAG, "Criando certificado X509 para $nome")

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
                ExtendedKeyUsage(KeyPurposeId.id_kp_serverAuth)
            )

            Log.i(TAG, "Assinando certificado com $ALGORITMO_ASSINATURA")

            val assinador =
                JcaContentSignerBuilder(ALGORITMO_ASSINATURA)
                    .setProvider(provider)
                    .build(parChaves.private)

            val certificado =
                JcaX509CertificateConverter()
                    .setProvider(provider)
                    .getCertificate(builder.build(assinador))

            certificado.checkValidity()
            certificado.verify(parChaves.public)

            Log.i(TAG, "Certificado criado e validado")

            val keyStore = KeyStore.getInstance("PKCS12")

            keyStore.load(
                null,
                SENHA_KEYSTORE.toCharArray()
            )

            keyStore.setKeyEntry(
                ALIAS,
                parChaves.private,
                SENHA_KEYSTORE.toCharArray(),
                arrayOf(certificado)
            )

            Log.i(TAG, "Salvando PKCS12 em ${arquivo.absolutePath}")

            arquivo.outputStream().use {
                keyStore.store(
                    it,
                    SENHA_KEYSTORE.toCharArray()
                )
            }

            Log.i(TAG, "PKCS12 salvo com sucesso")
            Log.i(TAG, "Tamanho do arquivo: ${arquivo.length()} bytes")

        } catch (e: Exception) {
            Log.e(TAG, "Falha ao gerar certificado HTTPS", e)
            throw e
        }
    }

    private fun carregar(arquivo: File): KeyStore {
        Log.d(TAG, "Abrindo arquivo PKCS12")

        val keyStore = KeyStore.getInstance("PKCS12")

        arquivo.inputStream().use {
            keyStore.load(
                it,
                SENHA_KEYSTORE.toCharArray()
            )
        }

        Log.d(TAG, "Arquivo PKCS12 lido com sucesso")

        return keyStore
    }

    private fun obterArquivo(context: Context): File {
        return File(
            context.filesDir,
            NOME_ARQUIVO
        )
    }
}
