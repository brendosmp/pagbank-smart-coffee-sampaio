package br.com.uol.pagbank.plugpagservice.demo

import android.app.Application
import br.com.uol.pagbank.plugpagservice.demo.comunicacao.ServidorDescobertaTerminal
import br.com.uol.pagbank.plugpagservice.demo.comunicacao.ServidorHttpPagamento
import br.com.uol.pagbank.plugpagservice.demo.di.plugpagModule
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPag
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class PayApplication : Application() {

    private lateinit var servidorDescobertaTerminal: ServidorDescobertaTerminal
    private lateinit var servidorHttpPagamento: ServidorHttpPagamento

    override fun onCreate() {
        super.onCreate()

        val koinApplication = startKoin {
            androidContext(this@PayApplication)
            modules(plugpagModule)
        }

        val plugPag = koinApplication.koin.get<PlugPag>()

        servidorDescobertaTerminal = ServidorDescobertaTerminal(this)
        servidorDescobertaTerminal.iniciar()

        servidorHttpPagamento = ServidorHttpPagamento(
            this,
            plugPag
        )

        servidorHttpPagamento.iniciarServidor()
    }
}
