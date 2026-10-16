package br.com.sampaiosi.softwareultimate.pagamento

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import br.com.sampaiosi.softwareultimate.ui.pagamento.PagamentoActivity
import br.com.sampaiosi.softwareultimate.wrapper.ValidadorPlugPag
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPag
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagEventData
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagEventListener
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagPaymentData
import pagamento.RequisicaoPagamento

class ServicoPagamento(
    context: Context,
    private val plugPag: PlugPag
) {

    companion object {
        private const val TAG = "PlugPag"
    }

    private val contexto = context.applicationContext

    fun executar(requisicao: RequisicaoPagamento): RespostaPagamento {
        validar(requisicao)

        EstadoPagamento.iniciar(requisicao)

        abrirTelaPagamento()
        configurarEventos()

        val pagamento = PlugPagPaymentData(
            type = obterTipoPagamento(requisicao.tipo),
            amount = requisicao.valorCentavos,
            installmentType = obterTipoParcelamento(requisicao.parcelamento),
            installments = requisicao.parcelas,
            userReference = obterReferencia(requisicao.pedidoId),
            printReceipt = true,
            partialPay = false,
            isCarne = false
        )

        Log.i(
            TAG,
            "Iniciando pagamento. PedidoId=${requisicao.pedidoId}"
        )

        try {
            val resultado = plugPag.doPayment(pagamento)

            Log.i(
                TAG,
                "Pagamento finalizado. Resultado=${resultado.result} Erro=${resultado.errorCode} Mensagem=${resultado.message}"
            )

            when (resultado.result) {
                PlugPag.OPERATION_ABORTED ->
                    EstadoPagamento.cancelar()

                PlugPag.RET_OK ->
                    EstadoPagamento.finalizar(
                        true,
                        "Pagamento aprovado"
                    )

                else ->
                    EstadoPagamento.finalizar(
                        false,
                        resultado.message ?: "Pagamento não autorizado"
                    )
            }

            return RespostaPagamento(
                requisicao.pedidoId,
                resultado
            )
        } catch (e: Exception) {
            Log.e(
                TAG,
                "Erro inesperado durante pagamento. PedidoId=${requisicao.pedidoId}",
                e
            )

            EstadoPagamento.finalizar(
                false,
                "Erro ao processar pagamento"
            )

            throw e
        }
    }

    private fun validar(requisicao: RequisicaoPagamento) {
        require(requisicao.pedidoId.isNotBlank()) {
            "PedidoId não informado."
        }

        require(requisicao.valorCentavos > 0) {
            "Valor do pagamento deve ser maior que zero."
        }

        require(requisicao.parcelas > 0) {
            "Quantidade de parcelas inválida."
        }

        ValidadorPlugPag.validarDisponibilidade(
            plugPag
        )
    }

    private fun configurarEventos() {
        plugPag.setEventListener(
            object : PlugPagEventListener {

                override fun onEvent(data: PlugPagEventData) {
                    Log.i(
                        TAG,
                        "EventoPagamento=${data.eventCode} Mensagem=${data.customMessage}"
                    )

                    if (data.customMessage.isNotBlank()) {
                        EstadoPagamento.atualizarMensagem(
                            data.customMessage
                        )
                    }
                }
            }
        )
    }

    private fun obterTipoPagamento(
        tipo: TipoPagamento
    ): Int {
        return when (tipo) {
            TipoPagamento.CREDITO ->
                PlugPag.TYPE_CREDITO

            TipoPagamento.DEBITO ->
                PlugPag.TYPE_DEBITO

            TipoPagamento.PIX ->
                PlugPag.TYPE_PIX
        }
    }

    private fun obterTipoParcelamento(
        tipo: TipoParcelamento
    ): Int {
        return when (tipo) {
            TipoParcelamento.A_VISTA ->
                PlugPag.INSTALLMENT_TYPE_A_VISTA

            TipoParcelamento.PARC_VENDEDOR ->
                PlugPag.INSTALLMENT_TYPE_PARC_VENDEDOR

            TipoParcelamento.PARC_COMPRADOR ->
                PlugPag.INSTALLMENT_TYPE_PARC_COMPRADOR
        }
    }

    private fun obterReferencia(
        pedidoId: String
    ): String {
        return pedidoId
            .filter { it.isLetterOrDigit() }
            .take(10)
    }

    private fun abrirTelaPagamento() {
        Handler(Looper.getMainLooper()).post {
            val intent = Intent(
                contexto,
                PagamentoActivity::class.java
            ).apply {
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP
                )
            }

            contexto.startActivity(intent)
        }
    }
}
