package com.example.artesano

import com.google.type.DateTime

data class ModelPedido(
    var pedido_key: String? = null,
    var usuario: String? = null,
    var fecha_hora: DateTime? = null,
    var estado: String? = null,
    var direccion: String? = null,
    var total: Int
)
