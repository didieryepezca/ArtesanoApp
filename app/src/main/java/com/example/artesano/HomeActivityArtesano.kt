package com.example.artesano

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_home_artesano.*

class HomeActivityArtesano : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_artesano)

        //creamos funcion que se ejecutara con la carga. los bundles son para obtener los datos que vienen del Login
        var bundle = intent.extras
        var emailArtesano:String? = bundle?.getString("emailArtesano")

        homeSetupArtesano(emailArtesano?:"")

        //Guardar el usuario autenticado a nivel de app.
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit() //obtenemos el string creado en "strings.xml"
        prefs.putString("emailArtesano", emailArtesano)

        prefs.apply() //aplicamos los cambios a los strings

    }


    private fun homeSetupArtesano(email: String){

        txtArtesanoLoged.text = "Bienvenido(a) ${email} !"


        //---------- mostrar Carta
        cardButtonVerCarta.setOnClickListener(){

            var cartaActivity = Intent(this, CartaActivityArtesano::class.java).apply{

                putExtra("email", email)
            }
            startActivity(cartaActivity)
        }


        cardButtonLogOutArtesano.setOnClickListener(){

            //en el momento que presionamos CERRAR SESION se eliminan los datos temporales del usuario
            val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit() //obtenemos el string creado en "strings.xml"
            prefs.clear()
            prefs.apply()

            FirebaseAuth.getInstance().signOut()
            onBackPressed()// para volver a la pantalla anterior
        }

    }
}