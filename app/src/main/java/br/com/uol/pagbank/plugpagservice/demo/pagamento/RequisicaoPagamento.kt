package br.com.uol.pagbank.plugpagservice.demo.pagamento

data class RequisicaoPagamento(
    val pedidoId: String,
    val valorCentavos: Int,
    val tipo: TipoPagamento,
    val parcelamento: TipoParcelamento = TipoParcelamento.A_VISTA,
    val parcelas: Int = 1,
    val imprimir: Boolean = true
)
