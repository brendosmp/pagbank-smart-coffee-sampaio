package br.com.uol.pagbank.plugpagservice.demo.ui.estorno

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.com.uol.pagbank.plugpagservice.demo.databinding.ActivityEstornoBinding
import br.com.uol.pagbank.plugpagservice.demo.estorno.EstadoEstorno

class EstornoActivity : AppCompatActivity() {
    private var animacaoLogo: AnimatorSet? = null
    private lateinit var binding: ActivityEstornoBinding
    private var animacao: AnimatorSet? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEstornoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        iniciarAnimacaoLogo()

        EstadoEstorno.estado.observe(this) { estado ->
            binding.txtMensagem.text = estado.mensagem

            if (!estado.finalizado) {
                binding.txtTitulo.text = "Realizando estorno"
                return@observe
            }

            pararAnimacao()

            binding.txtTitulo.text =
                if (estado.sucesso == true)
                    "Estorno realizado"
                else
                    "Falha no estorno"

            binding.root.postDelayed({
                finish()
            }, 1500)
        }
    }

    private fun iniciarAnimacaoLogo() {
        val escalaX = ObjectAnimator.ofFloat(
            binding.imgLogoSampaio,
            "scaleX",
            0.88f,
            1.12f
        )

        val escalaY = ObjectAnimator.ofFloat(
            binding.imgLogoSampaio,
            "scaleY",
            0.88f,
            1.12f
        )

        val transparencia = ObjectAnimator.ofFloat(
            binding.imgLogoSampaio,
            "alpha",
            0.65f,
            1f
        )

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

    private fun pararAnimacao() {
        animacao?.cancel()
        animacao = null
    }

    override fun onDestroy() {
        pararAnimacao()
        super.onDestroy()
    }
}
