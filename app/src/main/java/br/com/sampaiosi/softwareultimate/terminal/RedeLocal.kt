package br.com.sampaiosi.softwareultimate.terminal

import android.content.Context
import android.net.ConnectivityManager
import java.net.Inet4Address

object RedeLocal {

    fun obterIpLocal(context: Context): String? {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val redeAtiva = connectivityManager.activeNetwork ?: return null
        val propriedades = connectivityManager.getLinkProperties(redeAtiva) ?: return null

        return propriedades.linkAddresses
            .map { it.address }
            .firstOrNull { endereco ->
                endereco is Inet4Address && !endereco.isLoopbackAddress
            }
            ?.hostAddress
    }
}
