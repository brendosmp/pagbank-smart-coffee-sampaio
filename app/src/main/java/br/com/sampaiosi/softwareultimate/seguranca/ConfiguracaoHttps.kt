package br.com.sampaiosi.softwareultimate.seguranca

import android.content.Context
import android.util.Log
import java.net.InetAddress
import java.net.ServerSocket
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLServerSocket
import javax.net.ssl.SSLServerSocketFactory

object ConfiguracaoHttps {

    private const val TAG = "ConfiguracaoHttps"

    private val PROTOCOLOS_PERMITIDOS =
        arrayOf(
            "TLSv1.2"
        )

    private val CIPHERS_DESEJADOS =
        arrayOf(
            "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
            "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384"
        )

    fun criarServerSocketFactory(
        context: Context
    ): SSLServerSocketFactory {
        try {
            Log.i(
                TAG,
                "Iniciando configuração HTTPS"
            )

            val keyStore =
                CertificadoHttps.obterKeyStore(
                    context
                )

            Log.i(
                TAG,
                "KeyStore HTTPS obtido"
            )

            val algoritmo =
                KeyManagerFactory
                    .getDefaultAlgorithm()

            Log.i(
                TAG,
                "KeyManagerFactory: $algoritmo"
            )

            val keyManagerFactory =
                KeyManagerFactory.getInstance(
                    algoritmo
                )

            keyManagerFactory.init(
                keyStore,
                CertificadoHttps
                    .SENHA_KEYSTORE
                    .toCharArray()
            )

            Log.i(
                TAG,
                "KeyManagerFactory inicializado"
            )

            val sslContext =
                SSLContext.getInstance(
                    "TLSv1.2"
                )

            sslContext.init(
                keyManagerFactory.keyManagers,
                null,
                null
            )

            Log.i(
                TAG,
                "SSLContext TLSv1.2 inicializado"
            )

            val factoryOriginal =
                sslContext.serverSocketFactory

            val factorySegura =
                ServerSocketFactorySegura(
                    factoryOriginal
                )

            Log.i(
                TAG,
                "SSLServerSocketFactory segura criada"
            )

            return factorySegura

        } catch (e: Exception) {
            Log.e(
                TAG,
                "Erro ao configurar HTTPS",
                e
            )

            throw e
        }
    }

    private class ServerSocketFactorySegura(
        private val factory: SSLServerSocketFactory
    ) : SSLServerSocketFactory() {

        override fun getDefaultCipherSuites(): Array<String> {
            return obterCiphersPermitidos(
                factory.defaultCipherSuites
            )
        }

        override fun getSupportedCipherSuites(): Array<String> {
            return obterCiphersPermitidos(
                factory.supportedCipherSuites
            )
        }

        override fun createServerSocket(): ServerSocket {
            return configurar(
                factory.createServerSocket()
            )
        }

        override fun createServerSocket(
            port: Int
        ): ServerSocket {
            return configurar(
                factory.createServerSocket(
                    port
                )
            )
        }

        override fun createServerSocket(
            port: Int,
            backlog: Int
        ): ServerSocket {
            return configurar(
                factory.createServerSocket(
                    port,
                    backlog
                )
            )
        }

        override fun createServerSocket(
            port: Int,
            backlog: Int,
            ifAddress: InetAddress?
        ): ServerSocket {
            return configurar(
                factory.createServerSocket(
                    port,
                    backlog,
                    ifAddress
                )
            )
        }

        private fun configurar(
            socket: ServerSocket
        ): ServerSocket {

            val sslSocket =
                socket as? SSLServerSocket
                    ?: throw IllegalStateException(
                        "O socket criado não é SSLServerSocket."
                    )

            sslSocket.enabledProtocols =
                PROTOCOLOS_PERMITIDOS

            val ciphersPermitidos =
                obterCiphersPermitidos(
                    sslSocket.supportedCipherSuites
                )

            if (ciphersPermitidos.isEmpty()) {
                Log.e(
                    TAG,
                    "Nenhuma cipher suite TLS segura suportada pelo dispositivo."
                )

                Log.e(
                    TAG,
                    "Cipher suites suportadas: ${
                        sslSocket.supportedCipherSuites.joinToString()
                    }"
                )

                throw IllegalStateException(
                    "Nenhuma cipher suite TLS segura suportada pelo dispositivo."
                )
            }

            sslSocket.enabledCipherSuites =
                ciphersPermitidos

            Log.i(
                TAG,
                "Protocolos TLS habilitados: ${
                    sslSocket.enabledProtocols.joinToString()
                }"
            )

            Log.i(
                TAG,
                "Cipher suites habilitadas: ${
                    sslSocket.enabledCipherSuites.joinToString()
                }"
            )

            return sslSocket
        }

        private fun obterCiphersPermitidos(
            suportados: Array<String>
        ): Array<String> {

            return CIPHERS_DESEJADOS
                .filter {
                    suportados.contains(it)
                }
                .toTypedArray()
        }
    }
}
