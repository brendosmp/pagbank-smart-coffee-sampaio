package br.com.sampaiosi.softwareultimate.comunicacao

import android.content.Context
import android.util.Log
import br.com.sampaiosi.softwareultimate.estorno.RequisicaoEstorno
import br.com.sampaiosi.softwareultimate.estorno.ServicoEstorno
import br.com.sampaiosi.softwareultimate.pagamento.ServicoPagamento
import br.com.sampaiosi.softwareultimate.seguranca.ConfiguracaoHttps
import br.com.sampaiosi.softwareultimate.terminal.HashTerminal
import br.com.sampaiosi.softwareultimate.terminal.IdentificadorTerminal
import br.com.sampaiosi.softwareultimate.terminal.InformacoesConexaoTerminal
import br.com.sampaiosi.softwareultimate.terminal.SegurancaTerminal
import br.com.sampaiosi.softwareultimate.terminal.TerminalNaoAutorizadoException
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPag
import com.google.gson.Gson
import fi.iki.elonen.NanoHTTPD
import pagamento.RequisicaoPagamento

class ServidorHttpPagamento(context: Context, plugPag: PlugPag) : NanoHTTPD("0.0.0.0", PORTA) {
    private val contexto = context.applicationContext

    companion object {
        private const val PORTA = 9090
        private const val MIME_JSON = "application/json; charset=utf-8"
    }

    private val gson = Gson()
    private val servicoPagamento =
        ServicoPagamento(
            context.applicationContext,
            plugPag
        )
    private val servicoEstorno =
        ServicoEstorno(
            context.applicationContext,
            plugPag
        )
    private val bloqueioPlugPag = Any()

    override fun serve(session: IHTTPSession): Response {
        return try {
            when {
                session.method == Method.GET && session.uri == "/terminal" ->
                    obterInformacoesTerminal()

                session.method == Method.POST && session.uri == "/pagamentos" -> {
                    SegurancaTerminal.validar(contexto, session.headers)
                    receberPagamento(session)
                }

                session.method == Method.POST && session.uri == "/estornos" -> {
                    SegurancaTerminal.validar(contexto, session.headers)
                    receberEstorno(session)
                }

                else -> respostaErro(
                    Response.Status.NOT_FOUND,
                    "ROTA_NAO_ENCONTRADA",
                    "O recurso solicitado não foi encontrado."
                )
            }
        } catch (e: TerminalNaoAutorizadoException) {
            Log.e("ServidorHttp", "Terminal não autorizado", e)

            respostaErro(
                Response.Status.UNAUTHORIZED,
                "TERMINAL_NAO_AUTORIZADO",
                e.message ?: "Terminal não autorizado."
            )
        } catch (e: IllegalArgumentException) {
            Log.e("ServidorHttp", "Requisição inválida", e)

            respostaErro(
                Response.Status.BAD_REQUEST,
                "REQUISICAO_INVALIDA",
                e.message ?: "Os dados enviados são inválidos."
            )
        } catch (e: TerminalNaoAutenticadoException) {
            Log.e("ServidorHttp", "Terminal não autenticado", e)

            respostaErro(
                Response.Status.SERVICE_UNAVAILABLE,
                "TERMINAL_NAO_ATIVADO",
                e.message ?: "A Moderninha não está ativada."
            )
        } catch (e: TerminalOcupadoException) {
            Log.e("ServidorHttp", "Terminal ocupado", e)

            respostaErro(
                Response.Status.CONFLICT,
                "TERMINAL_OCUPADO",
                e.message ?: "A Moderninha está ocupada."
            )
        } catch (e: Exception) {
            Log.e("ServidorHttp", "Erro inesperado", e)

            respostaErro(
                Response.Status.INTERNAL_ERROR,
                "ERRO_INTERNO",
                "Ocorreu um erro interno ao processar a operação."
            )
        }
    }

    private fun receberPagamento(session: IHTTPSession): Response {
        val json = obterCorpo(session) ?: return respostaErro(
            Response.Status.BAD_REQUEST,
            "CORPO_NAO_INFORMADO",
            "O corpo da requisição de pagamento não foi informado."
        )

        val requisicao = try {
            gson.fromJson(
                json,
                RequisicaoPagamento::class.java
            )
        } catch (_: Exception) {
            return respostaErro(
                Response.Status.BAD_REQUEST,
                "JSON_PAGAMENTO_INVALIDO",
                "Os dados enviados para o pagamento estão em formato inválido."
            )
        }

        Log.i("ServidorHttp", "Pagamento recebido. PedidoId=${requisicao.pedidoId}")

        val resposta = synchronized(bloqueioPlugPag) {
            servicoPagamento.executar(requisicao)
        }

        Log.i("ServidorHttp", "Pagamento finalizado. PedidoId=${requisicao.pedidoId}")

        return respostaOk(gson.toJson(resposta))
    }

    private fun receberEstorno(session: IHTTPSession): Response {
        val json = obterCorpo(session) ?: return respostaErro(
            Response.Status.BAD_REQUEST,
            "CORPO_NAO_INFORMADO",
            "O corpo da requisição de estorno não foi informado."
        )

        val requisicao = try {
            gson.fromJson(
                json,
                RequisicaoEstorno::class.java
            )
        } catch (_: Exception) {
            return respostaErro(
                Response.Status.BAD_REQUEST,
                "JSON_ESTORNO_INVALIDO",
                "Os dados enviados para o estorno estão em formato inválido."
            )
        }

        Log.i("ServidorHttp", "Estorno recebido. TransactionId=${requisicao.transactionId}")

        val resposta = synchronized(bloqueioPlugPag) {
            servicoEstorno.executar(requisicao)
        }

        Log.i("ServidorHttp", "Estorno finalizado. TransactionId=${requisicao.transactionId}")

        return respostaOk(gson.toJson(resposta))
    }

    private fun obterInformacoesTerminal(): Response {
        val terminalId = IdentificadorTerminal.obterOuCriarTerminalId(contexto)
        val terminalKey = IdentificadorTerminal.obterOuCriarTerminalKey(contexto)

        val resposta = InformacoesConexaoTerminal(
            terminalId = terminalId,
            terminalKeyHash = HashTerminal.gerar(terminalKey)
        )

        return respostaOk(gson.toJson(resposta))
    }

    private fun obterCorpo(session: IHTTPSession): String? {
        val arquivos = HashMap<String, String>()
        session.parseBody(arquivos)

        return arquivos["postData"]?.takeIf { it.isNotBlank() }
    }

    private fun respostaOk(json: String): Response {
        return newFixedLengthResponse(Response.Status.OK, MIME_JSON, json)
    }

    private fun respostaErro(status: Response.Status, codigo: String, mensagem: String): Response {
        val erro = RespostaErro(
            codigo,
            mensagem
        )
        return newFixedLengthResponse(status, MIME_JSON, gson.toJson(erro))
    }

    fun iniciarServidor() {
        Log.i("ServidorHttp", "Configurando servidor HTTPS...")

        makeSecure(
            ConfiguracaoHttps.criarServerSocketFactory(contexto),
            arrayOf("TLSv1.2")
        )

        Log.i("ServidorHttp", "Configuração HTTPS concluída")

        start(SOCKET_READ_TIMEOUT, false)

        Log.i("ServidorHttp", "Servidor HTTPS iniciado na porta $PORTA")
    }
}
