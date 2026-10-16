package br.com.sampaiosi.softwareultimate.seguranca

import android.content.Context
import android.util.Log
import java.security.MessageDigest

object FingerprintCertificado {

    private const val TAG = "FingerprintCertificado"

    fun obterSha256(context: Context): String {
        val certificado = CertificadoHttps.obterCertificado(context)

        val hash = MessageDigest.getInstance("SHA-256")
            .digest(certificado.encoded)

        val fingerprint = hash.joinToString("") { "%02X".format(it) }

        Log.i(TAG, "Subject certificado local: ${certificado.subjectX500Principal}")
        Log.i(TAG, "Serial certificado local: ${certificado.serialNumber}")
        Log.i(TAG, "Fingerprint LOCAL: $fingerprint")

        return fingerprint
    }
}
