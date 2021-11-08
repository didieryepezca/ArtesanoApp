package com.example.artesano

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_cliente_update.*

class ActivityClienteDatos : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance() //Instancia que conecta a la BD remota de Firebase.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cliente_update)

        var bundle = intent.extras

        var email: String? = bundle?.getString("email")
        var nombresApellidos:String? = bundle?.getString("nombresApellidos")
        var direccion:String? = bundle?.getString("direccion")
        var celular:String? = bundle?.getString("celular")

        setupDataUpdate(email!!,nombresApellidos!!,direccion!!,celular!!)
    }

    private fun setupDataUpdate(email: String, nombresapellidos: String, direccion: String, celular: String){

        editTextEmailUpdate.setText(email)
        editTextNameSurnameUpdate.setText(nombresapellidos)
        editTextAddressUpdate.setText(direccion)
        editTextTelephoneUpdate.setText(celular)

        btnActualizarCliente.setOnClickListener(){

            db.collection("usuarios").document(email).set( //la clave primaria seria lo que va dentro de document()
                hashMapOf("nombresApellidos" to editTextNameSurnameUpdate.text.toString(),
                    "direccion" to editTextAddressUpdate.text.toString(),
                    "telefonoCelular" to editTextTelephoneUpdate.text.toString())
            ).addOnSuccessListener {

                Toast.makeText(this, "Se han guardado tus datos", Toast.LENGTH_LONG).show()
            }
        }
    }
}