package pagamento

import br.com.sampaiosi.softwareultimate.pagamento.TipoPagamento
import br.com.sampaiosi.softwareultimate.pagamento.TipoParcelamento

data class RequisicaoPagamento(
    val pedidoId: String,
    val valorCentavos: Int,
    val tipo: TipoPagamento,
    val parcelamento: TipoParcelamento = TipoParcelamento.A_VISTA,
    val parcelas: Int = 1,
    val imprimir: Boolean = true
)
