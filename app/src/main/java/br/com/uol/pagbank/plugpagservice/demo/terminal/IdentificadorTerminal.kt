package br.com.uol.pagbank.plugpagservice.demo.terminal

import android.content.Context
import androidx.core.content.edit
import java.util.UUID

object IdentificadorTerminal {
    private const val PREFERENCIAS = "terminal"
    private const val CHAVE_TERMINAL_ID = "terminal_id"
    private const val CHAVE_TERMINAL_ID_EXIBIDO = "terminal_id_exibido"

    fun obterOuCriar(context: Context): String {
        val preferencias = context.getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE)
        val existente = preferencias.getString(CHAVE_TERMINAL_ID, null)

        if (!existente.isNullOrBlank())
            return existente

        val terminalId = UUID.randomUUID()
            .toString()
            .replace("-", "")
            .uppercase()
            .take(12)

        preferencias.edit {
            putString(CHAVE_TERMINAL_ID, terminalId)
        }

        return terminalId
    }

    fun deveMostrarPrimeiroAcesso(context: Context, terminalId: String): Boolean {
        val preferencias = context.getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE)
        val terminalIdExibido = preferencias.getString(CHAVE_TERMINAL_ID_EXIBIDO, null)

        return terminalIdExibido != terminalId
    }

    fun marcarComoExibido(context: Context, terminalId: String) {
        context.getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE).edit {
            putString(CHAVE_TERMINAL_ID_EXIBIDO, terminalId)
        }
    }
}
