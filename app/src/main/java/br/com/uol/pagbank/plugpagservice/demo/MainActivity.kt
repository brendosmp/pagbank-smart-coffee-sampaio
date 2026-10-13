package br.com.uol.pagbank.plugpagservice.demo

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import br.com.uol.pagbank.plugpagservice.demo.databinding.ActivityMainBinding
import br.com.uol.pagbank.plugpagservice.demo.terminal.IdentificadorTerminal
import br.com.uol.pagbank.plugpagservice.demo.ui.terminal.InformacoesTerminalActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var animacaoLogo: AnimatorSet? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val terminalId =
            IdentificadorTerminal.obterOuCriar(this)

        mostrarTelaPadrao(terminalId)

        if (
            IdentificadorTerminal.deveMostrarPrimeiroAcesso(
                this,
                terminalId
            )
        ) {
            abrirInformacoesTerminal()

            IdentificadorTerminal.marcarComoExibido(
                this,
                terminalId
            )
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menuVerTerminal -> {
                abrirInformacoesTerminal()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun abrirInformacoesTerminal() {
        val intent = Intent(
            this,
            InformacoesTerminalActivity::class.java
        )

        startActivity(intent)
    }

    @SuppressLint("SetTextI18n")
    private fun mostrarTelaPadrao(terminalId: String) {
        binding.layoutPrimeiroAcesso.visibility = View.GONE
        binding.layoutPadrao.visibility = View.VISIBLE

        iniciarAnimacaoLogo()
    }

    private fun iniciarAnimacaoLogo() {
        if (animacaoLogo != null)
            return

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
            playTogether(
                escalaX,
                escalaY,
                transparencia
            )

            start()
        }
    }

    private fun pararAnimacaoLogo() {
        animacaoLogo?.cancel()
        animacaoLogo = null
    }

    override fun onDestroy() {
        pararAnimacaoLogo()
        super.onDestroy()
    }
}
