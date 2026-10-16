package br.com.sampaiosi.softwareultimate.ui.terminal

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.com.sampaiosi.softwareultimate.databinding.ActivityInformacoesTerminalBinding
import br.com.sampaiosi.softwareultimate.terminal.IdentificadorInternoTerminal
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
        binding.txtTerminalId.text = IdentificadorInternoTerminal.removerPrefixo(
            IdentificadorTerminal.obterOuCriarTerminalId(this)
        )
        binding.txtTerminalKey.text = IdentificadorInternoTerminal.removerPrefixo(
            IdentificadorTerminal.obterOuCriarTerminalKey(this)
        )
    }

    private fun configurarEventos() {
        binding.btnContinuar.setOnClickListener {
            finish()
        }
    }
}
