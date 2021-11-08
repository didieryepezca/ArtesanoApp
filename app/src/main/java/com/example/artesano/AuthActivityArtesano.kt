package com.example.artesano

import android.content.Context
import android.content.Intent

import android.os.Bundle
import android.view.View
import android.widget.Toast //mensajitos Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.common.reflect.TypeToken
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import kotlinx.android.synthetic.main.activity_auth_artesano.*
import com.google.gson.GsonBuilder

class AuthActivityArtesano : AppCompatActivity() {

    val validations = SomeValidations() //llamamos a la clase que contiene las validaciones

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth_artesano)

        //Registro del Artesano con Email y Password
        ingresoArtesano()

        //Session de artesano para que no se salga cuando esta logeado
        sessionArtesano()

        //Boton Olvide mi Contraseña de Artesano.
        forgotPasswordArtesano()
    }

    //COMPROBAR SI EXISTE UNA SESION INICIADA
    private fun sessionArtesano(){
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE) //obtenemos el string creado en "strings.xml"
        val emailArtesano:String? = prefs.getString("emailArtesano",null)
        //val provider:String? = prefs.getString("provider",null)

        if(emailArtesano!=null){
            authLayoutArtesano.visibility = View.INVISIBLE //No mostrar la pantalla de logeo en caso haya una sesion iniciada
            showHomeArtesano(emailArtesano)
        }
    }

    //Si hacemos Logout, deberiamos volver a mostrar el Layout de Logeo
    override fun onStart() {
        super.onStart()
        authLayoutArtesano.visibility = View.VISIBLE //authLayout es el id de nuestro Layout del login Cliente
    }



    private fun ingresoArtesano(){

        btnEntrarArtesano.setOnClickListener(){
            //---------------------------------------------
            var email = editTextTextEmailAddress.text.toString()
            var password = editTextTextPassword.text.toString()

            var esMailValido = validations.isValidEmail(email)

            if (email.isNotEmpty() && password.isNotEmpty()){

                if(esMailValido == true){

                    //----Configuracion remota para el acceso de los principales stakeholders.
                    Firebase.remoteConfig.fetchAndActivate().addOnCompleteListener{ tarea ->
                        if(tarea.isSuccessful){
                            //val user:String = Firebase.remoteConfig.getString("usuario") //Obtenemos la configuracion remota de la clave "usuario"
                            //println(user)
                            val gson = GsonBuilder().create()
                            val json = FirebaseRemoteConfig.getInstance().getString("artesanos")
                            val artesanos: List<ModelArtesano> =
                                gson.fromJson(json, object : TypeToken<List<ModelArtesano?>?>() {}.type)

                            println(artesanos)
                            //si la configuracion remota contiene uno de los emails ingresados
                            if(artesanos.contains(ModelArtesano(artesanoId = email))){
                                //Finalmente Autenticacion Firebase
                                FirebaseAuth.getInstance().signInWithEmailAndPassword(email ,password).addOnCompleteListener{
                                    if(it.isSuccessful){
                                        //los simbolos de preguntas admiten emails nulos
                                        showHomeArtesano(it.result?.user?.email?:"")
                                    }else{
                                        Toast.makeText(this, "Error de Autenticacion", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }else{
                                Toast.makeText(this, "No hemos encontrado un usuario registrado", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                    //----Configuracion remota para el acceso de los dueños y chefs del artesano.
                }else{
                    Toast.makeText(this, "Email Inválido, por favor ingresa un email válido", Toast.LENGTH_LONG).show()
                }
            }else{
                showAlert(email,password)
            }
        }
    }

    private fun showHomeArtesano(email: String){
        //navegamos y colocamos los datos en la pantalla Home
        val homeIntent = Intent(this,HomeActivityArtesano::class.java).apply {
            putExtra("emailArtesano", email)
        }
        startActivity(homeIntent)
    }


    private fun forgotPasswordArtesano(){
        btnRestablecerArtesano.setOnClickListener(){
            showForgotPassword()
        }
    }
    private fun showForgotPassword(){
        val forgotPass = Intent(this,AuthActivityPassRecovery::class.java).apply {
        }
        startActivity(forgotPass)
    }

    fun showAlert(email:String, password: String){
        var builder = AlertDialog.Builder(this)
        builder.setTitle("ERROR")

        if (email.isEmpty() && password.isEmpty()){
            builder.setMessage("Por favor ingrese sus Credenciales.")
        } else if (email.isEmpty()){
            builder.setMessage("Por favor ingrese su Usuario/Email.")
        } else if (password.isEmpty()){
            builder.setMessage("Por favor ingrese su Contraseña.")
        } else {
            builder.setMessage("Hubo un problema al autenticarse.")
        }
        builder.setPositiveButton("Aceptar",null)
        var dialog: AlertDialog = builder.create()
        dialog.show()
    }
}