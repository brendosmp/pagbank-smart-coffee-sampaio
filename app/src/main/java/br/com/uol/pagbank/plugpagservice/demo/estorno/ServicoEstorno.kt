package br.com.uol.pagbank.plugpagservice.demo.estorno

import android.content.Context
import android.content.Intent
import android.util.Log
import br.com.uol.pagbank.plugpagservice.demo.ui.estorno.EstornoActivity
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPag
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagEventData
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagEventListener
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagVoidData

class ServicoEstorno(
    context: Context,
    private val plugPag: PlugPag
) {

    private val contexto = context.applicationContext

    @Synchronized
    fun executar(requisicao: RequisicaoEstorno): RespostaEstorno {
        validar(requisicao)

        EstadoEstorno.iniciar()
        abrirTelaEstorno()
        configurarEventos()

        val dadosEstorno = PlugPagVoidData(
            transactionCode = requisicao.transactionCode,
            transactionId = requisicao.transactionId,
            printReceipt = requisicao.imprimir,
            voidType = obterTipoEstorno(requisicao.tipo)
        )

        Log.i(
            "PlugPag",
            "Iniciando estorno. TransactionId=${requisicao.transactionId}"
        )

        val resultado = plugPag.voidPayment(dadosEstorno)

        Log.i(
            "PlugPag",
            "Estorno finalizado. Resultado=${resultado.result} Erro=${resultado.errorCode} Mensagem=${resultado.message}"
        )

        val sucesso = resultado.result == PlugPag.RET_OK

        if (sucesso) {
            EstadoEstorno.finalizar(
                sucesso = true,
                mensagem = "Estorno realizado com sucesso"
            )
        } else {
            EstadoEstorno.finalizar(
                sucesso = false,
                mensagem = resultado.message ?: "Não foi possível realizar o estorno"
            )
        }

        return RespostaEstorno(
            resultado = resultado
        )
    }

    private fun configurarEventos() {
        plugPag.setEventListener(object : PlugPagEventListener {

            override fun onEvent(data: PlugPagEventData) {
                Log.i(
                    "PlugPag",
                    "EventoEstorno=${data.eventCode} Mensagem=${data.customMessage}"
                )

                if (data.customMessage.isNotBlank())
                    EstadoEstorno.atualizarMensagem(data.customMessage)
            }
        })
    }

    private fun validar(requisicao: RequisicaoEstorno) {
        require(requisicao.transactionCode.isNotBlank()) {
            "TransactionCode não informado."
        }

        require(requisicao.transactionId.isNotBlank()) {
            "TransactionId não informado."
        }

        if (!plugPag.isAuthenticated())
            throw IllegalStateException("Terminal PagBank não está autenticado.")

        if (plugPag.isServiceBusy())
            throw IllegalStateException("Terminal PagBank está ocupado.")
    }

    private fun obterTipoEstorno(tipo: TipoEstorno): Int {
        return when (tipo) {
            TipoEstorno.CARTAO -> PlugPag.VOID_PAYMENT
            TipoEstorno.PIX -> PlugPag.VOID_QRCODE
        }
    }

    private fun abrirTelaEstorno() {
        val intent = Intent(contexto, EstornoActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        contexto.startActivity(intent)
    }
}
