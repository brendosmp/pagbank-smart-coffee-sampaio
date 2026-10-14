package br.com.uol.pagbank.plugpagservice.demo.terminal

import android.content.Context

object SegurancaTerminal {

    private const val HEADER_TERMINAL_ID = "x-terminal-id"
    private const val HEADER_TERMINAL_KEY = "x-terminal-key"

    fun validar(context: Context, headers: Map<String, String>) {
        val terminalIdEsperado = IdentificadorTerminal.obterOuCriarTerminalId(context)
        val terminalKeyEsperada = IdentificadorTerminal.obterOuCriarTerminalKey(context)

        val terminalIdRecebido = headers[HEADER_TERMINAL_ID]
        val terminalKeyRecebida = headers[HEADER_TERMINAL_KEY]

        if (terminalIdRecebido.isNullOrBlank())
            throw TerminalNaoAutorizadoException("Terminal ID não informado.")

        if (terminalKeyRecebida.isNullOrBlank())
            throw TerminalNaoAutorizadoException("Terminal Key não informada.")

        if (terminalIdRecebido != terminalIdEsperado || terminalKeyRecebida != terminalKeyEsperada)
            throw TerminalNaoAutorizadoException("Credenciais do terminal inválidas.")
    }
}
