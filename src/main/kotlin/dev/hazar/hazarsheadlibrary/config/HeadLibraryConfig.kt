package dev.hazar.hazarsheadlibrary.config

import com.google.gson.annotations.SerializedName

internal data class HeadLibraryConfig(
    @SerializedName("requiresCreative")
    val requiresCreative: Boolean = true,

    @SerializedName("useCurrency")
    val useCurrency: Boolean = false,
    
    @SerializedName("itemCurrencyJson")
    val itemCurrencyJson: String? = "{\"id\":\"minecraft:paper\",\"count\":1,\"components\":{\"minecraft:custom_model_data\":3,\"minecraft:custom_data\":{\"myTag\":1}}}",

    @SerializedName("currencyAmount")
    val currencyAmount: Int? = 1,

    @SerializedName("permissionLevelCommandReload")
    val permissionLevelCommandReload: Int = 4,

    @SerializedName("permissionLevelCommandInfo")
    val permissionLevelCommandInfo: Int = 0,

    @SerializedName("permissionLevelCommandUI")
    val permissionLevelCommandUI: Int = 2
)