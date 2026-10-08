package br.com.uol.pagbank.plugpagservice.demo.estorno

enum class TipoEstorno {
    CARTAO,
    PIX
}

data class RequisicaoEstorno(
    val transactionCode: String,
    val transactionId: String,
    val tipo: TipoEstorno,
    val imprimir: Boolean = true
)
