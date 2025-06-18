package dev.hazar.hazarsheadlibrary.data

import kotlinx.serialization.Serializable

@Serializable
data class HeadData(
    val name: String,
    val category: String,
    val type: HeadType,
    val textureValue: String? = null,
    val playerName: String? = null,
    val uuidString: String? = null
)

