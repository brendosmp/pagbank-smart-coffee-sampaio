package br.com.sampaiosi.softwareultimate.seguranca

import android.util.Log
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object AssinaturaTerminal {

    private const val TAG = "AssinaturaTerminal"

    fun gerar(
        terminalKey: String,
        terminalId: String,
        nonce: String,
        fingerprint: String
    ): String {
        try {
            Log.i(TAG, "Gerando assinatura HMAC-SHA256")
            Log.d(TAG, "Terminal ID: $terminalId")
            Log.d(TAG, "Nonce: $nonce")
            Log.d(TAG, "Fingerprint: $fingerprint")

            val conteudo =
                "$terminalId|$nonce|$fingerprint"

            Log.d(TAG, "Conteúdo assinado: $conteudo")

            val mac =
                Mac.getInstance("HmacSHA256")

            mac.init(
                SecretKeySpec(
                    terminalKey.toByteArray(Charsets.UTF_8),
                    "HmacSHA256"
                )
            )

            val assinatura =
                mac.doFinal(
                    conteudo.toByteArray(Charsets.UTF_8)
                )
                    .joinToString("") {
                        "%02X".format(it)
                    }

            Log.i(TAG, "Assinatura HMAC gerada com sucesso")
            Log.d(TAG, "Assinatura: $assinatura")

            return assinatura

        } catch (e: Exception) {
            Log.e(TAG, "Erro ao gerar assinatura HMAC", e)
            throw e
        }
    }
}
