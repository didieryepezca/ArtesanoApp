package com.example.artesano

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

import kotlinx.android.synthetic.main.activity_home.* // para acceder a los ids de los elementos.

class HomeActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance() //Instancia que conecta a la BD remota de Firebase.
    lateinit var mProgressBar : ProgressDialog

    var nombreapellidos: String? = null
    var direccion: String? = null
    var celular: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        //creamos funcion que se ejecutara con la carga. los bundles son para obtener los datos que vienen del Login
        var bundle = intent.extras
        var nombre:String? = bundle?.getString("nombre")
        var email: String? = bundle?.getString("email")

        homeSetup(nombre?:"", email?:"")

        //Guardar el usuario autenticado a nivel de app.
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit() //obtenemos el string creado en "strings.xml"
        prefs.putString("nombre", nombre)
        prefs.putString("email", email)

        prefs.apply() //aplicamos los cambios a los strings
    }

    private fun homeSetup(nombre: String, email: String){

        txtUserLoged.text = "Bienvenido(a) ${nombre} !"

        cardButtonLogOut.setOnClickListener(){

            //en el momento que presionamos CERRAR SESION se eliminan los datos temporales del usuario
            val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit() //obtenemos el string creado en "strings.xml"
            prefs.clear()
            prefs.apply()

            FirebaseAuth.getInstance().signOut()
            onBackPressed()// para volver a la pantalla anterior

            val authenticacion = Intent(this,AuthActivity::class.java).apply {

            }
            startActivity(authenticacion)

        }

        cardHomeMisDatos.setOnClickListener(){

            mProgressBar = ProgressDialog(this)
            mProgressBar.setTitle("Cargando")
            mProgressBar.setMessage("Recuperando tus datos...")
            mProgressBar.setCanceledOnTouchOutside(false)
            mProgressBar.show()

            db.collection("usuarios").document(email).get().addOnSuccessListener {
                nombreapellidos = it.get("nombresApellidos") as String?
                direccion = it.get("direccion") as String?
                celular = it.get("telefonoCelular") as String?
            }.addOnSuccessListener {

                val myDataIntent = Intent(this,  ActivityClienteDatos::class.java).apply {
                    putExtra("email", email)
                    putExtra("nombresApellidos", nombreapellidos)
                    putExtra("direccion", direccion)
                    putExtra("celular", celular)
                }
                mProgressBar.hide()
                startActivity(myDataIntent)
            }
        }

        cardMakeOrder.setOnClickListener(){

            val verCartaCategorias = Intent(this, CartaActivityArtesano::class.java).apply {
                putExtra("email", email)
            }
            startActivity(verCartaCategorias)

        }
    }

}