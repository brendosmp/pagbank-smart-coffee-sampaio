package br.com.sampaiosi.softwareultimate.terminal

import android.content.Context

object SegurancaTerminal {

    private const val HEADER_TERMINAL_ID = "x-terminal-id"
    private const val HEADER_TERMINAL_KEY = "x-terminal-key"

    fun validar(context: Context, headers: Map<String, String>) {
        val terminalIdEsperado =
            IdentificadorTerminal.obterOuCriarTerminalId(
                context
            )
        val terminalKeyEsperada =
            IdentificadorTerminal.obterOuCriarTerminalKey(
                context
            )

        val terminalIdRecebido = headers[HEADER_TERMINAL_ID]
        val terminalKeyRecebida = headers[HEADER_TERMINAL_KEY]

        if (terminalIdRecebido.isNullOrBlank())
            throw TerminalNaoAutorizadoException(
                "A requisição do PDV não informou o Terminal ID."
            )

        if (terminalKeyRecebida.isNullOrBlank())
            throw TerminalNaoAutorizadoException(
                "A requisição do PDV não informou a Terminal Key."
            )

        if (terminalIdRecebido != terminalIdEsperado)
            throw TerminalNaoAutorizadoException(
                "O Terminal ID informado pelo PDV não corresponde a esta Moderninha."
            )

        if (terminalKeyRecebida != terminalKeyEsperada)
            throw TerminalNaoAutorizadoException(
                "A Terminal Key informada pelo PDV não corresponde a esta Moderninha."
            )
    }
}
