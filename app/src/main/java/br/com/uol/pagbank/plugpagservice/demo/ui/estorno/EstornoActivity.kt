package br.com.uol.pagbank.plugpagservice.demo.ui.estorno

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import br.com.uol.pagbank.plugpagservice.demo.databinding.ActivityEstornoBinding
import br.com.uol.pagbank.plugpagservice.demo.estorno.EstadoEstorno
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPag
import org.koin.android.ext.android.inject
import java.text.NumberFormat
import java.util.Locale

class EstornoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEstornoBinding
    private val plugPag: PlugPag by inject()

    private var animacaoLogo: AnimatorSet? = null
    private var finalizacaoAgendada = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEstornoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        iniciarAnimacaoLogo()
        configurarCancelamento()
        observarEstorno()
    }

    private fun configurarCancelamento() {
        binding.btnCancelarEstorno.setOnClickListener {
            binding.btnCancelarEstorno.isEnabled = false
            EstadoEstorno.atualizarMensagem("Cancelando estorno...")

            Thread {
                try {
                    plugPag.abort()
                } catch (_: Exception) {
                    runOnUiThread {
                        binding.btnCancelarEstorno.isEnabled = true
                        EstadoEstorno.atualizarMensagem("Não foi possível cancelar o estorno.")
                    }
                }
            }.start()
        }
    }

    private fun observarEstorno() {
        EstadoEstorno.estado.observe(this) { estado ->
            binding.txtValor.text = formatarValor(estado.valorCentavos)
            binding.txtMensagem.text = estado.mensagem

            if (!estado.finalizado) {
                binding.btnCancelarEstorno.visibility = View.VISIBLE
                return@observe
            }

            binding.btnCancelarEstorno.visibility = View.GONE

            binding.txtTitulo.text = when {
                estado.cancelado -> "Estorno cancelado"
                estado.sucesso == true -> "Estorno realizado"
                else -> "Estorno não realizado"
            }

            finalizarTela()
        }
    }

    private fun finalizarTela() {
        if (finalizacaoAgendada)
            return

        finalizacaoAgendada = true

        binding.root.postDelayed({
            if (!isFinishing)
                finish()
        }, 1500)
    }

    private fun iniciarAnimacaoLogo() {
        if (animacaoLogo != null)
            return

        val escalaX = ObjectAnimator.ofFloat(binding.imgLogoSampaio, "scaleX", 0.88f, 1.12f)
        val escalaY = ObjectAnimator.ofFloat(binding.imgLogoSampaio, "scaleY", 0.88f, 1.12f)
        val transparencia = ObjectAnimator.ofFloat(binding.imgLogoSampaio, "alpha", 0.65f, 1f)

        escalaX.repeatMode = ObjectAnimator.REVERSE
        escalaY.repeatMode = ObjectAnimator.REVERSE
        transparencia.repeatMode = ObjectAnimator.REVERSE

        escalaX.repeatCount = ObjectAnimator.INFINITE
        escalaY.repeatCount = ObjectAnimator.INFINITE
        transparencia.repeatCount = ObjectAnimator.INFINITE

        escalaX.duration = 1200
        escalaY.duration = 1200
        transparencia.duration = 1200

        animacaoLogo = AnimatorSet().apply {
            playTogether(escalaX, escalaY, transparencia)
            start()
        }
    }

    private fun pararAnimacaoLogo() {
        animacaoLogo?.cancel()
        animacaoLogo = null

        if (::binding.isInitialized) {
            binding.imgLogoSampaio.scaleX = 1f
            binding.imgLogoSampaio.scaleY = 1f
            binding.imgLogoSampaio.alpha = 1f
        }
    }

    override fun onDestroy() {
        pararAnimacaoLogo()
        super.onDestroy()
    }

    private fun formatarValor(valorCentavos: Int): String {
        val valor = valorCentavos.toDouble() / 100.0
        return NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(valor)
    }
}
