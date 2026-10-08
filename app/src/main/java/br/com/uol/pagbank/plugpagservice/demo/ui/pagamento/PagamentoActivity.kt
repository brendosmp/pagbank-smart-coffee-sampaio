package br.com.uol.pagbank.plugpagservice.demo.ui.pagamento

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.com.uol.pagbank.plugpagservice.demo.databinding.ActivityPagamentoBinding
import br.com.uol.pagbank.plugpagservice.demo.pagamento.EstadoPagamento
import br.com.uol.pagbank.plugpagservice.demo.pagamento.TipoParcelamento
import java.text.NumberFormat
import java.util.Locale

class PagamentoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPagamentoBinding
    private var animacao: AnimatorSet? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPagamentoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        iniciarAnimacao()

        EstadoPagamento.estado.observe(this) { estado ->
            atualizarTela(
                estado.valorCentavos,
                estado.parcelamento,
                estado.parcelas,
                estado.mensagem,
                estado.finalizado,
                estado.aprovado
            )
        }
    }

    @SuppressLint("SetTextI18n")
    private fun atualizarTela(
        valorCentavos: Int,
        parcelamento: TipoParcelamento,
        parcelas: Int,
        mensagem: String,
        finalizado: Boolean,
        aprovado: Boolean?
    ) {
        binding.txtValor.text = formatarValor(valorCentavos)
        binding.txtParcelamento.text = formatarParcelamento(parcelamento, parcelas)
        binding.txtMensagem.text = mensagem

        if (!finalizado) {
            binding.txtTitulo.text = "Realize o pagamento"
            return
        }

        pararAnimacao()

        binding.txtTitulo.text =
            if (aprovado == true)
                "Pagamento aprovado"
            else
                "Pagamento não autorizado"

        binding.root.postDelayed({
            finish()
        }, 1500)
    }

    private fun iniciarAnimacao() {
        val escalaX = ObjectAnimator.ofFloat(
            binding.bolinhaPagamento,
            "scaleX",
            0.82f,
            1.18f
        )

        val escalaY = ObjectAnimator.ofFloat(
            binding.bolinhaPagamento,
            "scaleY",
            0.82f,
            1.18f
        )

        val transparencia = ObjectAnimator.ofFloat(
            binding.bolinhaPagamento,
            "alpha",
            0.55f,
            1f
        )

        escalaX.repeatMode = ObjectAnimator.REVERSE
        escalaY.repeatMode = ObjectAnimator.REVERSE
        transparencia.repeatMode = ObjectAnimator.REVERSE

        escalaX.repeatCount = ObjectAnimator.INFINITE
        escalaY.repeatCount = ObjectAnimator.INFINITE
        transparencia.repeatCount = ObjectAnimator.INFINITE

        escalaX.duration = 1100
        escalaY.duration = 1100
        transparencia.duration = 1100

        animacao = AnimatorSet().apply {
            playTogether(escalaX, escalaY, transparencia)
            start()
        }
    }

    private fun pararAnimacao() {
        animacao?.cancel()
        animacao = null
    }

    private fun formatarValor(valorCentavos: Int): String {
        val valor = valorCentavos / 100.0

        return NumberFormat
            .getCurrencyInstance(Locale("pt", "BR"))
            .format(valor)
    }

    override fun onDestroy() {
        pararAnimacao()
        super.onDestroy()
    }

    private fun formatarParcelamento(
        parcelamento: TipoParcelamento,
        parcelas: Int
    ): String {
        if (parcelamento == TipoParcelamento.A_VISTA || parcelas <= 1)
            return "À vista"

        return when (parcelamento) {
            TipoParcelamento.A_VISTA -> "À vista"
            TipoParcelamento.PARC_VENDEDOR -> "${parcelas}x • Parcelado vendedor"
            TipoParcelamento.PARC_COMPRADOR -> "${parcelas}x • Parcelado comprador"
        }
    }
}
