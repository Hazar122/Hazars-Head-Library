package dev.hazar.hazarsheadlibrary.data

import kotlinx.serialization.Serializable

@Serializable
enum class HeadType(val label: String) {
    Player("Player"),
    Custom("Custom"),
    ImportedPlayer("Player"),
    ImportedCustom("Custom");
}