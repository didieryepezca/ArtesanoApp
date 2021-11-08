package com.example.artesano.recyclerviewsource

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.artesano.ModelCategoria
import com.example.artesano.R
import com.squareup.picasso.Picasso

//Clase que servira como adaptador de nuestra Lista optimizada (RecyclerView)
//agregamos entre parentesis de CustomAdapter 2 parametros, para listar nuestro ModelCategoria y
// itemClickHandler para saber cual se ha pulsado.
class CategoryAdapter(private val lista: ArrayList<ModelCategoria>
                                ,val itemClickHandler: (category_name: String,categoria: String,fotoURL: String) -> Unit
                                ,val itemEditClickHandler: (categoryId: String) -> Unit
                                ,private val isArtesano: Boolean):
    RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    //-----------valores estaticos para ejemplo.
    //val titles = arrayOf("Categoria 1", "Categoria 2", "Categoria 3", "Categoria 4", "Categoria 5")
    //val details = arrayOf("Descripcion 1", "Descripcion 2", "Descripcion 3", "Descripcion 4", "Descripcion 5")
    //val images = intArrayOf(R.drawable.client_icon, R.drawable.client_phone_icon, R.drawable.history, R.drawable.client_phone_icon, R.drawable.history)

    //metodo cuando entra primero, la i es la posicion de cada elemento
    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): CategoryViewHolder {
        //le indicamos cual sera el Layout que al iniciar inflaremos.
        val v = LayoutInflater.from(viewGroup.context).inflate(R.layout.card_categorias_carta, viewGroup, false)

        return CategoryViewHolder(v)
    }
    //metodo cuando ya llena con datos cada elemento.
    override fun onBindViewHolder(viewHolder: CategoryViewHolder, i: Int) {

        //println(i)
        //---------utilizando los valores estaticos que le pasamos arriba.
        //viewHolder.itemImage.setImageResource(images[i])
        //viewHolder.itemTitle.text = titles[i]
        //viewHolder.itemDetail.text = details[i]
        //---------utilizando los valores estaticos que le pasamos arriba.
        val model = lista[i]; //i es la posicion

        Picasso.get().load(model.image_url).into(viewHolder.itemImage)
        viewHolder.itemTitle.text = model.category
        viewHolder.itemDetail.text = model.category_description

        //--------------- para obtener el valor de la posicion que se clikeo
        viewHolder.itemView.setOnClickListener(){
            itemClickHandler.invoke(model.category.toString(),model.category_id.toString(),model.image_url.toString())
        }
        viewHolder.itemBtnEditCategory.setOnClickListener(){
            itemEditClickHandler.invoke(model.category_id.toString())
        }
    }
    //metodo que contiene el tamaño del arreglo que le pasaremos
    override fun getItemCount(): Int {
        //obtenemos el tamaño de la lista que le hemos pasado a la clase...
        return lista.size
    }
    inner class CategoryViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        var itemImage: ImageView
        var itemTitle: TextView
        var itemDetail: TextView

        var itemBtnEditCategory: Button

        init {
            itemImage = itemView.findViewById(R.id.item_card_image)
            itemTitle = itemView.findViewById(R.id.item_card_tittle)
            itemDetail = itemView.findViewById(R.id.item_card_description)
            itemBtnEditCategory = itemView.findViewById(R.id.btnEditCategoria)

            if(isArtesano){
                itemBtnEditCategory.visibility = View.VISIBLE
            }else{
                itemBtnEditCategory.visibility = View.INVISIBLE
            }
        }

    }
}