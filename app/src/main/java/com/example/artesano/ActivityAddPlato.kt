package com.example.artesano

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toFile
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_add_plato.*
import kotlinx.android.synthetic.main.card_plato_carta.*
import java.io.File
import java.util.*

class ActivityAddPlato : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance() //Instancia que conecta a la BD remota de Firebase.
    //Inicializamos imagen de la categoria
    lateinit var platoImage : ImageView

    private val GALLERY_PICK = 1 // Para capturar la funcion onActivityResult
    lateinit var mProgressBar : ProgressDialog

    var posPlatoId: String = ""
    //-----------------------------------------
    var imageUri : Uri? = null
    var imageUrlFromBD : String? = null
    var categoryKey : String? = null
    var PlatoId : String? = null
    var catName : String? = null
    var plato_availability: String? = null // variable global que asignaremos cuando se ejecuta el evento seleccion del Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_plato)

        var bundle = intent.extras
        var categoria:String? = bundle?.getString("categoriaId")
        PlatoId = bundle?.getString("PlatoId")

        catName = bundle?.getString("categoriaName")

        if (PlatoId?.isNotEmpty() == true) {
            posPlatoId = PlatoId.toString()
            //println("------------- ES DISTINTO DE NULL")
        }else{
            var uniqueID = UUID.randomUUID().toString()
            posPlatoId = uniqueID
            categoryKey = categoria
            //println("------------- ES NULL")
        }

        editTextHiddenCategory.setText(categoria)

        var platoClicked:String? = bundle?.getString("platoClicked") //viene del plato seleccionado
        if(platoClicked?.isNotEmpty() == true){
            fnSetupEditPlato(posPlatoId)
            fnEliminarPlato(posPlatoId)
        }else{
            fnSetupAddPlato(catName?:"")
        }
        //-----------------------------------------ComboBox opciones Disponibilidad: Availability
        val spinnerOpcionesAvailability: Spinner = findViewById(R.id.spinnerAvailability)
        spinnerOpcionesAvailability.onItemSelectedListener = this.SpinnerActivity()
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(this, R.array.spinnerAvailabilityArray, android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinnera
            spinnerOpcionesAvailability.adapter = adapter
        }
        //-----------------------------------------
        platoImage = findViewById(R.id.platoImageView)
        platoImage.setOnClickListener(){
            Toast.makeText(this, "Seleccione imagen", Toast.LENGTH_SHORT).show()
            val galleryIntentPlato = Intent()
            galleryIntentPlato.type = "image/*"
            galleryIntentPlato.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(galleryIntentPlato, "SELECCIONE IMAGEN"), GALLERY_PICK)
        }
    }
    private fun fnSetupAddPlato(catName:String){
        btnEliminarPlato.visibility = View.INVISIBLE
        txtViewAddPlato.text = catName + ": Añadir plato"
    }

    private fun fnSetupEditPlato(positionPlatoId:String) {

        txtViewAddPlato.text = "Actualizar Plato"
        db.collection("platos").document(positionPlatoId).get().addOnSuccessListener {

            var indice_disp: Int
            var availabilityFromFs = it.get("availability") as String?

            if (availabilityFromFs == "Disponible") {
                indice_disp = 0
            } else {
                indice_disp = 1
            }
            imageUrlFromBD = it.get("plato_image_url") as String?

            Picasso.get().load(imageUrlFromBD).into(platoImage)
            editTextHiddenCategory.setText(it.get("category_key") as String?)
            //editTextPlatoPositionId.setText(it.get("plato_id") as String?)
            editTextPlatoName.setText(it.get("plato") as String?)
            editTextPlatoDescription.setText(it.get("description") as String?)
            editTextDeliveryTime.setText(it.get("delivery") as String?)
            editTextPrecio.setText(it.get("price").toString())
            spinnerAvailability.setSelection(indice_disp)

            //------------------------------ asignamos valores.
            PlatoId = posPlatoId
            categoryKey = editTextHiddenCategory.text.toString()
        }

        btnGuardarPlato.setOnClickListener() {
            if(editTextPlatoName.text.toString() != null &&
                editTextPlatoDescription.text.toString() != null &&
                editTextDeliveryTime.text.toString() != null &&
                editTextPrecio.text.toString() != null
                && plato_availability!!.isNotEmpty())
                {
                    uploadPlatoToFirebase(imageUri,PlatoId!!,categoryKey!!)
                    //Volvemos a la pantalla anterior y regargamos la pantalla anterior con el metodo super.onResume()

                }else{
                Toast.makeText(this, "Completa Todos los Campos", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun fnEliminarPlato(idPlatoDelete: String){
        btnEliminarPlato.setOnClickListener() {
            db.collection("platos").document(idPlatoDelete).delete().addOnSuccessListener {

                Toast.makeText(this, "Se eliminó el plato", Toast.LENGTH_LONG).show()
                onBackPressed()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == GALLERY_PICK && resultCode == Activity.RESULT_OK){
            Toast.makeText(this, "Seleccionaste una imágen !", Toast.LENGTH_LONG).show()
            imageUri = data?.data

            btnGuardarPlato.setOnClickListener() {
                if(editTextPlatoName.text.toString() != null &&
                    editTextPlatoDescription.text.toString() != null &&
                    editTextDeliveryTime.text.toString() != null &&
                    editTextPrecio.text.toString() != null && plato_availability!!.isNotEmpty()){

                    uploadPlatoToFirebase(imageUri,posPlatoId,categoryKey!!)

                }else{
                    Toast.makeText(this, "Completa Todos los Campos", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    private fun uploadPlatoToFirebase(fileUri: Uri?, platoId: String, categoryKey: String){

        mProgressBar = ProgressDialog(this)
        mProgressBar.setTitle("Guardando")
        mProgressBar.setMessage("Por favor, espere un momento...")
        mProgressBar.setCanceledOnTouchOutside(false)
        mProgressBar.show()

       var imageURL: String

       val fileName = editTextPlatoName.text.toString() + ".jpg"
       val refStorage = FirebaseStorage.getInstance().reference.child("platos/$fileName")

       //------------------si hubo cambios en la seleccion de la imagen
       if(fileUri != null){

           refStorage.putFile(fileUri).addOnSuccessListener(
               OnSuccessListener<UploadTask.TaskSnapshot> { task ->
               task.storage.downloadUrl.addOnSuccessListener {

                   imageURL = it.toString()

                   db.collection("platos").document(platoId).set(
                       hashMapOf("plato_image_url" to imageURL,
                               "plato_id" to platoId,
                               "plato" to editTextPlatoName.text.toString(),
                               "description" to editTextPlatoDescription.text.toString(),
                               "delivery" to editTextDeliveryTime.text.toString(),
                               "price" to editTextPrecio.text.toString().toInt(),
                               "availability" to plato_availability,
                               "category_key" to categoryKey)
                   )
                   val retrievedPhoto = File.createTempFile("TempImage", "jpg")
                   refStorage.getFile(retrievedPhoto).addOnSuccessListener {
                       val bitmapTemporal = BitmapFactory.decodeFile(retrievedPhoto.absolutePath)
                       platoImage.setImageBitmap(bitmapTemporal)
                   }

                   Thread.sleep(3000) //aguantamos unos segundos antes de volver a la pantalla anterior
                   mProgressBar.hide()
                   // Mostramos un mensajito Toast.
                   Toast.makeText(this, "Se ha guardado la información", Toast.LENGTH_LONG).show()

                   btnGuardarPlato.visibility = View.INVISIBLE
                   onBackPressed()

               }
           })?.addOnFailureListener( OnFailureListener { e ->
               println(e.message)
           })
           //------------------si NO hubo cambios en la seleccion de la imagen
       }else{
           db.collection("platos").document(platoId).set(
               hashMapOf("plato_image_url" to imageUrlFromBD,
                   "plato_id" to platoId,
                   "plato" to editTextPlatoName.text.toString(),
                   "description" to editTextPlatoDescription.text.toString(),
                   "delivery" to editTextDeliveryTime.text.toString(),
                   "price" to editTextPrecio.text.toString().toInt(),
                   "availability" to plato_availability,
                   "category_key" to categoryKey)
           ).addOnSuccessListener {
               Picasso.get().load(imageUrlFromBD).into(platoImage)
               Toast.makeText(this, "Se ha guardado la información", Toast.LENGTH_LONG).show()
               mProgressBar.hide()
            }
        }
    }

    //------------------Clase interna con evento para saber cual item eligio del combo box
    inner class SpinnerActivity : Activity(), AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
            // An item was selected. You can retrieve the selected item using
            var itemSelected : String = parent.getItemAtPosition(pos).toString()
            plato_availability = itemSelected // asignamos
            //println(plato_availability)
        }
        override fun onNothingSelected(parent: AdapterView<*>) {
            // Another interface callback
        }
    }
    //--------------------------------------------------------------------
}