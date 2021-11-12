package com.example.artesano

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.res.Configuration

import android.os.Build
import android.os.Bundle
import android.view.View

import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.artesano.recyclerviewsource.PlatosAdapter
import com.google.common.reflect.TypeToken
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.GsonBuilder

import com.squareup.picasso.Picasso

import kotlinx.android.synthetic.main.activity_carta_detail_artesano.*
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class CartaDetailActivityArtesano : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance() //Instancia que conecta a la BD remota de Firebase.
    lateinit var progresoDialog : ProgressDialog

    var category_name : String? = null
    var categoria_id : String? = null
    var fotoURL : String? = null

    var esArtesano: Boolean? = false

    val userPedido : String? = FirebaseAuth.getInstance().currentUser?.email

    var pedidoKey: String? = null
    var pedidoItemKey: String = ""

    var listaMutable : MutableList<ModelPedidoDetalle> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_carta_detail_artesano)

        //creamos funcion que se ejecutara con la carga. los bundles son para obtener los datos que vienen de la otra pantalla.
        var bundle = intent.extras

        category_name = bundle?.getString("category_name")
        categoria_id = bundle?.getString("categoria_id")
        fotoURL = bundle?.getString("fotoURL")

        esArtesano = bundle?.getBoolean("isArtesano")

        progresoDialog = ProgressDialog(this)
        progresoDialog.setMessage("Cargando los platos...")
        progresoDialog.setCanceledOnTouchOutside(false)
        progresoDialog.show()

        //realizamos los ajustes respecto a los botones principales.
        if(esArtesano == true){
            btnCart.visibility = View.INVISIBLE
        }else{
            btnAddPlato.visibility = View. INVISIBLE
        }

        //Guardar la URL de foto de la portada
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit() //obtenemos el string creado en "strings.xml"
        prefs.putString("image_category_url", fotoURL)
        prefs.apply() //aplicamos los cambios a los strings


        //Funcion para setear la portada con foto y descripcion de la categoria donde se encuentra.
        fnSetupCategoriaPlatos(category_name?:"",categoria_id?:"",fotoURL?:"")

        db.collection("platos").whereEqualTo("category_key", categoria_id?:"").get().addOnSuccessListener {
            document ->
           val platosLista: ArrayList<ModelPlato> = ArrayList();

            for(i in document.documents){
                val plat = i.toObject(ModelPlato::class.java)!!
                plat.plato_id = i.id
                platosLista.add(plat)
            }
            llenarPlatosListaToUI(platosLista, esArtesano!!)
            //println(platosLista)
        }.addOnFailureListener { e ->
            println("Error while creating a platos")
        }

        try{
            listaMutable = getPedidosListFromSharedPreferences() //obtenemos la lista de pedidos almacenados en SharedPreferences
        }catch (e: NullPointerException){
            println(e.message)
        }

        nullCartAlertOrShowItemsInCart() //Comprobamos y seteamos los botones para ver el carrito pedido.
    }

    //Método que se ejecuta cuando se ejecuta onBackPressed()
    override fun onResume() {
        super.onResume()

        //Funcion para setear la portada con foto y descripcion de la categoria donde se encuentra.
        fnSetupCategoriaPlatos(category_name?:"",categoria_id?:"",fotoURL?:"")
        db.collection("platos").whereEqualTo("category_key", categoria_id?:"").get().addOnSuccessListener {
                document ->
            val platosLista: ArrayList<ModelPlato> = ArrayList();

            for(i in document.documents){
                val plat = i.toObject(ModelPlato::class.java)!!
                plat.plato_id = i.id
                platosLista.add(plat)
            }
            llenarPlatosListaToUI(platosLista, esArtesano!!)
            //println(platosLista)
        }.addOnFailureListener { e ->
            println("Error while creating a platos")
        }
        nullCartAlertOrShowItemsInCart() //Comprobamos y seteamos los botones para ver el carrito pedido.
    }

    private fun llenarPlatosListaToUI(platosLista: ArrayList<ModelPlato>, isArtesano: Boolean){

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewPlatos)
        val adaptador = PlatosAdapter(platosLista, this::platoEditItemClickHandler, isArtesano, this::platoClickHandler)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adaptador

        progresoDialog.hide()
    }

    private fun platoEditItemClickHandler(PlatoId: String, platoClicked: String){
        Toast.makeText(this, "Elegiste: ${PlatoId}", Toast.LENGTH_SHORT).show()
        val editPlatoIntent = Intent(this, ActivityAddPlato::class.java).apply {
            putExtra("PlatoId", PlatoId)
            putExtra("platoClicked", platoClicked)
        }
        startActivity(editPlatoIntent)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun platoClickHandler(platoNombre: String, plato_foto: String, precio: Int, disp: String, platoId: String){

        var editor = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()

        Toast.makeText(this, "Se añadió: ${platoNombre}" + " al carrito", Toast.LENGTH_SHORT).show()

        //val peruZone = ZoneId.of("America/Lima")
        //val peruCurrentDateTime = ZonedDateTime.now(peruZone)
        //val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
        //var fecha_hora: String =  peruCurrentDateTime.format(formatter)
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val pedido_key = prefs.getString("pedido_key",null)

        if(pedido_key.isNullOrEmpty()){

            pedidoKey = UUID.randomUUID().toString()
            editor.putString("pedido_key",pedidoKey)

        }else{
            pedidoKey = prefs.getString("pedido_key",null)
        }

        //id unico de cada elemento del pedido
        pedidoItemKey = UUID.randomUUID().toString()

        var pedidoDetail = ModelPedidoDetalle()
        var itemEncontrado = listaMutable?.find { it.plato_id == platoId }

        if(itemEncontrado != null){

            itemEncontrado?.cantidad = itemEncontrado?.cantidad!! + 1
            itemEncontrado?.semitotal = itemEncontrado?.precio!! * itemEncontrado?.cantidad!!

        }else if(disp != "No Disponible"){

            pedidoDetail.item_key = pedidoItemKey
            pedidoDetail.pedido_key_detalle = pedidoKey
            pedidoDetail.plato_id = platoId
            pedidoDetail.nombre = platoNombre
            pedidoDetail.image = plato_foto
            pedidoDetail.precio = precio
            pedidoDetail.cantidad = 1
            pedidoDetail.semitotal = pedidoDetail.precio!!

            listaMutable.add(pedidoDetail)
        } else {

            var msjito = AlertDialog.Builder(this)
            msjito.setTitle("Atención")
            msjito.setMessage("El plato que has elegido no está disponible en estos momentos.")
            msjito.setIcon(android.R.drawable.ic_dialog_alert)
            msjito.setPositiveButton("OK") { dialogInterface, which ->
            }
            val alertDialog: AlertDialog = msjito.create()
            alertDialog.show()
        }

        //----------------- Guardamos la lista en Shared Preferences para recuperarla desde cualquier parte
        var gson = GsonBuilder().create()
        var json = gson.toJson(listaMutable)

        editor.putString("usuario",userPedido.toString())
        editor.putString("estado","POR CONFIRMAR")
        editor.putString("PERSISTANT_PEDIDOS_LIST",json)
        //editor.commit()
        editor.apply()

        btnCart.setOnClickListener(){
            val viewCartIntent = Intent( this,ViewCartActivity::class.java).apply {

                putExtra("listaPedidos", ArrayList(getPedidosListFromSharedPreferences()))
            }
            startActivity(viewCartIntent)
        }
    }

    fun getPedidosListFromSharedPreferences():ArrayList<ModelPedidoDetalle>{

        var preferences = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        var gson = GsonBuilder().create()
        var json:String? = preferences.getString("PERSISTANT_PEDIDOS_LIST",null)
        var type = object : TypeToken<ArrayList<ModelPedidoDetalle>>(){}.type//convertimos json to lista
        return gson.fromJson(json,type)//retorno la lista de Pedidos decodificada
    }

    private fun nullCartAlertOrShowItemsInCart() {
        btnCart.setOnClickListener(){
            if(listaMutable.count() < 1){
                val builder = AlertDialog.Builder(this)
                //set title for alert dialog
                builder.setTitle("Leeme")
                //set message for alert dialog
                builder.setMessage("Tu carrito está vacio, presiona en un plato para agregarlo a tu carrito")
                builder.setIcon(android.R.drawable.ic_dialog_alert)

                //performing positive action
                builder.setPositiveButton("Está bien") { dialogInterface, which ->
                    Toast.makeText(applicationContext, "Presiona en un plato", Toast.LENGTH_LONG).show()
                }
                val alertDialog: AlertDialog = builder.create()
                alertDialog.show()
            }else{
                btnCart.setOnClickListener(){
                    val viewCartIntent = Intent( this,ViewCartActivity::class.java).apply {

                        putExtra("listaPedidos", ArrayList(getPedidosListFromSharedPreferences()))
                    }
                    startActivity(viewCartIntent)
                }
            }
        }
    }

    private fun fnSetupCategoriaPlatos(categoriaName: String, categoriaId:String, fotoURL: String){
        //Cuando volvemos de la pantalla de añadir platos, obtenemos la url de la imagen de la portada almacenada en preferencias,
        //sino la añadimos de la pantalla inicial de categorias.
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE) //obtenemos el string creado en "strings.xml"
        val photo_saved_URL:String? = prefs.getString("image_category_url",null)

        val imagePortada = imgViewPlatosPortada

        if(fotoURL.isNullOrEmpty()){
            Picasso.get().load(photo_saved_URL).into(imagePortada)
        }else{
            Picasso.get().load(fotoURL).into(imagePortada)
        }
        toolBarTxt.title = categoriaName
        //Funcion cuando carga, para añadir los platos nuevos con el boton flotante.
        fnAddPlato(categoriaId, categoriaName)
    }

    private fun fnAddPlato(categoriaId:String, categoriaName: String){
        btnAddPlato.setOnClickListener() {
            val addPlatoIntent = Intent(this, ActivityAddPlato::class.java).apply {
                putExtra("categoriaId", categoriaId)
                putExtra("categoriaName", categoriaName)
            }
            startActivity(addPlatoIntent)
        }
    }
}


