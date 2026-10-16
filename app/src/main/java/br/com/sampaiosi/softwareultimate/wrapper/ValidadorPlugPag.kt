package br.com.sampaiosi.softwareultimate.wrapper

import br.com.sampaiosi.softwareultimate.comunicacao.TerminalNaoAutenticadoException
import br.com.sampaiosi.softwareultimate.comunicacao.TerminalOcupadoException
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPag

object ValidadorPlugPag {

    fun validarDisponibilidade(
        plugPag: PlugPag
    ) {
        if (!plugPag.isAuthenticated()) {
            throw TerminalNaoAutenticadoException()
        }

        if (plugPag.isServiceBusy()) {
            throw TerminalOcupadoException()
        }
    }
}
