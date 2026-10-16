package br.com.sampaiosi.softwareultimate.estorno

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import br.com.sampaiosi.softwareultimate.ui.estorno.EstornoActivity
import br.com.sampaiosi.softwareultimate.wrapper.ValidadorPlugPag
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPag
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagEventData
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagEventListener
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagVoidData

class ServicoEstorno(
    context: Context,
    private val plugPag: PlugPag
) {

    companion object {
        private const val TAG = "PlugPag"
    }

    private val contexto = context.applicationContext

    fun executar(requisicao: RequisicaoEstorno): RespostaEstorno {
        validar(requisicao)

        EstadoEstorno.iniciar(requisicao)

        abrirTelaEstorno()
        configurarEventos()

        val voidType = when (requisicao.tipo) {
            TipoEstorno.CREDITO,
            TipoEstorno.DEBITO ->
                PlugPag.VOID_PAYMENT

            TipoEstorno.PIX ->
                PlugPag.VOID_QRCODE
        }

        val dadosEstorno = PlugPagVoidData(
            transactionCode = requisicao.transactionCode,
            transactionId = requisicao.transactionId,
            printReceipt = requisicao.imprimir,
            voidType = voidType
        )

        Log.i(
            TAG,
            "Iniciando estorno. TransactionCode=${requisicao.transactionCode} TransactionId=${requisicao.transactionId} Tipo=${requisicao.tipo}"
        )

        try {
            val resultado =
                plugPag.voidPayment(dadosEstorno)

            Log.i(
                TAG,
                "Estorno finalizado. Resultado=${resultado.result} Erro=${resultado.errorCode} Mensagem=${resultado.message}"
            )

            when (resultado.result) {
                PlugPag.OPERATION_ABORTED ->
                    EstadoEstorno.cancelar()

                PlugPag.RET_OK ->
                    EstadoEstorno.finalizar(
                        true,
                        "Estorno realizado com sucesso"
                    )

                else ->
                    EstadoEstorno.finalizar(
                        false,
                        resultado.message
                            ?: "Não foi possível realizar o estorno"
                    )
            }

            return RespostaEstorno(
                resultado
            )
        } catch (e: Exception) {
            Log.e(
                TAG,
                "Erro inesperado durante estorno. TransactionId=${requisicao.transactionId}",
                e
            )

            EstadoEstorno.finalizar(
                false,
                "Erro ao processar estorno"
            )

            throw e
        }
    }

    private fun validar(
        requisicao: RequisicaoEstorno
    ) {
        require(requisicao.transactionCode.isNotBlank()) {
            "TransactionCode não informado."
        }

        require(requisicao.transactionId.isNotBlank()) {
            "TransactionId não informado."
        }

        ValidadorPlugPag.validarDisponibilidade(
            plugPag
        )
    }

    private fun abrirTelaEstorno() {
        Handler(Looper.getMainLooper()).post {
            val intent = Intent(
                contexto,
                EstornoActivity::class.java
            ).apply {
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP
                )
            }

            contexto.startActivity(intent)
        }
    }

    private fun configurarEventos() {
        plugPag.setEventListener(
            object : PlugPagEventListener {

                override fun onEvent(data: PlugPagEventData) {
                    Log.i(
                        TAG,
                        "EventoEstorno=${data.eventCode} Mensagem=${data.customMessage}"
                    )

                    if (data.customMessage.isNotBlank()) {
                        EstadoEstorno.atualizarMensagem(
                            data.customMessage
                        )
                    }
                }
            }
        )
    }
}
