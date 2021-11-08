package com.example.artesano

import com.google.gson.annotations.SerializedName

class ListPedidos {

    //var listaMutable : MutableList<ModelPedidoDetalle> = ArrayList()
    @SerializedName("listaPedidos") var listaMutable : MutableList<ModelPedidoDetalle> = ArrayList()
}