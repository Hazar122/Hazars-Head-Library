package dev.hazar.hazarsheadlibrary.item

import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import dev.hazar.hazarsheadlibrary.data.HeadData
import net.minecraft.block.entity.SkullBlockEntity
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.LoreComponent
import net.minecraft.component.type.ProfileComponent
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.util.UUID

object HeadStack {

    fun createHeadAsync(
        head: HeadData? = null,
        playerNameOverride: String? = null,
        uuidOverride: UUID? = null,
        textureOverride: String? = null,
        player: PlayerEntity? = null,
        onReady: (ItemStack) -> Unit,
        onError: ((Text) -> Unit)? = null
    ) {
        val texture = textureOverride ?: head?.textureValue
        val playerName = playerNameOverride ?: head?.playerName
        val uuid = uuidOverride ?: head?.uuidString?.let { runCatching { UUID.fromString(it) }.getOrNull() }

        when {
            !playerName.isNullOrBlank() -> {
                SkullBlockEntity.fetchProfileByName(playerName).thenApplyAsync({ optProfile ->
                    val stack = ItemStack(Items.PLAYER_HEAD)
                    if (optProfile.isPresent) {
                        stack.set(DataComponentTypes.PROFILE, ProfileComponent(optProfile.get()))
                        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Head of ${optProfile.get().name}"))
                    } else {
                        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Unknown player").formatted(Formatting.RED))
                        onError?.invoke(Text.literal("⚠ Player with name '$playerName' not found"))
                    }
                    applyLore(head, stack)
                    onReady(stack)
                }, SkullBlockEntity.EXECUTOR)
            }

            uuid != null -> {
                SkullBlockEntity.fetchProfileByUuid(uuid).thenApplyAsync({ optProfile ->
                    val stack = ItemStack(Items.PLAYER_HEAD)
                    if (optProfile.isPresent) {
                        stack.set(DataComponentTypes.PROFILE, ProfileComponent(optProfile.get()))
                        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Head of ${optProfile.get().name}"))
                    } else {
                        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Unknown UUID").formatted(Formatting.RED))
                        onError?.invoke(Text.literal("⚠ Player with UUID '$uuid' not found"))
                    }
                    applyLore(head, stack)
                    onReady(stack)
                }, SkullBlockEntity.EXECUTOR)
            }

            !texture.isNullOrBlank() -> {
                val profile = GameProfile(UUID.randomUUID(), "TextureOnly")
                profile.properties.put("textures", Property("textures", texture))
                val stack = ItemStack(Items.PLAYER_HEAD)
                stack.set(DataComponentTypes.PROFILE, ProfileComponent(profile))
                stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(head?.name ?: "Custom Head"))
                applyLore(head, stack)
                onReady(stack)
            }

            player != null -> {
                val stack = ItemStack(Items.PLAYER_HEAD)
                stack.set(DataComponentTypes.PROFILE, ProfileComponent(player.gameProfile))
                stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Head of ${player.gameProfile.name}"))
                applyLore(head, stack)
                onReady(stack)
            }

            else -> {
                val stack = ItemStack(Items.PLAYER_HEAD)
                stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Missing texture or player").formatted(Formatting.RED))
                onError?.invoke(Text.literal("⚠ No valid texture, playerName, or UUID"))
                onReady(stack)
            }
        }
    }

    private fun applyLore(head: HeadData?, stack: ItemStack) {
        head?.let {
            stack.set(
                DataComponentTypes.CUSTOM_NAME,
                Text.literal(it.name).formatted(Formatting.DARK_AQUA, Formatting.BOLD)
            )
            stack.set(
                DataComponentTypes.LORE,
                LoreComponent(
                    listOf(
                        Text.literal("Category: ${it.category}").formatted(Formatting.GRAY),
                        Text.literal("Type: ${it.type.label}").formatted(Formatting.GRAY)
                    )
                )
            )
        }
    }

}
