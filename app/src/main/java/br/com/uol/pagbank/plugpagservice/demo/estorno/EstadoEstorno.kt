package br.com.uol.pagbank.plugpagservice.demo.estorno

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

data class DadosEstadoEstorno(
    val mensagem: String = "Preparando estorno...",
    val finalizado: Boolean = false,
    val sucesso: Boolean? = null
)

object EstadoEstorno {

    private val _estado = MutableLiveData(DadosEstadoEstorno())

    val estado: LiveData<DadosEstadoEstorno>
        get() = _estado

    fun iniciar() {
        _estado.postValue(
            DadosEstadoEstorno(
                mensagem = "Preparando estorno...",
                finalizado = false,
                sucesso = null
            )
        )
    }

    fun atualizarMensagem(mensagem: String) {
        val atual = _estado.value ?: DadosEstadoEstorno()

        _estado.postValue(
            atual.copy(
                mensagem = mensagem
            )
        )
    }

    fun finalizar(sucesso: Boolean, mensagem: String) {
        val atual = _estado.value ?: DadosEstadoEstorno()

        _estado.postValue(
            atual.copy(
                mensagem = mensagem,
                finalizado = true,
                sucesso = sucesso
            )
        )
    }
}
