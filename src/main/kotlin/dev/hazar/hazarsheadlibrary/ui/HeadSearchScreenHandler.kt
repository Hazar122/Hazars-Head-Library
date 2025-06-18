package dev.hazar.hazarsheadlibrary.ui

import net.minecraft.component.DataComponentTypes
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.screen.AnvilScreenHandler
import net.minecraft.screen.SimpleNamedScreenHandlerFactory
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Formatting

internal class HeadSearchScreenHandler(
    syncId: Int,
    private val player: ServerPlayerEntity
) : AnvilScreenHandler(syncId, player.inventory) {

    init {
        val nameTag = ItemStack(Items.NAME_TAG)
        nameTag.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Search Heads").formatted(Formatting.DARK_AQUA, Formatting.BOLD))
        input.setStack(0, nameTag)
        output.setStack(0, nameTag.copy())
    }

    override fun onSlotClick(slotIndex: Int, button: Int, actionType: SlotActionType?, player: PlayerEntity) {
        if (slotIndex == 2) {
            val nameTag = output.getStack(0)
            val query = nameTag.name.string.trim()
            if (query.isNotEmpty()) {
                player.openHandledScreen(SimpleNamedScreenHandlerFactory({ syncId, _, _ ->
                    HeadGiverScreenHandler(syncId, player as ServerPlayerEntity, searchQuery = query)
                }, Text.literal("Head Dex").formatted(Formatting.GOLD)))
            }
        }
    }
}