package dev.hazar.hazarsheadlibrary.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import dev.hazar.hazarsheadlibrary.HazarsHeadLibrary.headListReady
import dev.hazar.hazarsheadlibrary.config.ConfigManager
import dev.hazar.hazarsheadlibrary.luckperms.LuckPermsUtil
import dev.hazar.hazarsheadlibrary.luckperms.LuckPermsUtil.hasPermission
import dev.hazar.hazarsheadlibrary.ui.HeadGiverScreenHandler
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.screen.SimpleNamedScreenHandlerFactory
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import org.slf4j.LoggerFactory

internal object HHLCommands {
    private const val PREFIX = "[HHL]: "
    private val LOGGER = LoggerFactory.getLogger("hazars-head-library")

    private fun br(count: Int = 25) = Text.literal("━".repeat(count)).formatted(Formatting.DARK_AQUA, Formatting.BOLD)

    private fun getSafePermissionLevel(configLevel: Int, fallback: Int): Int {
        return if (configLevel in 0..4) configLevel else {
            LOGGER.warn("$PREFIX Invalid permission level in config: $configLevel. Falling back to $fallback.")
            fallback
        }
    }

    private val commandBase: LiteralArgumentBuilder<ServerCommandSource> = CommandManager.literal("headdex")

    private val reloadCommand: LiteralArgumentBuilder<ServerCommandSource> =
        CommandManager.literal("reload")
            .requires { source ->
                val player = source.player
                hasPermission(
                    player!!,
                    LuckPermsUtil.HEADDEX_RELOAD,
                    fallbackLevel = getSafePermissionLevel(ConfigManager.config.permissionLevelCommandReload, 4)
                )
            }
            .executes { ctx ->
                ConfigManager.reloadConfig()
                ctx.source.sendFeedback({ Text.literal("Heads reloaded successfully!") }, true)
                1
            }

    private val infoCommand: LiteralArgumentBuilder<ServerCommandSource> =
        CommandManager.literal("info")
            .requires { source ->
                val player = source.player
                hasPermission(
                    player!!,
                    LuckPermsUtil.HEADDEX_INFO,
                    fallbackLevel = getSafePermissionLevel(ConfigManager.config.permissionLevelCommandInfo, 0)
                )
            }
            .executes { ctx ->
                val source = ctx.source
                val lines = mutableListOf<Text>()

                val version = FabricLoader.getInstance()
                    .getModContainer("hazars-head-library")
                    .map { it.metadata.version.friendlyString }
                    .orElse("unknown")

                lines += br()
                lines += Text.literal("Hlib Status Report:").formatted(Formatting.DARK_AQUA, Formatting.BOLD)
                lines += Text.literal("Mod Version: ").formatted(Formatting.GRAY)
                    .append(Text.literal(version).formatted(Formatting.GREEN))
                    .append(Text.literal(": for Fabric").formatted(Formatting.GREEN))
                lines += br()

                lines.forEach { line -> source.sendMessage(line) }

                1
            }

    private val uiCommand: LiteralArgumentBuilder<ServerCommandSource> =
        CommandManager.literal("ui")
            .requires { source ->
                val player = source.player
                hasPermission(
                    player!!,
                    LuckPermsUtil.HEADDEX_UI,
                    fallbackLevel = getSafePermissionLevel(ConfigManager.config.permissionLevelCommandUI, 2)
                )
            }
            .executes { ctx ->
                val player = ctx.source.player ?: return@executes 0

                val requiresCreative = ConfigManager.config.requiresCreative
                if (requiresCreative && !player.isCreative) {
                    player.sendMessage(Text.literal("This command requires Creative mode!").formatted(Formatting.RED))
                    return@executes 1
                }

                if (!headListReady) {
                    player.sendMessage(Text.literal("Hold up! Heads still loading...").formatted(Formatting.RED))
                    return@executes 1
                }

                player.openHandledScreen(
                    SimpleNamedScreenHandlerFactory(
                        { syncId, _, _ -> HeadGiverScreenHandler(syncId, player) },
                        Text.literal("Head Library").formatted(Formatting.DARK_AQUA)
                    )
                )

                1
            }

    internal fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        commandBase.then(reloadCommand)
        commandBase.then(infoCommand)
        commandBase.then(uiCommand)

        dispatcher.register(commandBase)
    }
}