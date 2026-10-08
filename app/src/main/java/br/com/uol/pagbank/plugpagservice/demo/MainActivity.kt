package br.com.uol.pagbank.plugpagservice.demo

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import br.com.uol.pagbank.plugpagservice.demo.databinding.ActivityMainBinding
import br.com.uol.pagbank.plugpagservice.demo.terminal.IdentificadorTerminal
import br.com.uol.pagbank.plugpagservice.demo.ui.terminal.InformacoesTerminalActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val terminalJaExistia = IdentificadorTerminal.existe(this)
        val terminalId = IdentificadorTerminal.obterOuCriar(this)

        mostrarTelaPadrao(terminalId)

        if (!terminalJaExistia)
            abrirInformacoesTerminal()
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

    @SuppressLint("SetTextI18n")
    private fun mostrarTelaPadrao(terminalId: String) {
        binding.txtTerminalId.text = "Terminal: $terminalId"
        binding.txtStatus.text = "Aguardando PDV..."
    }

    private fun abrirInformacoesTerminal() {
        val intent = Intent(this, InformacoesTerminalActivity::class.java)
        startActivity(intent)
    }
}
