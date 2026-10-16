package br.com.sampaiosi.softwareultimate.comunicacao

class TerminalNaoAutenticadoException :
    RuntimeException(
        "A Moderninha não está ativada. Realize a ativação do terminal PagBank."
    )
