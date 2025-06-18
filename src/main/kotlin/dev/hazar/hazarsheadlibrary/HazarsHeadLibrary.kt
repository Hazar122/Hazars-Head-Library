package dev.hazar.hazarsheadlibrary

import dev.hazar.hazarsheadlibrary.data.HeadData
import dev.hazar.hazarsheadlibrary.net.HeadFetcher.fetchDefaultHeads
import dev.hazar.hazarsheadlibrary.ui.HeadGiverScreenHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.screen.SimpleNamedScreenHandlerFactory
import net.minecraft.server.command.CommandManager
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import org.slf4j.LoggerFactory

object HazarsHeadLibrary : ModInitializer {
	private const val PREFIX = "[HHL]: "
	private val LOGGER = LoggerFactory.getLogger("hazars-head-library")

	private val modScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
	internal var headListReady = false
		private set

	private var _defaultHeadList: List<HeadData> = emptyList()
	val headList: MutableList<HeadData> = mutableListOf()

	val defaultHeadList: List<HeadData>
		get() = _defaultHeadList

	override fun onInitialize() {
		LOGGER.info("$PREFIX Initializing Hazars Head Library")

		modScope.launch {
			try {
				_defaultHeadList = fetchDefaultHeads()
				headList.clear()
				headList.addAll(_defaultHeadList)
				headListReady = true
				LOGGER.info("$PREFIX Loaded ${_defaultHeadList.size} default heads")
			} catch (e: Exception) {
				LOGGER.error("$PREFIX Failed to load default heads: ${e.message}")
			}
		}

		LOGGER.info("$PREFIX Registering commands")
		CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
			dispatcher.register(
				CommandManager.literal("heads")
					.executes { context ->
						val player = context.source.player
						if (player?.gameMode?.isCreative == true) {
							if (!headListReady) {
								player.sendMessage(Text.literal("Hold up! Heads still loading...").formatted(Formatting.RED))
								return@executes 1
							}
							player.openHandledScreen(
								SimpleNamedScreenHandlerFactory(
									{ syncId, _, _ ->
										HeadGiverScreenHandler(syncId, player)
									},
									Text.literal("Head Dex").formatted(Formatting.GOLD)
								)
							)
						}
						1
					}
			)
		}
	}
}