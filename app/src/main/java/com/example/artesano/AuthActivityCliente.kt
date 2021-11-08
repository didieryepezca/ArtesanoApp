package com.example.artesano

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_auth_artesano.*

import kotlinx.android.synthetic.main.activity_auth_cliente.*

class AuthActivityCliente : AppCompatActivity() {

    private val GOOGLE_SIGN_IN = 100

    val validations = SomeValidations() //llamamos a la clase que contiene las validaciones

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth_cliente)
        // cargar cliente registro
        loadClienteRegistro()

        //Logearse con Google
        googleLogin()
        session()

       //Login normal
        fnClienteEntrar()

        //Boton Olvide mi Contraseña de Cliente.
        forgotPasswordCliente()
    }

    //COMPROBAR SI EXISTE UNA SESION INICIADA
    private fun session(){
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE) //obtenemos el string creado en "strings.xml"
        val nombre:String? = prefs.getString("nombre",null)
        val email: String? = prefs.getString("email", null)
        //val provider:String? = prefs.getString("provider",null)

        if(nombre!=null && email != null){
            authLayoutCliente.visibility = View.INVISIBLE //No mostrar la pantalla de logeo en caso haya una sesion iniciada
            showHome(nombre,email)
        }
    }

    //Si hacemos Logout, deberiamos volver a mostrar el Layout de Logeo
    override fun onStart() {
        super.onStart()
        authLayoutCliente.visibility = View.VISIBLE //authLayout es el id de nuestro Layout del login Cliente
    }


    private fun googleLogin(){

        googleButton.setOnClickListener(){

            //Configuración de Google
            val googleConf: GoogleSignInOptions =
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken("994576054609-prk4mpptvudh4r5l7t7avls1k1pqnij9.apps.googleusercontent.com") //client id de archivo google-services.json
                    .requestEmail()
                    .build()

            var googleClient = GoogleSignIn.getClient(this,googleConf)
            googleClient.signOut() // si se hace click en Logeo con google se sale de todas las que esten logeadas.
            startActivityForResult(googleClient.signInIntent,GOOGLE_SIGN_IN)

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GOOGLE_SIGN_IN){

            val task = GoogleSignIn.getSignedInAccountFromIntent(data)

            try {
                val account = task.getResult(ApiException::class.java)

                if (account != null) {
                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                    FirebaseAuth.getInstance().signInWithCredential(credential)
                        .addOnCompleteListener() {
                            if (it.isSuccessful) {
                                //los simbolos de preguntas admiten emails nulos
                                //Toast.makeText(this, "Ingresaste !", Toast.LENGTH_SHORT).show()
                                showHome(account.displayName ?: "", account.email)

                            } else {
                                Toast.makeText(this, "Error de Autenticacion 1", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            }catch (e: ApiException){

                Toast.makeText(this,"Error 2", Toast.LENGTH_SHORT).show()

                println(e.stackTraceToString())

                //Toast.makeText(this, "Error de Autenticacion 2", Toast.LENGTH_SHORT).show()
            }

        }
    }


    private fun fnClienteEntrar(){

        btnEntrarCliente.setOnClickListener {

            var email = editTextClienteEmailAddress.text.toString()
            var password = editTextClientePassword.text.toString()

            var esMailValido = validations.isValidEmail(email)

            if (email.isNotEmpty() && password.isNotEmpty()) {

                if (esMailValido == true) {

                    FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password).addOnCompleteListener{

                        if(it.isSuccessful){
                            //los simbolos de preguntas admiten emails nulos
                            showHome(it.result?.user?.email?:"", email)

                        }else{
                            Toast.makeText(this, "Error de Autenticacion", Toast.LENGTH_LONG).show()
                        }
                    }
                }else{
                    Toast.makeText(this, "Email Inválido, por favor ingresa un email válido", Toast.LENGTH_LONG).show()
                }
            }else{

                showAlert(email,password)
            }
        }
    }

    private fun showHome(nombre: String, email: String){
        val homeActivity = Intent(this,HomeActivity::class.java).apply {

            putExtra("nombre", nombre)
            putExtra("email", email)
        }
        startActivity(homeActivity)
    }



    private fun loadClienteRegistro(){
        btnRegistrarCliente.setOnClickListener(){
            showRegistroCliente()
        }
    }

    private fun showRegistroCliente(){
        //navegamos a la pantalla del Registro del Cliente
        val clienteRegistroActivity = Intent(this, AuthActivityClienteRegistro::class.java).apply {
        }
        startActivity(clienteRegistroActivity)
    }


    private fun forgotPasswordCliente(){
        btnForgotPasswordClient.setOnClickListener(){
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