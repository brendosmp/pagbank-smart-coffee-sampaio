package br.com.uol.pagbank.plugpagservice.demo.ui.estorno

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.com.uol.pagbank.plugpagservice.demo.databinding.ActivityEstornoBinding
import br.com.uol.pagbank.plugpagservice.demo.estorno.EstadoEstorno

class EstornoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEstornoBinding
    private var animacao: AnimatorSet? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEstornoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        iniciarAnimacao()

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

    private fun iniciarAnimacao() {
        val escalaX = ObjectAnimator.ofFloat(
            binding.bolinhaEstorno,
            "scaleX",
            0.82f,
            1.18f
        )

        val escalaY = ObjectAnimator.ofFloat(
            binding.bolinhaEstorno,
            "scaleY",
            0.82f,
            1.18f
        )

        val transparencia = ObjectAnimator.ofFloat(
            binding.bolinhaEstorno,
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

    override fun onDestroy() {
        pararAnimacao()
        super.onDestroy()
    }
}
