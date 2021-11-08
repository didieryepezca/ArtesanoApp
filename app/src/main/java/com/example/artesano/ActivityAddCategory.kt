package com.example.artesano

import android.app.Activity
import android.app.Dialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_add_category.*
import java.io.File
import java.util.*

class ActivityAddCategory: AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance() //Instancia que conecta a la BD remota de Firebase.

    //Inicializamos imagen de la categoria
    lateinit var categoryImage : ImageView
    //Firebase
    lateinit var mAuth : FirebaseAuth

    private val GALLERY_PICK = 1 // Para capturar la funcion onActivityResult
    lateinit var mProgressBar : ProgressDialog

    var categoryId: String? = null
    var imageCategoryUri : Uri? = null
    var imageCategoryUrlFromBD : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_category)

        var bundle = intent.extras
        categoryId = bundle?.getString("categoryId")

        if(categoryId?.isNotEmpty() == true){
            println("------------- ES DISTINTO DE NULL")
            fnSetupEditCategoria(categoryId!!)
        }else{
            var uniqueCategoryID = UUID.randomUUID().toString()
            categoryId = uniqueCategoryID
            println("------------- ES NULL")
            fnSetupAddCategory()
        }

        categoryImage = findViewById(R.id.categoryImageView)

        //Firebase
        //mAuth = FirebaseAuth.getInstance()
        //Firebase usuario logeado
        //val userId = mAuth.currentUser?.uid

        //capturamos el evento cuando hacemos click en la imagen por default.
        categoryImage.setOnClickListener(){

            Toast.makeText(this, "Seleccione imagen", Toast.LENGTH_SHORT).show()

            val galleryIntent = Intent()
            galleryIntent.type = "image/*"
            galleryIntent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(galleryIntent,"SELECCIONE IMAGEN"),GALLERY_PICK)
        }
    }

    private fun fnSetupAddCategory(){

        txtViewAddCategoria.text = "Añadir Nueva Categoria"
    }

    private fun fnSetupEditCategoria(category_id: String){

        txtViewAddCategoria.text = "Actualizar Categoria"
        //categoryId
        db.collection("categorias").document(category_id).get().addOnSuccessListener {

            imageCategoryUrlFromBD = it.get("image_url") as String?

            Picasso.get().load(imageCategoryUrlFromBD).into(categoryImage)
            editTextCategoryName.setText(it.get("category") as String?)
            editTextDescription.setText(it.get("category_description") as String?)
        }

        btnGuardarCategoria.setOnClickListener(){
            if(editTextCategoryName.text.toString() != null && editTextDescription.text.toString() != null){

                uploadCategoryToFirebase(imageCategoryUri,
                    editTextCategoryName.text.toString(),
                    editTextDescription.text.toString(),
                    category_id)
            }else{
                Toast.makeText(this, "Completa Todos los Campos", Toast.LENGTH_LONG).show()
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == GALLERY_PICK && resultCode == Activity.RESULT_OK){

            Toast.makeText(this, "Seleccionaste una imagen !", Toast.LENGTH_LONG).show()
            imageCategoryUri = data?.data

            btnGuardarCategoria.setOnClickListener() {

                var categoryName = editTextCategoryName.text.toString()
                var categoryDescription = editTextDescription.text.toString()

                if (imageCategoryUri != null && categoryName.isNotEmpty() && categoryDescription.isNotEmpty()) {
                    uploadCategoryToFirebase(imageCategoryUri,categoryName,categoryDescription, categoryId!!)
                }else{
                    Toast.makeText(this, "Completa el Nombre y la Descripcion", Toast.LENGTH_LONG).show()
                }
            }
        }else{
            Toast.makeText(this, "No has seleccionado una imagen", Toast.LENGTH_LONG).show()
        }
    }

    private fun uploadCategoryToFirebase(fileUri: Uri?, catName: String, catDesc: String, categoryid: String) {

        mProgressBar = ProgressDialog(this)
        mProgressBar.setTitle("Guardando")
        mProgressBar.setMessage("Por favor, espere un momento...")
        mProgressBar.setCanceledOnTouchOutside(false)
        mProgressBar.show()

        mAuth = FirebaseAuth.getInstance()
        //val userId = mAuth.currentUser?.uid

        val fileName = catName +".jpg"
        val refStorage = FirebaseStorage.getInstance().reference.child("categorias/$fileName")

        if (fileUri != null) {
            refStorage.putFile(fileUri)
                .addOnSuccessListener(
                    OnSuccessListener<UploadTask.TaskSnapshot> { taskSnapshot ->
                        taskSnapshot.storage.downloadUrl.addOnSuccessListener {

                            val imageUrl = it.toString() //url de la imagen
                            //val miniatura = taskSnapshot.storage.downloadUrl!!.toString()

                            //guardamos en Firestore los datos de la categoria con su imagen URL obtenida
                            db.collection("categorias").document(categoryid).set( //la clave primaria seria lo que va dentro de document()
                                hashMapOf("image_url" to imageUrl,
                                    "image_name" to fileName,
                                    "category_id" to categoryid,
                                    "category" to catName,
                                    "category_description" to catDesc)
                            )
                            //recuperamos la imagen para setearla en el Image View
                            val fotoRecuperada = File.createTempFile("TempImage", "jpg")
                            refStorage.getFile(fotoRecuperada).addOnSuccessListener {
                                val bitmapTemporal = BitmapFactory.decodeFile(fotoRecuperada.absolutePath)
                                categoryImage.setImageBitmap(bitmapTemporal)
                            }
                            //recuperamos la imagen para setearla en el Image View

                            // Ocultamos el mensajito de la carga cuando se completa
                            mProgressBar.hide()
                            // Mostramos un mensajito Toast.
                            Toast.makeText(this, "Se ha guardado la informacion", Toast.LENGTH_LONG).show()

                            //mostramos la pantalla de categorias.
                            showCategorias()
                        }
                    })?.addOnFailureListener(OnFailureListener { e ->
                    println(e.message)
                })
        }else{
            db.collection("categorias").document(categoryid).set( //la clave primaria seria lo que va dentro de document()
                hashMapOf("image_url" to imageCategoryUrlFromBD,
                    "image_name" to fileName,
                    "category_id" to categoryid,
                    "category" to catName,
                    "category_description" to catDesc)
            ).addOnSuccessListener {
                Picasso.get().load(imageCategoryUrlFromBD).into(categoryImage)
                Toast.makeText(this, "Se ha guardado la información", Toast.LENGTH_LONG).show()
                mProgressBar.hide()
            }
        }
    }

    private fun showCategorias(){
        val categoriasIntent = Intent(this, CartaActivityArtesano::class.java)
        startActivity(categoriasIntent)
    }



}