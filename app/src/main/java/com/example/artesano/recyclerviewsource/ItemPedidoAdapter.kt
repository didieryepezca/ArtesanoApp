package com.example.artesano.recyclerviewsource


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.artesano.ModelPedidoDetalle

import com.example.artesano.R
import com.squareup.picasso.Picasso

class ItemPedidoAdapter(private val listaPedidos: MutableList<ModelPedidoDetalle>,
                        val AddOrSubstractOrDeteleItem: (itemId: String, action: String, posicion: Int)->Unit):
    RecyclerView.Adapter<ItemPedidoAdapter.ItemPedidoViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ItemPedidoViewHolder {

        val i = LayoutInflater.from(viewGroup.context).inflate(R.layout.card_pedido_item, viewGroup, false)
        return ItemPedidoViewHolder(i)
    }

    override fun onBindViewHolder(viewHolder: ItemPedidoViewHolder, position: Int) {

        var model = listaPedidos[position]; //i es la posicion

        Picasso.get().load(model.image).into(viewHolder.itemPedidoImg)
        viewHolder.itemPedidoName.text = model.nombre
        viewHolder.itemPedidoPrice.text = model.semitotal.toString()
        viewHolder.itemPedidoQuantity.text = model.cantidad.toString()

            //cuando hacemos click en el botón menos
            viewHolder.itemMinusItem.setOnClickListener(){
                AddOrSubstractOrDeteleItem.invoke(model.item_key.toString(), "substract", position)
                //actualizamos los valores de la vista.
                viewHolder.itemPedidoQuantity.text = model.cantidad.toString()
                viewHolder.itemPedidoPrice.text = model.semitotal.toString()

            }
            //cuando hacemos click en el botón mas
            viewHolder.itemAddItem.setOnClickListener(){
                AddOrSubstractOrDeteleItem.invoke(model.item_key.toString(), "add", position)
                //actualizamos los valores de la vista.
                viewHolder.itemPedidoQuantity.text = model.cantidad.toString()
                viewHolder.itemPedidoPrice.text = model.semitotal.toString()
            }

            //cuando eliminamos un item
            viewHolder.itemDeleteItem.setOnClickListener(){
                AddOrSubstractOrDeteleItem.invoke(model.item_key.toString(), "delete", position)
            }
    }

    override fun getItemCount(): Int {
        return listaPedidos.size
    }

    inner class ItemPedidoViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){

        var itemPedidoImg: ImageView
        var itemPedidoName: TextView
        var itemPedidoQuantity: TextView
        var itemPedidoPrice: TextView
        var itemAddItem: ImageView
        var itemMinusItem: ImageView
        var itemDeleteItem : ImageView

        init{
            itemPedidoImg = itemView.findViewById(R.id.img_pedido_item)
            itemPedidoName = itemView.findViewById(R.id.txtItemNombre)
            itemPedidoQuantity = itemView.findViewById(R.id.txtItemQuantity)
            itemPedidoPrice = itemView.findViewById(R.id.txtItemPrice)
            itemAddItem = itemView.findViewById(R.id.btnMore)
            itemMinusItem = itemView.findViewById(R.id.btnMinus)
            itemDeleteItem = itemView.findViewById(R.id.btnDeleteItemPedido)
        }
    }
}