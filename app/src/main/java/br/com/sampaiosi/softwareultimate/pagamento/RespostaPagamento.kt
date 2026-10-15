package br.com.sampaiosi.softwareultimate.pagamento

import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagTransactionResult

data class RespostaPagamento(
    val pedidoId: String,
    val resultado: PlugPagTransactionResult
)
