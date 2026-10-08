package br.com.uol.pagbank.plugpagservice.demo.terminal

import android.content.Context
import java.util.UUID
import androidx.core.content.edit

object IdentificadorTerminal  {
    private const val PREFERENCIAS = "terminal"
    private const val CHAVE_TERMINAL_ID = "terminal_id"

    fun existe(context: Context): Boolean {
        val preferencias = context.getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE)

        return !preferencias
            .getString(CHAVE_TERMINAL_ID, null)
            .isNullOrBlank()
    }

    fun obterOuCriar(context: Context): String {
        val preferencias = context.getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE)
        val terminalIdExistente = preferencias.getString(CHAVE_TERMINAL_ID, null)

        if (!terminalIdExistente.isNullOrBlank())
            return terminalIdExistente

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
}
