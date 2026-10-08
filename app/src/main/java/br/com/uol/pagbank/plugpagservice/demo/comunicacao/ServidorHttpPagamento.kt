package br.com.uol.pagbank.plugpagservice.demo.comunicacao

import android.content.Context
import android.util.Log
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

    override fun serve(session: IHTTPSession): Response {
        return try {
            when {
                session.method == Method.POST && session.uri == "/pagamentos" ->
                    receberPagamento(session)

                else ->
                    newFixedLengthResponse(
                        Response.Status.NOT_FOUND,
                        MIME_JSON,
                        gson.toJson(
                            mapOf("erro" to "Rota não encontrada.")
                        )
                    )
            }
        } catch (e: Exception) {
            Log.e("ServidorHttpPagamento", "Erro na requisição", e)

            newFixedLengthResponse(
                Response.Status.INTERNAL_ERROR,
                MIME_JSON,
                gson.toJson(
                    mapOf(
                        "erro" to (e.message ?: "Erro interno.")
                    )
                )
            )
        }
    }

    private fun receberPagamento(session: IHTTPSession): Response {
        val arquivos = HashMap<String, String>()

        session.parseBody(arquivos)

        val json = arquivos["postData"]

        if (json.isNullOrBlank()) {
            return newFixedLengthResponse(
                Response.Status.BAD_REQUEST,
                MIME_JSON,
                gson.toJson(
                    mapOf("erro" to "Corpo da requisição não informado.")
                )
            )
        }

        Log.i("ServidorHttpPagamento", "Pagamento recebido: $json")

        val requisicao = try {
            gson.fromJson(json, RequisicaoPagamento::class.java)
        } catch (e: Exception) {
            return newFixedLengthResponse(
                Response.Status.BAD_REQUEST,
                MIME_JSON,
                gson.toJson(
                    mapOf("erro" to "JSON de pagamento inválido.")
                )
            )
        }

        val resposta = servicoPagamento.executar(requisicao)

        val jsonResposta = gson.toJson(resposta)

        Log.i("ServidorHttpPagamento", "Pagamento finalizado: $jsonResposta")

        return newFixedLengthResponse(
            Response.Status.OK,
            MIME_JSON,
            jsonResposta
        )
    }

    fun iniciarServidor() {
        start(SOCKET_READ_TIMEOUT, false)

        Log.i(
            "ServidorHttpPagamento",
            "Servidor HTTP iniciado na porta $PORTA"
        )
    }

    fun pararServidor() {
        stop()
    }
}
