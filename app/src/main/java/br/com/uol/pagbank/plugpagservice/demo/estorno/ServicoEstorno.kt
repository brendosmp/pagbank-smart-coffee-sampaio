package br.com.uol.pagbank.plugpagservice.demo.estorno

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import br.com.uol.pagbank.plugpagservice.demo.ui.estorno.EstornoActivity
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPag
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagEventData
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagEventListener
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagVoidData

class ServicoEstorno(context: Context, private val plugPag: PlugPag) {

    private val context = context.applicationContext

    fun executar(requisicao: RequisicaoEstorno): RespostaEstorno {
        validar(requisicao)

        EstadoEstorno.iniciar(requisicao)
        abrirTelaEstorno()
        configurarEventos()

        val voidType = when (requisicao.tipo) {
            TipoEstorno.CREDITO,
            TipoEstorno.DEBITO -> PlugPag.VOID_PAYMENT

            TipoEstorno.PIX -> PlugPag.VOID_QRCODE
        }

        val dadosEstorno = PlugPagVoidData(
            transactionCode = requisicao.transactionCode,
            transactionId = requisicao.transactionId,
            printReceipt = requisicao.imprimir,
            voidType = voidType
        )

        Log.i(
            "PlugPag",
            "Estorno: transactionCode=${requisicao.transactionCode}, transactionId=${requisicao.transactionId}, tipo=${requisicao.tipo}"
        )
        Log.i("PlugPag", "Chamando voidPayment...")

        val resultado = plugPag.voidPayment(dadosEstorno)

        Log.i(
            "PlugPag",
            "Estorno finalizado. Resultado=${resultado.result} Erro=${resultado.errorCode} Mensagem=${resultado.message}"
        )

        when {
            resultado.result == PlugPag.OPERATION_ABORTED -> EstadoEstorno.cancelar()

            resultado.result == PlugPag.RET_OK ->
                EstadoEstorno.finalizar(true, "Estorno realizado com sucesso")

            else ->
                EstadoEstorno.finalizar(
                    false,
                    resultado.message ?: "Não foi possível realizar o estorno"
                )
        }

        return RespostaEstorno(resultado)
    }

    private fun validar(requisicao: RequisicaoEstorno) {
        if (requisicao.transactionCode.isBlank())
            throw IllegalArgumentException("TransactionCode não informado.")

        if (requisicao.transactionId.isBlank())
            throw IllegalArgumentException("TransactionId não informado.")

        if (!plugPag.isAuthenticated())
            throw IllegalStateException("Moderninha não ativada.")

        if (plugPag.isServiceBusy())
            throw IllegalStateException("PlugPag ocupado.")
    }

    private fun abrirTelaEstorno() {
        Handler(Looper.getMainLooper()).post {
            val intent = Intent(context, EstornoActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }

            context.startActivity(intent)
        }
    }

    private fun configurarEventos() {
        plugPag.setEventListener(object : PlugPagEventListener {
            override fun onEvent(data: PlugPagEventData) {
                Log.i("PlugPag", "EventoEstorno=${data.eventCode} Mensagem=${data.customMessage}")

                if (!data.customMessage.isNullOrBlank())
                    EstadoEstorno.atualizarMensagem(data.customMessage)
            }
        })
    }
}
