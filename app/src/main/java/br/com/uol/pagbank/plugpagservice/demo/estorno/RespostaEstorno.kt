package br.com.uol.pagbank.plugpagservice.demo.estorno

import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagTransactionResult

data class RespostaEstorno(
    val resultado: PlugPagTransactionResult
)
