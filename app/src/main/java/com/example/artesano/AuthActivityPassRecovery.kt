package com.example.artesano

import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_send_mail_recovery.*

class AuthActivityPassRecovery : AppCompatActivity() {

    val validations = SomeValidations() //llamamos a la clase que contiene las validaciones

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_mail_recovery)

        //Recuperar contraseña del Artesano con Email.
        recoveryPasswordWithMail()
    }


    private fun recoveryPasswordWithMail(){

        btnEnviarMail.setOnClickListener(){

            var email = editTextRecoveryMail.text.toString()

            var esMailValido = validations.isValidEmail(email)

            if(esMailValido == true){
                Firebase.auth.sendPasswordResetEmail(email).addOnCompleteListener(){ tarea ->
                    if(tarea.isSuccessful){
                        Toast.makeText(this, "Hemos enviado un mail para cambiar tu clave.", Toast.LENGTH_LONG).show()
                    }else
                    {
                        Toast.makeText(this, "Tal vez no estés registrado.", Toast.LENGTH_LONG).show()
                    }
                }
            }else{
                Toast.makeText(this, "Email Inválido, por favor ingresa un email válido", Toast.LENGTH_LONG).show()
            }
        }
    }

}