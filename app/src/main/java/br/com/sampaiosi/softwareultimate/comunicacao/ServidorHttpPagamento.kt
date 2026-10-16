package br.com.sampaiosi.softwareultimate.comunicacao

import android.content.Context
import android.util.Log
import br.com.sampaiosi.softwareultimate.estorno.RequisicaoEstorno
import br.com.sampaiosi.softwareultimate.estorno.ServicoEstorno
import br.com.sampaiosi.softwareultimate.pagamento.ServicoPagamento
import br.com.sampaiosi.softwareultimate.seguranca.AssinaturaTerminal
import br.com.sampaiosi.softwareultimate.seguranca.ConfiguracaoHttps
import br.com.sampaiosi.softwareultimate.seguranca.FingerprintCertificado
import br.com.sampaiosi.softwareultimate.terminal.IdentificadorTerminal
import br.com.sampaiosi.softwareultimate.terminal.RespostaTerminal
import br.com.sampaiosi.softwareultimate.terminal.SegurancaTerminal
import br.com.sampaiosi.softwareultimate.terminal.TerminalNaoAutorizadoException
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPag
import com.google.gson.Gson
import fi.iki.elonen.NanoHTTPD
import pagamento.RequisicaoPagamento

class ServidorHttpPagamento(
    context: Context,
    plugPag: PlugPag
) : NanoHTTPD("0.0.0.0", PORTA) {

    companion object {
        private const val TAG = "ServidorHttp"
        private const val PORTA = 9090
        private const val MIME_JSON = "application/json; charset=utf-8"
    }

    private val contexto = context.applicationContext
    private val gson = Gson()

    private val servicoPagamento = ServicoPagamento(
        contexto,
        plugPag
    )

    private val servicoEstorno = ServicoEstorno(
        contexto,
        plugPag
    )

    private val bloqueioPlugPag = Any()

    override fun serve(session: IHTTPSession): Response {
        return try {
            when {
                session.method == Method.GET &&
                        session.uri == "/terminal" ->
                    processarTerminal(session)

                session.method == Method.POST &&
                        session.uri == "/pagamentos" -> {
                    validarTerminal(session)
                    receberPagamento(session)
                }

                session.method == Method.POST &&
                        session.uri == "/estornos" -> {
                    validarTerminal(session)
                    receberEstorno(session)
                }

                else ->
                    respostaErro(
                        Response.Status.NOT_FOUND,
                        "ROTA_NAO_ENCONTRADA",
                        "Rota não encontrada."
                    )
            }
        } catch (e: TerminalNaoAutorizadoException) {
            Log.e(TAG, "Terminal não autorizado", e)

            respostaErro(
                Response.Status.UNAUTHORIZED,
                "TERMINAL_NAO_AUTORIZADO",
                e.message ?: "Terminal não autorizado."
            )
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Requisição inválida", e)

            respostaErro(
                Response.Status.BAD_REQUEST,
                "REQUISICAO_INVALIDA",
                e.message ?: "Os dados enviados são inválidos."
            )
        } catch (e: TerminalNaoAutenticadoException) {
            Log.e(TAG, "Terminal não autenticado", e)

            respostaErro(
                Response.Status.SERVICE_UNAVAILABLE,
                "TERMINAL_NAO_ATIVADO",
                e.message ?: "A Moderninha não está ativada."
            )
        } catch (e: TerminalOcupadoException) {
            Log.e(TAG, "Terminal ocupado", e)

            respostaErro(
                Response.Status.CONFLICT,
                "TERMINAL_OCUPADO",
                e.message ?: "A Moderninha está ocupada."
            )
        } catch (e: Exception) {
            Log.e(TAG, "Erro inesperado", e)

            respostaErro(
                Response.Status.INTERNAL_ERROR,
                "ERRO_INTERNO",
                "Ocorreu um erro interno ao processar a operação."
            )
        }
    }

    private fun processarTerminal(
        session: IHTTPSession
    ): Response {
        Log.i(TAG, "Recebida requisição GET /terminal")

        val nonce =
            session.parameters["nonce"]?.firstOrNull()

        if (nonce.isNullOrBlank()) {
            Log.w(TAG, "Nonce não informado")

            return respostaErro(
                Response.Status.BAD_REQUEST,
                "NONCE_NAO_INFORMADO",
                "Nonce obrigatório."
            )
        }

        val terminalId =
            IdentificadorTerminal
                .obterOuCriarTerminalId(contexto)

        val terminalKey =
            SegurancaTerminal
                .obterTerminalKey(contexto)

        val fingerprint =
            FingerprintCertificado
                .obterSha256(contexto)

        val assinatura =
            AssinaturaTerminal.gerar(
                terminalKey,
                terminalId,
                nonce,
                fingerprint
            )

        val resposta =
            RespostaTerminal(
                terminalId = terminalId,
                certificadoFingerprint = fingerprint,
                assinatura = assinatura
            )

        val json =
            gson.toJson(resposta)

        Log.i(
            TAG,
            "Resposta /terminal gerada. TerminalId=$terminalId"
        )

        return respostaOk(json)
    }

    private fun validarTerminal(
        session: IHTTPSession
    ) {
        Log.i(TAG, "Validando credenciais do PDV")

        SegurancaTerminal.validar(
            contexto,
            session.headers
        )

        val terminalId =
            session.headers["x-terminal-id"]

        Log.i(
            TAG,
            "Terminal autorizado. TerminalId=$terminalId"
        )
    }

    private fun receberPagamento(
        session: IHTTPSession
    ): Response {
        Log.i(TAG, "Recebida requisição POST /pagamentos")

        val json =
            obterCorpo(session)
                ?: return respostaErro(
                    Response.Status.BAD_REQUEST,
                    "CORPO_NAO_INFORMADO",
                    "O corpo da requisição de pagamento não foi informado."
                )

        val requisicao =
            try {
                gson.fromJson(
                    json,
                    RequisicaoPagamento::class.java
                )
            } catch (e: Exception) {
                Log.w(
                    TAG,
                    "JSON de pagamento inválido",
                    e
                )

                return respostaErro(
                    Response.Status.BAD_REQUEST,
                    "JSON_PAGAMENTO_INVALIDO",
                    "Os dados enviados para o pagamento estão em formato inválido."
                )
            }

        Log.i(
            TAG,
            "Pagamento recebido. PedidoId=${requisicao.pedidoId}"
        )

        val resposta =
            synchronized(bloqueioPlugPag) {
                servicoPagamento.executar(requisicao)
            }

        Log.i(
            TAG,
            "Pagamento finalizado. PedidoId=${requisicao.pedidoId}"
        )

        return respostaOk(
            gson.toJson(resposta)
        )
    }

    private fun receberEstorno(
        session: IHTTPSession
    ): Response {
        Log.i(TAG, "Recebida requisição POST /estornos")

        val json =
            obterCorpo(session)
                ?: return respostaErro(
                    Response.Status.BAD_REQUEST,
                    "CORPO_NAO_INFORMADO",
                    "O corpo da requisição de estorno não foi informado."
                )

        val requisicao =
            try {
                gson.fromJson(
                    json,
                    RequisicaoEstorno::class.java
                )
            } catch (e: Exception) {
                Log.w(
                    TAG,
                    "JSON de estorno inválido",
                    e
                )

                return respostaErro(
                    Response.Status.BAD_REQUEST,
                    "JSON_ESTORNO_INVALIDO",
                    "Os dados enviados para o estorno estão em formato inválido."
                )
            }

        Log.i(
            TAG,
            "Estorno recebido. TransactionId=${requisicao.transactionId}"
        )

        val resposta =
            synchronized(bloqueioPlugPag) {
                servicoEstorno.executar(requisicao)
            }

        Log.i(
            TAG,
            "Estorno finalizado. TransactionId=${requisicao.transactionId}"
        )

        return respostaOk(
            gson.toJson(resposta)
        )
    }

    private fun obterCorpo(session: IHTTPSession): String? {
        val arquivos =
            HashMap<String, String>()

        session.parseBody(arquivos)

        return arquivos["postData"]
            ?.takeIf {
                it.isNotBlank()
            }
    }

    private fun respostaOk(
        json: String
    ): Response {
        return newFixedLengthResponse(
            Response.Status.OK,
            MIME_JSON,
            json
        )
    }

    private fun respostaErro(
        status: Response.Status,
        codigo: String,
        mensagem: String
    ): Response {
        val erro =
            RespostaErro(
                codigo,
                mensagem
            )

        return newFixedLengthResponse(
            status,
            MIME_JSON,
            gson.toJson(erro)
        )
    }

    fun iniciarServidor() {
        Log.i(
            TAG,
            "Configurando servidor HTTPS..."
        )

        makeSecure(
            ConfiguracaoHttps
                .criarServerSocketFactory(contexto),
            arrayOf("TLSv1.2")
        )

        Log.i(
            TAG,
            "Configuração HTTPS concluída"
        )

        start(
            SOCKET_READ_TIMEOUT,
            false
        )

        Log.i(
            TAG,
            "Servidor HTTPS iniciado na porta $PORTA"
        )
    }
}
