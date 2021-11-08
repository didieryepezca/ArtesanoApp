package com.example.artesano

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.artesano.recyclerviewsource.CategoryAdapter
import com.google.common.reflect.TypeToken
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.activity_carta_artesano.*
import kotlinx.android.synthetic.main.card_categorias_carta.*

class CartaActivityArtesano : AppCompatActivity()  {

    private val db = FirebaseFirestore.getInstance() //Instancia que conecta a la BD remota de Firebase.
    lateinit var progresoDialog : ProgressDialog

    var esArtesano : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_carta_artesano)

        progresoDialog = ProgressDialog(this)
        progresoDialog.setMessage("Cargando la carta...")
        progresoDialog.setCanceledOnTouchOutside(false)
        progresoDialog.show()

        var bundle = intent.extras
        var email: String? = bundle?.getString("email")

        //----------------------- comprobar si es artesano
        Firebase.remoteConfig.fetchAndActivate().addOnCompleteListener{ tarea ->
            if(tarea.isSuccessful){
                val gson = GsonBuilder().create()
                val json = FirebaseRemoteConfig.getInstance().getString("artesanos")
                val artesanos: List<ModelArtesano> =
                    gson.fromJson(json, object : TypeToken<List<ModelArtesano?>?>() {}.type)
                //si la configuracion remota contiene uno de los emails ingresados
                if(artesanos.contains(ModelArtesano(artesanoId = email!!))){
                    esArtesano = true
                    btnAddCategory.visibility = View.VISIBLE
                }else{
                    btnAddCategory.visibility = View.INVISIBLE
                }
            }
        }
        //----------------------------------------------------------------
        //Funcion para añadir categorias...
        fnAddCategory()

        //Funcion para traer los datos de Firestore y añadirlos a una Lista...
        db.collection("categorias").orderBy("category", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener {
                    document ->
                //println(document.documents.toString()); //logs correct data
                val categoriasLista: ArrayList<ModelCategoria> = ArrayList()

                for(i in document.documents){
                    val cat = i.toObject(ModelCategoria::class.java)!!
                    cat.category_id = i.id
                    categoriasLista.add(cat)
                }
                //pasamos la lista a la funcion que renderizara el RecyclerView...

                println(esArtesano)
                println("#si 'esArtesano' devuelve true muestra el botón editar categoria, sino NO.#")
                llenarCategoriasListaToUI(categoriasLista, esArtesano)

            }.addOnFailureListener {e ->
                //activity.hideProgressDialog();
                println("Error while creating a category") //logs correct data
            }
    }

    private fun llenarCategoriasListaToUI(categoriasLista : ArrayList<ModelCategoria>, isArtesano: Boolean){

        //levantamos el Recycler View
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val adaptador = CategoryAdapter(categoriasLista, this::categoryItemClickHandler,this::categoryEditClickHandler, isArtesano) //llamamos nuestra clase creada de com.example.artesano.recyclerviewsource

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adaptador
        //levantamos el Recycler View

        progresoDialog.hide()
    }

    private fun fnAddCategory(){

        btnAddCategory.setOnClickListener()
        {
            val addcategoryIntent = Intent(this, ActivityAddCategory::class.java)
            startActivity(addcategoryIntent)
        }
    }

    private fun categoryEditClickHandler(categoryId: String){

        btnEditCategoria.setOnClickListener() {

            Toast.makeText(this, "Has elegido: ${categoryId}", Toast.LENGTH_SHORT).show()
            val editCategoryIntent = Intent(this,ActivityAddCategory::class.java).apply {

                putExtra("categoryId", categoryId)
            }
            startActivity(editCategoryIntent)
        }
    }

    //Navegamos a la pantalla de ver platos pasandole la posicion como id de Categoria.
    private fun categoryItemClickHandler(category_name: String, categoria_id: String, fotoURL: String){

        Toast.makeText(this, "Has elegido: ${category_name}", Toast.LENGTH_SHORT).show()

        //creamos nuestra navegacion a la pantalla Platos, pasandole los datos de la posicion clickeada, categoria y URL de la foto.
        val viewPlatosIntent = Intent(this, CartaDetailActivityArtesano::class.java).apply{
                putExtra("category_name", category_name)
                putExtra("categoria_id", categoria_id)
                putExtra("fotoURL", fotoURL)

                putExtra("isArtesano", esArtesano)
            }
        startActivity(viewPlatosIntent)
    }

}