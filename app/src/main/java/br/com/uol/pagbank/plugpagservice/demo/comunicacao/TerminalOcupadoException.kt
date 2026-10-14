package br.com.uol.pagbank.plugpagservice.demo.comunicacao

class TerminalOcupadoException :
    RuntimeException("A Moderninha está executando outra operação. Aguarde a operação atual finalizar.")
