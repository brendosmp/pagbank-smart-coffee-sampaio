package br.com.uol.pagbank.plugpagservice.demo.comunicacao

import android.content.Context
import android.util.Log
import br.com.uol.pagbank.plugpagservice.demo.estorno.RequisicaoEstorno
import br.com.uol.pagbank.plugpagservice.demo.estorno.ServicoEstorno
import br.com.uol.pagbank.plugpagservice.demo.pagamento.RequisicaoPagamento
import br.com.uol.pagbank.plugpagservice.demo.pagamento.ServicoPagamento
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPag
import com.google.gson.Gson
import fi.iki.elonen.NanoHTTPD

class ServidorHttpPagamento(
    context: Context,
    plugPag: PlugPag
) : NanoHTTPD("0.0.0.0", PORTA) {

    companion object {
        private const val PORTA = 9090
        private const val MIME_JSON = "application/json; charset=utf-8"
    }

    private val gson = Gson()

    private val servicoPagamento = ServicoPagamento(
        context.applicationContext,
        plugPag
    )

    private val servicoEstorno = ServicoEstorno(
        context.applicationContext,
        plugPag
    )

    private val bloqueioPlugPag = Any()

    override fun serve(session: IHTTPSession): Response {
        return try {
            when {
                session.method == Method.POST && session.uri == "/pagamentos" ->
                    receberPagamento(session)

                session.method == Method.POST && session.uri == "/estornos" ->
                    receberEstorno(session)

                else ->
                    respostaErro(
                        Response.Status.NOT_FOUND,
                        "Rota não encontrada."
                    )
            }
        } catch (e: Exception) {
            Log.e("ServidorHttp", "Erro ao processar requisição", e)

            respostaErro(
                Response.Status.INTERNAL_ERROR,
                e.message ?: "Erro interno."
            )
        }
    }

    private fun receberPagamento(session: IHTTPSession): Response {
        val json = obterCorpo(session)
            ?: return respostaErro(
                Response.Status.BAD_REQUEST,
                "Corpo da requisição não informado."
            )

        val requisicao = try {
            gson.fromJson(json, RequisicaoPagamento::class.java)
        } catch (e: Exception) {
            return respostaErro(
                Response.Status.BAD_REQUEST,
                "JSON de pagamento inválido."
            )
        }

        Log.i(
            "ServidorHttp",
            "Pagamento recebido. PedidoId=${requisicao.pedidoId}"
        )

        val resposta = synchronized(bloqueioPlugPag) {
            servicoPagamento.executar(requisicao)
        }

        val jsonResposta = gson.toJson(resposta)

        Log.i(
            "ServidorHttp",
            "Pagamento finalizado. PedidoId=${requisicao.pedidoId}"
        )

        return respostaOk(jsonResposta)
    }

    private fun receberEstorno(session: IHTTPSession): Response {
        val json = obterCorpo(session)
            ?: return respostaErro(
                Response.Status.BAD_REQUEST,
                "Corpo da requisição não informado."
            )

        val requisicao = try {
            gson.fromJson(json, RequisicaoEstorno::class.java)
        } catch (e: Exception) {
            return respostaErro(
                Response.Status.BAD_REQUEST,
                "JSON de estorno inválido."
            )
        }

        Log.i(
            "ServidorHttp",
            "Estorno recebido. TransactionId=${requisicao.transactionId}"
        )

        val resposta = synchronized(bloqueioPlugPag) {
            servicoEstorno.executar(requisicao)
        }

        val jsonResposta = gson.toJson(resposta)

        Log.i(
            "ServidorHttp",
            "Estorno finalizado. TransactionId=${requisicao.transactionId}"
        )

        return respostaOk(jsonResposta)
    }

    private fun obterCorpo(session: IHTTPSession): String? {
        val arquivos = HashMap<String, String>()

        session.parseBody(arquivos)

        return arquivos["postData"]
            ?.takeIf { it.isNotBlank() }
    }

    private fun respostaOk(json: String): Response {
        return newFixedLengthResponse(
            Response.Status.OK,
            MIME_JSON,
            json
        )
    }

    private fun respostaErro(
        status: Response.Status,
        mensagem: String
    ): Response {
        return newFixedLengthResponse(
            status,
            MIME_JSON,
            gson.toJson(
                mapOf("erro" to mensagem)
            )
        )
    }

    fun iniciarServidor() {
        start(SOCKET_READ_TIMEOUT, false)

        Log.i(
            "ServidorHttp",
            "Servidor HTTP iniciado na porta $PORTA"
        )
    }

    fun pararServidor() {
        stop()
    }
}
