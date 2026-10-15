package br.com.sampaiosi.softwareultimate.ui.terminal

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.com.sampaiosi.softwareultimate.databinding.ActivityInformacoesTerminalBinding
import br.com.sampaiosi.softwareultimate.terminal.IdentificadorTerminal

class InformacoesTerminalActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInformacoesTerminalBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityInformacoesTerminalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        carregarInformacoes()
        configurarEventos()
    }

    private fun carregarInformacoes() {
        val terminalId =
            IdentificadorTerminal.obterOuCriarTerminalId(
                this
            )
        val terminalKey =
            IdentificadorTerminal.obterOuCriarTerminalKey(
                this
            )

        binding.txtTerminalId.text = terminalId
        binding.txtTerminalKey.text = terminalKey
    }

    private fun configurarEventos() {
        binding.btnContinuar.setOnClickListener {
            finish()
        }
    }
}
