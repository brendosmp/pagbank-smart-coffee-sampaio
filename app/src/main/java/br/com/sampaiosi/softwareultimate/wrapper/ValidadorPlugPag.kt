package br.com.sampaiosi.softwareultimate.wrapper

import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPag

object ValidadorPlugPag {

    fun validarDisponibilidade(plugPag: PlugPag) {
        if (!plugPag.isAuthenticated())
            throw _root_ide_package_.br.com.sampaiosi.softwareultimate.comunicacao.TerminalNaoAutenticadoException()

        if (plugPag.isServiceBusy())
            throw _root_ide_package_.br.com.sampaiosi.softwareultimate.comunicacao.TerminalOcupadoException()
    }
}
