package br.com.sampaiosi.softwareultimate.estorno

data class RequisicaoEstorno(
    val transactionCode: String,
    val transactionId: String,
    val tipo: TipoEstorno,
    val valorCentavos: Int,
    val imprimir: Boolean = true
)
