package br.com.uol.pagbank.plugpagservice.demo.wrapper

import br.com.uol.pagbank.plugpagservice.demo.comunicacao.TerminalNaoAutenticadoException
import br.com.uol.pagbank.plugpagservice.demo.comunicacao.TerminalOcupadoException
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPag

object ValidadorPlugPag {

    fun validarDisponibilidade(plugPag: PlugPag) {
        if (!plugPag.isAuthenticated())
            throw TerminalNaoAutenticadoException()

        if (plugPag.isServiceBusy())
            throw TerminalOcupadoException()
    }
}
