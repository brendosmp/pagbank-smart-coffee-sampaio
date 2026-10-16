package br.com.sampaiosi.softwareultimate.seguranca

import android.content.Context
import android.util.Log
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLServerSocketFactory

object ConfiguracaoHttps {

    private const val TAG = "ConfiguracaoHttps"

    fun criarServerSocketFactory(context: Context): SSLServerSocketFactory {
        try {
            Log.i(TAG, "Iniciando configuração HTTPS")

            val keyStore = CertificadoHttps.obterKeyStore(context)

            Log.i(TAG, "KeyStore obtido")

            val algoritmo = KeyManagerFactory.getDefaultAlgorithm()

            Log.i(TAG, "KeyManagerFactory: $algoritmo")

            val keyManagerFactory =
                KeyManagerFactory.getInstance(algoritmo)

            keyManagerFactory.init(
                keyStore,
                CertificadoHttps.SENHA_KEYSTORE.toCharArray()
            )

            Log.i(TAG, "KeyManagerFactory inicializado")
            Log.i(TAG, "Criando SSLContext TLSv1.2")

            val sslContext =
                SSLContext.getInstance("TLSv1.2")

            sslContext.init(
                keyManagerFactory.keyManagers,
                null,
                null
            )

            Log.i(TAG, "SSLContext TLSv1.2 inicializado com sucesso")

            val factory = sslContext.serverSocketFactory

            Log.i(TAG, "SSLServerSocketFactory criada")

            return factory

        } catch (e: Exception) {
            Log.e(TAG, "Erro ao configurar HTTPS", e)
            throw e
        }
    }
}
