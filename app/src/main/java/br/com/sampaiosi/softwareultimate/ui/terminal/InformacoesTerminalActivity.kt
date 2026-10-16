package br.com.sampaiosi.softwareultimate.ui.terminal

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.com.sampaiosi.softwareultimate.databinding.ActivityInformacoesTerminalBinding
import br.com.sampaiosi.softwareultimate.terminal.IdentificadorInternoTerminal
import br.com.sampaiosi.softwareultimate.terminal.IdentificadorTerminal
import br.com.sampaiosi.softwareultimate.terminal.RedeLocal

class InformacoesTerminalActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInformacoesTerminalBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityInformacoesTerminalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        carregarInformacoes()
        configurarEventos()
    }

    @SuppressLint("SetTextI18n")
    private fun carregarInformacoes() {
        val terminalId = IdentificadorInternoTerminal.removerPrefixo(
            IdentificadorTerminal.obterOuCriarTerminalId(this)
        )

        val terminalKey = IdentificadorInternoTerminal.removerPrefixo(
            IdentificadorTerminal.obterOuCriarTerminalKey(this)
        )

        val ipLocal = RedeLocal.obterIpLocal(this)

        binding.txtTerminalId.text = terminalId
        binding.txtTerminalKey.text = terminalKey
        binding.txtIpLocal.text = ipLocal ?: "Não conectado"

        binding.txtPorta.text = "9090"
    }

    private fun configurarEventos() {
        binding.btnContinuar.setOnClickListener {
            finish()
        }
    }
}
