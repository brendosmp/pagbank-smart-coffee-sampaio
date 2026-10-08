package br.com.uol.pagbank.plugpagservice.demo.pagamento

import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagTransactionResult

data class RespostaPagamento(
    val pedidoId: String,
    val resultado: PlugPagTransactionResult
)
