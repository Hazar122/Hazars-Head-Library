package dev.hazar.hazarsheadlibrary

import dev.hazar.hazarsheadlibrary.commands.HHLCommands
import dev.hazar.hazarsheadlibrary.config.ConfigManager
import dev.hazar.hazarsheadlibrary.data.HeadData
import dev.hazar.hazarsheadlibrary.net.HeadFetcher.fetchDefaultHeads
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
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

		ConfigManager.loadOrCreateConfig()

		LOGGER.info("$PREFIX Registering commands")
		CommandRegistrationCallback.EVENT.register { dispatcher, _, _ -> HHLCommands.register(dispatcher) }
	}
}