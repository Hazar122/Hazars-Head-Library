package dev.hazar.hazarsheadlibrary.luckperms

import net.fabricmc.loader.api.FabricLoader
import net.luckperms.api.LuckPerms
import net.luckperms.api.LuckPermsProvider
import net.minecraft.server.network.ServerPlayerEntity
import org.slf4j.LoggerFactory

internal object LuckPermsUtil {
    private const val PREFIX = "[HHL]: "
    private val LOGGER = LoggerFactory.getLogger("hazars-head-library")

    internal const val HEADDEX = "hhl.headdex.reload"
    internal const val HEADDEX_RELOAD = "$HEADDEX.reload"
    internal const val HEADDEX_INFO = "$HEADDEX.info"
    internal const val HEADDEX_UI = "$HEADDEX.ui"

    internal val isLuckPermsAvailable: Boolean by lazy {
        FabricLoader.getInstance().isModLoaded("luckperms").also { available ->
            if (available) {
                LOGGER.debug("$PREFIX LuckPerms is available.")
            } else {
                LOGGER.debug("$PREFIX LuckPerms is NOT available.")
            }
        }
    }

    internal fun getLuckPerms(): LuckPerms? {
        if (!isLuckPermsAvailable) return null
        return try {
            LuckPermsProvider.get()
        } catch (e: IllegalStateException) {
            null
        }
    }

    internal fun hasPermission(player: ServerPlayerEntity, node: String, fallbackLevel: Int = 2): Boolean {
        val lp = getLuckPerms()
        return if (lp != null) {
            lp.userManager.getUser(player.uuid)
                ?.cachedData
                ?.permissionData
                ?.checkPermission(node)
                ?.asBoolean() == true
        } else {
            player.hasPermissionLevel(fallbackLevel)
        }
    }
}