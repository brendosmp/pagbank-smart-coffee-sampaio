package br.com.uol.pagbank.plugpagservice.demo.pagamento

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

data class DadosEstadoPagamento(
    val valorCentavos: Int = 0,
    val parcelamento: TipoParcelamento = TipoParcelamento.A_VISTA,
    val parcelas: Int = 1,
    val mensagem: String = "Realize o pagamento",
    val finalizado: Boolean = false,
    val aprovado: Boolean? = null
)

object EstadoPagamento {

    private val _estado = MutableLiveData(DadosEstadoPagamento())

    val estado: LiveData<DadosEstadoPagamento>
        get() = _estado

    fun iniciar(requisicao: RequisicaoPagamento) {
        _estado.postValue(
            DadosEstadoPagamento(
                valorCentavos = requisicao.valorCentavos,
                parcelamento = requisicao.parcelamento,
                parcelas = requisicao.parcelas,
                mensagem = "Realize o pagamento",
                finalizado = false,
                aprovado = null
            )
        )
    }

    fun atualizarMensagem(mensagem: String) {
        val atual = _estado.value ?: DadosEstadoPagamento()

        _estado.postValue(
            atual.copy(
                mensagem = mensagem
            )
        )
    }

    fun finalizar(aprovado: Boolean, mensagem: String) {
        val atual = _estado.value ?: DadosEstadoPagamento()

        _estado.postValue(
            atual.copy(
                mensagem = mensagem,
                finalizado = true,
                aprovado = aprovado
            )
        )
    }
}
