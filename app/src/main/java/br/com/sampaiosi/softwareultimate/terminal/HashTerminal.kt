package br.com.sampaiosi.softwareultimate.terminal

import java.security.MessageDigest

object HashTerminal {

    fun gerar(valor: String): String {
        val bytes = MessageDigest.getInstance("SHA-256")
            .digest(valor.toByteArray(Charsets.UTF_8))

        return bytes.joinToString("") { "%02X".format(it) }
    }
}
