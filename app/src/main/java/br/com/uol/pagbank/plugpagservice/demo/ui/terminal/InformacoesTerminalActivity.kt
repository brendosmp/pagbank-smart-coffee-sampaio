package br.com.uol.pagbank.plugpagservice.demo.ui.terminal

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.com.uol.pagbank.plugpagservice.demo.databinding.ActivityInformacoesTerminalBinding
import br.com.uol.pagbank.plugpagservice.demo.terminal.IdentificadorTerminal

class InformacoesTerminalActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInformacoesTerminalBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityInformacoesTerminalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.txtTerminalId.text = IdentificadorTerminal.obterOuCriar(this)

        binding.btnContinuar.setOnClickListener {
            finish()
        }
    }
}
