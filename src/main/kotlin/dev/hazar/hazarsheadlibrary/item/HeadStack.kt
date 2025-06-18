package dev.hazar.hazarsheadlibrary.item

import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import dev.hazar.hazarsheadlibrary.data.HeadData
import dev.hazar.hazarsheadlibrary.data.HeadType
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

    /**
     * Creates a player head with optional custom texture, player info, or full HeadData.
     */
    fun createHead(
        head: HeadData? = null,
        playerNameOverride: String? = null,
        uuidOverride: UUID? = null,
        textureOverride: String? = null,
        player: PlayerEntity? = null,
        onError: ((Text) -> Unit)? = null
    ): ItemStack {
        val stack = ItemStack(Items.PLAYER_HEAD)

        // Extract values from HeadData, or use overrides
        val type = head?.type
        val texture = textureOverride ?: head?.textureValue
        val playerName = playerNameOverride ?: head?.playerName
        val uuid = uuidOverride ?: head?.uuidString?.let {
            runCatching { UUID.fromString(it) }.getOrNull()
        }

        when {
            !playerName.isNullOrBlank() -> {
                SkullBlockEntity.fetchProfileByName(playerName).get().ifPresent {
                    stack.set(DataComponentTypes.PROFILE, ProfileComponent(it))
                }
            }

            uuid != null -> {
                SkullBlockEntity.fetchProfileByUuid(uuid).get().ifPresent {
                    stack.set(DataComponentTypes.PROFILE, ProfileComponent(it))
                }
            }

            !texture.isNullOrBlank() -> {
                val profile = GameProfile(UUID.randomUUID(), "TextureOnly")
                profile.properties.put("textures", Property("textures", texture))
                stack.set(DataComponentTypes.PROFILE, ProfileComponent(profile))
            }

            player != null -> {
                stack.set(DataComponentTypes.PROFILE, ProfileComponent(player.gameProfile))
            }

            else -> {
                stack.set(
                    DataComponentTypes.CUSTOM_NAME,
                    Text.literal("No texture or player").formatted(Formatting.RED)
                )
                onError?.invoke(Text.literal("Missing texture or player name"))
            }
        }

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

        return stack
    }
}
