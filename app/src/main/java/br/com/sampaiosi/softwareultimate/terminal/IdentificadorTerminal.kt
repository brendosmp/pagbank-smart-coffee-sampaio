package br.com.sampaiosi.softwareultimate.terminal

import android.content.Context
import android.util.Base64
import androidx.core.content.edit
import java.security.SecureRandom

object IdentificadorTerminal {
    private const val PREFERENCIAS = "terminal"
    private const val CHAVE_TERMINAL_ID = "terminal_id"
    private const val CHAVE_TERMINAL_KEY_CRIPTOGRAFADA = "terminal_key_criptografada"
    private const val CHAVE_TERMINAL_KEY_IV = "terminal_key_iv"
    private const val CHAVE_TERMINAL_ID_EXIBIDO = "terminal_id_exibido"

    fun obterOuCriarTerminalId(context: Context): String {
        val preferencias = context.getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE)
        val existente = preferencias.getString(CHAVE_TERMINAL_ID, null)

        if (!existente.isNullOrBlank())
            return existente

        val terminalId = gerarCodigo(8)

        preferencias.edit {
            putString(CHAVE_TERMINAL_ID, terminalId)
        }

        return terminalId
    }

    fun obterOuCriarTerminalKey(context: Context): String {
        val preferencias = context.getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE)

        val chaveCriptografada = preferencias.getString(CHAVE_TERMINAL_KEY_CRIPTOGRAFADA, null)
        val iv = preferencias.getString(CHAVE_TERMINAL_KEY_IV, null)

        if (!chaveCriptografada.isNullOrBlank() && !iv.isNullOrBlank()) {
            return SegurancaKeystore.descriptografar(
                Base64.decode(chaveCriptografada, Base64.NO_WRAP),
                Base64.decode(iv, Base64.NO_WRAP)
            )
        }

        val terminalKey = gerarCodigo(8)

        val resultado = SegurancaKeystore.criptografar(terminalKey)

        preferencias.edit {
            putString(
                CHAVE_TERMINAL_KEY_CRIPTOGRAFADA,
                Base64.encodeToString(resultado.dados, Base64.NO_WRAP)
            )

            putString(
                CHAVE_TERMINAL_KEY_IV,
                Base64.encodeToString(resultado.iv, Base64.NO_WRAP)
            )
        }

        return terminalKey
    }

    private fun gerarCodigo(tamanho: Int): String {
        val caracteres = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        val random = SecureRandom()

        return buildString {
            repeat(tamanho) {
                append(caracteres[random.nextInt(caracteres.length)])
            }
        }
    }

    fun deveMostrarPrimeiroAcesso(context: Context, terminalId: String): Boolean {
        val preferencias = context.getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE)
        return preferencias.getString(CHAVE_TERMINAL_ID_EXIBIDO, null) != terminalId
    }

    fun marcarComoExibido(context: Context, terminalId: String) {
        context.getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE).edit {
            putString(CHAVE_TERMINAL_ID_EXIBIDO, terminalId)
        }
    }
}
