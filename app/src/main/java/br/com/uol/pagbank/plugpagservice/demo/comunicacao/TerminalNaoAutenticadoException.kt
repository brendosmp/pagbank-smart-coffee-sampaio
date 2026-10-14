package br.com.uol.pagbank.plugpagservice.demo.comunicacao

class TerminalNaoAutenticadoException :
    RuntimeException("A Moderninha não está ativada. Realize a ativação do terminal PagBank.")
