package br.com.sampaiosi.softwareultimate.seguranca

import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLServerSocketFactory

object ConfiguracaoHttps {
    fun criarServerSocketFactory(): SSLServerSocketFactory {
        val keyStore = CertificadoHttps.obterKeyStore()

        val keyManagerFactory = KeyManagerFactory.getInstance(
            KeyManagerFactory.getDefaultAlgorithm()
        )

        keyManagerFactory.init(keyStore, null)

        val sslContext = SSLContext.getInstance("TLSv1.2")

        sslContext.init(
            keyManagerFactory.keyManagers,
            null,
            null
        )

        return sslContext.serverSocketFactory
    }
}
