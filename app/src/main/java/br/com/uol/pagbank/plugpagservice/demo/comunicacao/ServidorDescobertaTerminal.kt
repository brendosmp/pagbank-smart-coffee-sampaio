package br.com.uol.pagbank.plugpagservice.demo.comunicacao

import android.content.Context
import android.util.Log
import br.com.uol.pagbank.plugpagservice.demo.terminal.IdentificadorTerminal
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress

class ServidorDescobertaTerminal(context: Context) {
    private val contexto = context.applicationContext
    private val porta = 9091
    private var socket: DatagramSocket? = null

    @Volatile
    private var executando = false

    fun iniciar() {
        if (executando)
            return

        executando = true

        Thread {
            try {
                val terminalId = IdentificadorTerminal.obterOuCriar(contexto)

                socket = DatagramSocket(null).apply {
                    reuseAddress = true
                    bind(InetSocketAddress("0.0.0.0", porta))
                }

                Log.i("DescobertaTerminal", "Servidor UDP iniciado na porta $porta")

                while (executando) {
                    val buffer = ByteArray(1024)
                    val pacote = DatagramPacket(buffer, buffer.size)

                    socket?.receive(pacote)

                    val mensagem = String(
                        pacote.data,
                        0,
                        pacote.length,
                        Charsets.UTF_8
                    ).trim()

                    Log.i(
                        "DescobertaTerminal",
                        "Recebido de ${pacote.address.hostAddress}: $mensagem"
                    )

                    if (mensagem == terminalId) {
                        val resposta = terminalId.toByteArray(Charsets.UTF_8)

                        val pacoteResposta = DatagramPacket(
                            resposta,
                            resposta.size,
                            pacote.address,
                            pacote.port
                        )

                        socket?.send(pacoteResposta)

                        Log.i(
                            "DescobertaTerminal",
                            "Terminal encontrado. Respondendo para ${pacote.address.hostAddress}"
                        )
                    }
                }
            } catch (e: Exception) {
                if (executando)
                    Log.e("DescobertaTerminal", "Erro no servidor de descoberta", e)
            }
        }.start()
    }

    fun parar() {
        executando = false

        socket?.close()
        socket = null
    }
}
