package br.com.uol.pagbank.plugpagservice.demo.terminal

import android.content.Context
import androidx.core.content.edit
import java.util.UUID

object IdentificadorTerminal {

    private const val PREFERENCIAS = "terminal"
    private const val CHAVE_TERMINAL_ID = "terminal_id"
    private const val CHAVE_TERMINAL_KEY = "terminal_key"
    private const val CHAVE_TERMINAL_ID_EXIBIDO = "terminal_id_exibido"

    fun obterOuCriarTerminalId(context: Context): String {
        val preferencias = context.getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE)
        val existente = preferencias.getString(CHAVE_TERMINAL_ID, null)

        if (!existente.isNullOrBlank())
            return existente

        val terminalId = UUID.randomUUID().toString()
            .replace("-", "")
            .uppercase()
            .take(12)

        preferencias.edit {
            putString(CHAVE_TERMINAL_ID, terminalId)
        }

        return terminalId
    }

    fun obterOuCriarTerminalKey(context: Context): String {
        val preferencias = context.getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE)
        val existente = preferencias.getString(CHAVE_TERMINAL_KEY, null)

        if (!existente.isNullOrBlank())
            return existente

        val terminalKey = UUID.randomUUID()
            .toString()
            .replace("-", "")
            .uppercase()
            .take(16)

        preferencias.edit {
            putString(CHAVE_TERMINAL_KEY, terminalKey)
        }

        return terminalKey
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
