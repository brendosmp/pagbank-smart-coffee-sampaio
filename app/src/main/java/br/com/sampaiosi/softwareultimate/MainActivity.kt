package br.com.sampaiosi.softwareultimate

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import br.com.sampaiosi.softwareultimate.databinding.ActivityMainBinding
import br.com.sampaiosi.softwareultimate.terminal.IdentificadorTerminal
import br.com.sampaiosi.softwareultimate.ui.terminal.InformacoesTerminalActivity
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPag
import org.koin.android.ext.android.inject
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class MainActivity : androidx.appcompat.app.AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val plugPag: PlugPag by inject()

    private var animacaoLogo: AnimatorSet? = null
    private var verificandoPlugPag = false
    private var cancelandoOperacao = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarTerminal()
        configurarEventos()
    }

    override fun onResume() {
        super.onResume()

        if (::binding.isInitialized)
            verificarStatusPlugPag()
    }

    private fun configurarTerminal() {
        val terminalId = IdentificadorTerminal.obterOuCriarTerminalId(this)

        mostrarTelaPadrao()

        if (IdentificadorTerminal.deveMostrarPrimeiroAcesso(this, terminalId)) {
            abrirInformacoesTerminal()
            IdentificadorTerminal.marcarComoExibido(this, terminalId)
        }
    }

    private fun configurarEventos() {
        binding.btnCancelarOperacao.setOnClickListener {
            cancelarOperacaoAtual()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun mostrarTelaPadrao() {
        binding.layoutPrimeiroAcesso.visibility = View.GONE
        binding.layoutPadrao.visibility = View.VISIBLE
        binding.cardAvisoTerminal.visibility = View.GONE
        binding.btnCancelarOperacao.visibility = View.GONE

        iniciarAnimacaoLogo()
    }

    private fun verificarStatusPlugPag() {
        if (verificandoPlugPag || cancelandoOperacao)
            return

        verificandoPlugPag = true

        Thread {
            var respondeu = false
            var autenticado = false
            var ocupado = false

            val executor = Executors.newSingleThreadExecutor()

            try {
                val future = executor.submit {
                    autenticado = plugPag.isAuthenticated()
                    ocupado = plugPag.isServiceBusy()
                    respondeu = true
                }

                try {
                    future.get(10, TimeUnit.SECONDS)
                } catch (e: Exception) {
                    future.cancel(true)
                    Log.e(TAG, "Timeout ao consultar PlugPag", e)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao consultar PlugPag", e)
            } finally {
                executor.shutdownNow()
            }

            runOnUiThread {
                verificandoPlugPag = false

                when {
                    !respondeu -> {
                        mostrarAvisoTerminal("Serviço PagBank indisponível.", false)
                    }

                    !autenticado -> {
                        mostrarAvisoTerminal(
                            "Moderninha não ativada. Ative o terminal PagBank para utilizar pagamentos e estornos.",
                            false
                        )
                    }

                    ocupado -> {
                        mostrarAvisoTerminal(
                            "Moderninha ocupada no momento. Aguarde a operação atual finalizar.",
                            true
                        )
                    }

                    else -> {
                        esconderAvisoTerminal()
                    }
                }
            }
        }.start()
    }

    private fun cancelarOperacaoAtual() {
        if (cancelandoOperacao)
            return

        cancelandoOperacao = true

        binding.btnCancelarOperacao.isEnabled = false
        binding.btnCancelarOperacao.text = "CANCELANDO..."

        Thread {
            try {
                plugPag.abort()

                runOnUiThread {
                    Toast.makeText(
                        this,
                        "Cancelamento solicitado à Moderninha.",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                Thread.sleep(1500)
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao cancelar operação PlugPag", e)

                runOnUiThread {
                    Toast.makeText(
                        this,
                        "Não foi possível cancelar a operação.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } finally {
                runOnUiThread {
                    cancelandoOperacao = false

                    binding.btnCancelarOperacao.isEnabled = true
                    binding.btnCancelarOperacao.text = "CANCELAR OPERAÇÃO"

                    verificarStatusPlugPag()
                }
            }
        }.start()
    }

    private fun mostrarAvisoTerminal(mensagem: String, permitirCancelamento: Boolean) {
        binding.cardAvisoTerminal.visibility = View.VISIBLE
        binding.txtAvisoTerminal.text = mensagem

        binding.btnCancelarOperacao.visibility =
            if (permitirCancelamento) View.VISIBLE else View.GONE

        pararAnimacaoLogo()
    }

    private fun esconderAvisoTerminal() {
        binding.cardAvisoTerminal.visibility = View.GONE
        binding.txtAvisoTerminal.text = ""
        binding.btnCancelarOperacao.visibility = View.GONE

        iniciarAnimacaoLogo()
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

    companion object {
        private const val TAG = "MainActivity"
    }
}
