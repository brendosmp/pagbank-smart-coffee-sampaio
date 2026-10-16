package br.com.sampaiosi.softwareultimate.terminal

object IdentificadorInternoTerminal {

    private const val PREFIXO = "SMP_"

    fun normalizar(valor: String): String {
        val valorLimpo = valor.trim()

        return if (valorLimpo.startsWith(PREFIXO, ignoreCase = true))
            PREFIXO + valorLimpo.substring(PREFIXO.length)
        else
            PREFIXO + valorLimpo
    }

    fun removerPrefixo(valor: String): String {
        val valorLimpo = valor.trim()

        return if (valorLimpo.startsWith(PREFIXO, ignoreCase = true))
            valorLimpo.substring(PREFIXO.length)
        else
            valorLimpo
    }
}
