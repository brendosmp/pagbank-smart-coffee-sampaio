package br.com.sampaiosi.softwareultimate.estorno

import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagTransactionResult

data class RespostaEstorno(
    val resultado: PlugPagTransactionResult
)
