package br.com.sampaiosi.softwareultimate.seguranca

import android.content.Context
import android.util.Log
import java.security.MessageDigest

object FingerprintCertificado {

    private const val TAG = "FingerprintCertificado"

    fun obterSha256(context: Context): String {
        try {
            Log.i(TAG, "Calculando fingerprint SHA-256")

            val certificado =
                CertificadoHttps.obterCertificado(context)

            val hash =
                MessageDigest
                    .getInstance("SHA-256")
                    .digest(certificado.encoded)

            val fingerprint =
                hash.joinToString("") {
                    "%02X".format(it)
                }

            Log.i(TAG, "Fingerprint SHA-256 calculado")
            Log.i(TAG, "Fingerprint: $fingerprint")

            return fingerprint

        } catch (e: Exception) {
            Log.e(TAG, "Erro ao calcular fingerprint", e)
            throw e
        }
    }
}
