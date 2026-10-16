package br.com.sampaiosi.softwareultimate.comunicacao

class TerminalOcupadoException :
    RuntimeException(
        "A Moderninha está executando outra operação. Aguarde a operação atual finalizar."
    )
