package br.com.uol.pagbank.plugpagservice.demo

import android.app.Application
import br.com.uol.pagbank.plugpagservice.demo.comunicacao.ServidorDescobertaTerminal
import br.com.uol.pagbank.plugpagservice.demo.di.plugpagModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class PayApplication : Application() {

    private lateinit var servidorDescobertaTerminal: ServidorDescobertaTerminal

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@PayApplication)
            modules(plugpagModule)
        }

        servidorDescobertaTerminal = ServidorDescobertaTerminal(this)
        servidorDescobertaTerminal.iniciar()
    }
}
