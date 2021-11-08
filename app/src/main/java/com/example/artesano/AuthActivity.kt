package com.example.artesano

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_auth.* // para acceder a los ids de los elementos.
import kotlinx.android.synthetic.main.activity_auth_cliente.*

class AuthActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        Thread.sleep(2000) // Aguantamos unos segundos para que se note la Splash Screen
        setTheme(R.style.Theme_Artesano) // Primero se carga el "TemaSplash" luego al finalizar carga el normal
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        //Funcion para ingreso del Artesano
        loadAutenticacion()
    }

    private fun loadAutenticacion(){
        btnAuthArtesano.setOnClickListener(){
            showAuthArtesano()
        }

        btnAuthCliente.setOnClickListener(){
            showAuthCliente()
        }
    }

    private fun showAuthArtesano(){
        //navegamos a la pantalla del Logeo del artesano
        val artesanoAuthActivity = Intent(this, AuthActivityArtesano::class.java).apply {
        }
        startActivity(artesanoAuthActivity)
    }
    private fun showAuthCliente(){
        //navegamos a la pantalla del Logeo del Cliente
        val clienteAuthActivity = Intent(this, AuthActivityCliente::class.java).apply {
        }
        startActivity(clienteAuthActivity)
    }



}