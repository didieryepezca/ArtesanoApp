package com.example.artesano

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.artesano.recyclerviewsource.ItemPedidoAdapter
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.activity_view_cart.*
import kotlinx.android.synthetic.main.card_pedido_item.*

class ViewCartActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance() //Instancia que conecta a la BD remota de Firebase.
    lateinit var progresoDialog : ProgressDialog
    var pedidosFromCarta: MutableList<ModelPedidoDetalle> = ArrayList()

    var pedido_key : String? = null
    var usuario : String? = null
    var estado : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_cart)

        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)

        pedido_key = prefs.getString("pedido_key",null)
        usuario = prefs.getString("usuario", null)
        estado = prefs.getString("estado", null)

        println("------------->")
        println(pedido_key)
        println(usuario)
        println(estado)

        pedidosFromCarta = intent.getParcelableArrayListExtra<ModelPedidoDetalle>("listaPedidos") as MutableList<ModelPedidoDetalle>

        progresoDialog = ProgressDialog(this)
        progresoDialog.setMessage("Cargando tu pedido...")
        progresoDialog.setCanceledOnTouchOutside(false)
        progresoDialog.show()

        llenarItemsPedidoToUI(pedidosFromCarta as MutableList<ModelPedidoDetalle>)

        confirmPedidoAddress()
    }

    private fun llenarItemsPedidoToUI(listaPedidos: MutableList<ModelPedidoDetalle>){

        val reciclerViewPedido = findViewById<RecyclerView>(R.id.recyclerViewCart)
        val adaptador = ItemPedidoAdapter(listaPedidos, this::AddOrSubstractOrDeteleItem)

        reciclerViewPedido.layoutManager = LinearLayoutManager(this)
        reciclerViewPedido.adapter = adaptador

        progresoDialog.hide()
    }

    private fun AddOrSubstractOrDeteleItem(itemId: String, action: String, posicion: Int){

            //Toast.makeText(this, "Elegiste: ${itemId}", Toast.LENGTH_SHORT).show()
            var itemClicked = pedidosFromCarta.find { it.item_key == itemId }

            if(action == "substract")
            {
                   if(itemClicked!!.cantidad!! > 1) {
                       itemClicked!!.cantidad = itemClicked!!.cantidad!! - 1
                       itemClicked!!.semitotal = itemClicked.precio!! * itemClicked.cantidad!!
                   }

            } else if (action == "add"){

                itemClicked!!.cantidad = itemClicked!!.cantidad!! + 1
                itemClicked!!.semitotal = itemClicked.precio!! * itemClicked.cantidad!!

            } else {
                pedidosFromCarta.removeAt(posicion)
                if(pedidosFromCarta.count()>0){
                    llenarItemsPedidoToUI(pedidosFromCarta)
                }else{
                    onBackPressed()
                }
            }

        savePedidoInSharedPreferences(pedidosFromCarta)
    }

    private fun savePedidoInSharedPreferences(jsonPedidos: List<ModelPedidoDetalle>){
        var gson = GsonBuilder().create()
        var json = gson.toJson(jsonPedidos)

        var editor = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
        editor.putString("PERSISTANT_PEDIDOS_LIST",json)
        //editor.commit()
        editor.apply()
    }

    private fun confirmPedidoAddress(){

        btnConfirmPedido.setOnClickListener(){

            val builder: AlertDialog.Builder = android.app.AlertDialog.Builder(this)
            builder.setTitle("Dirección")

            // Set up the input
            val input = EditText(this)
            // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
            input.setHint("Confirme la dirección de Entrega")
            input.inputType = InputType.TYPE_CLASS_TEXT
            builder.setView(input)

            builder.setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                    // Here you get get input text from the Edittext
                    var direccion = input.text.toString()
                    if(direccion.isNullOrEmpty()){
                        Toast.makeText(applicationContext, "Por favor ingrese una dirección", Toast.LENGTH_LONG).show()
                    }else{
                        progresoDialog = ProgressDialog(this)
                        progresoDialog.setMessage("Estamos enviando tu pedido...")
                        progresoDialog.setCanceledOnTouchOutside(false)
                        progresoDialog.show()

                        Thread.sleep(3000) // Aguantamos unos segundos para que se note que hace algo
                        realizarPedido(direccion)
                    }
            })
            builder.setNegativeButton("Cancelar", DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })
            builder.show()
        }
    }

    private fun realizarPedido(direccion: String){

        var precioTotal : Int = 0

        //Creamos el detalle del pedido
        for(itemPedido in pedidosFromCarta){

            db.collection("pedidos_detalle").document(itemPedido.item_key!!).set(
                hashMapOf(
                    "item_key" to itemPedido.item_key,
                    "pedido_key_detalle" to itemPedido.pedido_key_detalle,
                    "plato_id" to itemPedido.plato_id,
                    "nombre" to itemPedido.nombre,
                    "image" to itemPedido.image,
                    "precio" to itemPedido.precio,
                    "cantidad" to itemPedido.cantidad,
                    "semitotal" to itemPedido.semitotal
                )
            )
            precioTotal = precioTotal + itemPedido.semitotal!!
        }

        //Creamos el pedido general
        db.collection("pedidos").document(pedido_key!!).set(
            hashMapOf(
                "pedido_key" to pedido_key,
                "usuario" to usuario,
                "fecha_hora" to FieldValue.serverTimestamp(),
                "estado" to "POR CONFIRMAR",
                "direccion" to direccion,
                "total" to precioTotal
            )
        )
        //limpiamos temporales.
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit() //obtenemos el string creado en "strings.xml"
        prefs.clear()
        prefs.apply()

        progresoDialog.hide()

        val builder = AlertDialog.Builder(this)
        //set title for alert dialog
        builder.setTitle("¡ Gracias !")
        //set message for alert dialog
        builder.setMessage("Tu pedido ha sido enviado con éxito, haz seguimiento a tu pedido...")
        builder.setIcon(android.R.drawable.ic_dialog_info)

        //performing positive action
        builder.setPositiveButton("OK") { dialogInterface, which ->
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
    }
}