package com.example.artesano.recyclerviewsource

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.artesano.ModelPlato
import com.example.artesano.R
import com.makeramen.roundedimageview.RoundedImageView
import com.squareup.picasso.Picasso

class PlatosAdapter(private val lista: ArrayList<ModelPlato>,
                    val itemEditClickHandler: (PlatoId:String,plato: String)->Unit,
                    private val isArtesano: Boolean,
                    val itemClickHandler: (plato_nombre: String, plato_foto: String, precio: Int, disp: String, platoId: String)->Unit):

    RecyclerView.Adapter<PlatosAdapter.PlatosViewHolder>() {


    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): PlatosViewHolder {
        val p = LayoutInflater.from(viewGroup.context).inflate(R.layout.card_plato_carta, viewGroup, false)
        return PlatosViewHolder(p)
    }

    override fun onBindViewHolder(viewHolder: PlatosViewHolder, position: Int) {

        var model = lista[position]; //i es la posicion

        Picasso.get().load(model.plato_image_url).into(viewHolder.itemImage)
        viewHolder.itemPlatoName.text = model.plato
        viewHolder.itemPlatoDescription.text = model.description
        viewHolder.itemDeliveryTime.text = model.delivery
        viewHolder.itemPrice.text = model.price.toString()
        viewHolder.itemAvailability.text = model.availability

        //Para editar el plato
        viewHolder.itemBtnEditPlato.setOnClickListener() {
            itemEditClickHandler.invoke(model.plato_id.toString(),model.plato.toString())
        }

        //para entrar a realizar el pedido

        viewHolder.itemView.setOnClickListener(){
            itemClickHandler.invoke(model.plato.toString()
                , model.plato_image_url.toString()
                , model.price!!.toInt()
                ,model.availability.toString()
                ,model.plato_id.toString())
            }
    }

    override fun getItemCount(): Int {
        return lista.size
    }

    inner class PlatosViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){

        var itemImage: RoundedImageView
        var itemPlatoName: TextView
        var itemPlatoDescription: TextView
        var itemDeliveryTime: TextView
        var itemPrice: TextView
        var itemAvailability: TextView

        var itemBtnEditPlato: Button

        init{
            itemImage = itemView.findViewById(R.id.item_rounded_img_view)
            itemPlatoName = itemView.findViewById(R.id.txt_view_plato_name)
            itemPlatoDescription = itemView.findViewById(R.id.txt_view_plato_description)
            itemDeliveryTime = itemView.findViewById(R.id.delivery_time)
            itemPrice = itemView.findViewById(R.id.price)
            itemAvailability = itemView.findViewById(R.id.availability)

            itemBtnEditPlato = itemView.findViewById(R.id.btnEditPlato)

            if(isArtesano){
                itemBtnEditPlato.visibility = View.VISIBLE

            }else{
                itemBtnEditPlato.visibility = View.INVISIBLE
            }
        }
    }

}