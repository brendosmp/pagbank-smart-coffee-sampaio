package br.com.sampaiosi.softwareultimate.terminal

data class RespostaTerminal(
    val terminalId: String,
    val certificadoFingerprint: String,
    val assinatura: String
)
