package com.example.artesano

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.actionCodeSettings
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_auth_artesano.*
import kotlinx.android.synthetic.main.activity_auth_cliente_registro.*

class AuthActivityClienteRegistro : AppCompatActivity() {

    val validations = SomeValidations() //llamamos a la clase que contiene las validaciones

    private val db = FirebaseFirestore.getInstance() //Instancia que conecta a la BD remota de Firebase.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth_cliente_registro)

        //funcion para registrar un cliente con cualquier otro correo que no sea google.
        fnRegistroCliente()
    }


    private fun fnRegistroCliente(){

        btnGuardarCliente.setOnClickListener(){

            var nameSurname = editTextNameSurname.text.toString()
            var address = editTextAddress.text.toString()
            var cellular = editTextTelephone.text.toString()
            var email = editTextEmail.text.toString()
            var password = editTextPasswordClient.text.toString()

            var esMailValido = validations.isValidEmail(email)

            if (email.isNotEmpty() && password.isNotEmpty()){

                if(esMailValido == true) {
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(email ,password).addOnCompleteListener{
                        if(it.isSuccessful){
                            //enviar email con link de confirmacion de email(Falta configurar Dynamic Links)
                            /*val actionCodeSettings = actionCodeSettings {
                                 // URL you want to redirect back to. The domain (www.example.com) for this
                                 // URL must be whitelisted in the Firebase Console.
                                 url = "https://artesano-8a330.web.app/"
                                 // This must be true
                                 handleCodeInApp = true
                                 setIOSBundleId("com.example.ios")
                                 setAndroidPackageName(
                                     "com.example.artesano",
                                     true, /* installIfNotAvailable */
                                     "12" /* minimumVersion */)
                             }

                             FirebaseAuth.getInstance().sendSignInLinkToEmail(email,actionCodeSettings).addOnCompleteListener(){ tarea ->
                                 if(tarea.isSuccessful){
                                     Toast.makeText(this, "Enviamos email de confirmacion", Toast.LENGTH_LONG).show()
                                 }else{
                                     Toast.makeText(this, "No se pudo enviar email de confirmacion", Toast.LENGTH_LONG).show()
                                 }
                             }*/
                            //guardamos los datos del cliente en Firestore
                            db.collection("usuarios").document(email).set( //la clave primaria seria lo que va dentro de document()
                                hashMapOf("nombresApellidos" to nameSurname,
                                    "direccion" to address,
                                    "telefonoCelular" to cellular)
                            )

                            // mostramos la pantalla de inicio
                           showHome(nameSurname)
                        }else{
                            showAlert(email,password)
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

    private fun showHome(nombre: String){
        val homeActivity = Intent(this,HomeActivity::class.java).apply {

            putExtra("nombre", nombre)
        }
        startActivity(homeActivity)
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