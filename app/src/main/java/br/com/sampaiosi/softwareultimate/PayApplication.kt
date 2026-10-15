package br.com.sampaiosi.softwareultimate

import android.app.Application
import br.com.sampaiosi.softwareultimate.comunicacao.ServidorDescobertaTerminal
import br.com.sampaiosi.softwareultimate.comunicacao.ServidorHttpPagamento
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
            modules(_root_ide_package_.br.com.sampaiosi.softwareultimate.di.plugpagModule)
        }

        val plugPag = koinApplication.koin.get<PlugPag>()

        servidorDescobertaTerminal =
            ServidorDescobertaTerminal(
                this
            )
        servidorDescobertaTerminal.iniciar()

        servidorHttpPagamento =
            ServidorHttpPagamento(
                this,
                plugPag
            )
        servidorHttpPagamento.iniciarServidor()
    }
}
