package br.com.uol.pagbank.plugpagservice.demo.estorno

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

data class DadosEstadoEstorno(
    val tipo: TipoEstorno? = null,
    val valorCentavos: Int = 0,
    val mensagem: String = "Preparando estorno...",
    val finalizado: Boolean = false,
    val sucesso: Boolean? = null,
    val cancelado: Boolean = false
)

object EstadoEstorno {

    private val _estado = MutableLiveData(DadosEstadoEstorno())

    val estado: LiveData<DadosEstadoEstorno>
        get() = _estado

    fun iniciar(requisicao: RequisicaoEstorno) {
        _estado.postValue(
            DadosEstadoEstorno(
                tipo = requisicao.tipo,
                valorCentavos = requisicao.valorCentavos,
                mensagem = "Preparando estorno..."
            )
        )
    }

    fun atualizarMensagem(mensagem: String) {
        val atual = _estado.value ?: return
        _estado.postValue(atual.copy(mensagem = mensagem))
    }

    fun finalizar(sucesso: Boolean, mensagem: String) {
        val atual = _estado.value ?: return
        _estado.postValue(
            atual.copy(
                mensagem = mensagem,
                finalizado = true,
                sucesso = sucesso,
                cancelado = false
            )
        )
    }

    fun cancelar() {
        val atual = _estado.value ?: return
        _estado.postValue(
            atual.copy(
                mensagem = "Estorno cancelado",
                finalizado = true,
                sucesso = false,
                cancelado = true
            )
        )
    }
}
