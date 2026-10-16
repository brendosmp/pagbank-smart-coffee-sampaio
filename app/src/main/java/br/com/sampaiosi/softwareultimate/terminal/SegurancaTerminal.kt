package br.com.sampaiosi.softwareultimate.terminal

import android.content.Context
import android.util.Log
import java.security.MessageDigest

object SegurancaTerminal {

    private const val TAG = "SegurancaTerminal"

    private const val HEADER_TERMINAL_ID =
        "x-terminal-id"

    private const val HEADER_TERMINAL_KEY =
        "x-terminal-key"

    fun obterTerminalKey(
        context: Context
    ): String {
        return IdentificadorTerminal
            .obterOuCriarTerminalKey(context)
    }

    fun validar(
        context: Context,
        headers: Map<String, String>
    ) {
        val terminalIdEsperado =
            IdentificadorTerminal
                .obterOuCriarTerminalId(context)

        val terminalKeyEsperada =
            IdentificadorTerminal
                .obterOuCriarTerminalKey(context)

        val terminalIdRecebido =
            headers[HEADER_TERMINAL_ID]

        val terminalKeyRecebida =
            headers[HEADER_TERMINAL_KEY]

        if (terminalIdRecebido.isNullOrBlank()) {
            Log.w(
                TAG,
                "Terminal ID não informado"
            )

            throw TerminalNaoAutorizadoException(
                "A requisição do PDV não informou o Terminal ID."
            )
        }

        if (terminalKeyRecebida.isNullOrBlank()) {
            Log.w(
                TAG,
                "Terminal Key não informada"
            )

            throw TerminalNaoAutorizadoException(
                "A requisição do PDV não informou a Terminal Key."
            )
        }

        if (terminalIdRecebido != terminalIdEsperado) {
            Log.w(
                TAG,
                "Terminal ID não corresponde ao terminal atual. Recebido=$terminalIdRecebido"
            )

            throw TerminalNaoAutorizadoException(
                "O Terminal ID informado pelo PDV não corresponde a esta Moderninha."
            )
        }

        if (!compararSeguro(
                terminalKeyRecebida,
                terminalKeyEsperada
            )
        ) {
            Log.w(
                TAG,
                "Terminal Key inválida. TerminalId=$terminalIdRecebido"
            )

            throw TerminalNaoAutorizadoException(
                "A Terminal Key informada pelo PDV não corresponde a esta Moderninha."
            )
        }

        Log.i(
            TAG,
            "Credenciais válidas. TerminalId=$terminalIdRecebido"
        )
    }

    private fun compararSeguro(
        valorRecebido: String,
        valorEsperado: String
    ): Boolean {
        val recebido =
            valorRecebido.toByteArray(
                Charsets.UTF_8
            )

        val esperado =
            valorEsperado.toByteArray(
                Charsets.UTF_8
            )

        return MessageDigest.isEqual(
            recebido,
            esperado
        )
    }
}
