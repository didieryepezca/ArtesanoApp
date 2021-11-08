package com.example.artesano

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

//@Parcelize y :Parcelable para poder pasarla de una pantalla a otra
@Parcelize
data class ModelPedidoDetalle (
    var item_key: String? = null,
    var pedido_key_detalle: String? = null,
    var plato_id: String? = null,
    var nombre: String? = null,
    var image: String? = null,
    var precio:  Int? = 0,
    var cantidad: Int? = 0,
    var semitotal: Int? = 0,
): Parcelable
