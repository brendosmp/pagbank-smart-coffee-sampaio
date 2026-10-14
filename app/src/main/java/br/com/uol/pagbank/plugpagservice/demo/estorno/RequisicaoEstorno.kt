package br.com.uol.pagbank.plugpagservice.demo.estorno

data class RequisicaoEstorno(
    val transactionCode: String,
    val transactionId: String,
    val tipo: TipoEstorno,
    val valorCentavos: Int,
    val imprimir: Boolean = true
)
