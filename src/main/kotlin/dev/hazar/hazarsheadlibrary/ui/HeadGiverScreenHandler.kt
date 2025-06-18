package dev.hazar.hazarsheadlibrary.ui



import com.google.gson.JsonParser
import com.mojang.serialization.JsonOps
import dev.hazar.hazarsheadlibrary.HazarsHeadLibrary.headList
import dev.hazar.hazarsheadlibrary.config.ConfigManager
import dev.hazar.hazarsheadlibrary.data.HeadData
import dev.hazar.hazarsheadlibrary.data.HeadType
import dev.hazar.hazarsheadlibrary.data.filterByName
import dev.hazar.hazarsheadlibrary.data.findSingleByName
import dev.hazar.hazarsheadlibrary.item.HeadStack
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import net.minecraft.component.DataComponentTypes
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.SimpleInventory
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.screen.GenericContainerScreenHandler
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.screen.SimpleNamedScreenHandlerFactory
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Formatting

internal enum class ScreenState {
    HOME,
    DISPLAY
}

internal class HeadGiverScreenHandler(
    syncId: Int,
    private val player: ServerPlayerEntity,
    private val searchQuery: String? = null
) : GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X6, syncId, player.inventory, SimpleInventory(9 * 6), 6) {

    private val payment = ConfigManager.config.useCurrency
    private val paymentItem = parseItemStackFromJson(ConfigManager.config.itemCurrencyJson)


    private val headsPerPage = 9 * 5
    private val allHeads: List<HeadData> = headList.toList() // static snapshot
    private var filteredHeads: List<HeadData> = allHeads
    private var screenState: ScreenState = ScreenState.HOME
    private var currentPage = 0

    private val today: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
    private val currentMonth: Int = today.monthNumber
    private val currentDay: Int = today.dayOfMonth

    init {
        if (!searchQuery.isNullOrBlank()) {
            applySearchResults(allHeads.filterByName(searchQuery))
        } else {
            renderHome()
        }

    }

    override fun canUse(player: PlayerEntity): Boolean = true

    override fun onSlotClick(slotIndex: Int, button: Int, actionType: SlotActionType?, player: PlayerEntity?) {
        when (screenState) {
            ScreenState.HOME -> {
                when (slotIndex) {
                    21 -> switchToFilter(HeadType.Player)
                    23 -> switchToFilter(HeadType.Custom)
                    31 -> switchToSearchMode()

                }
            }

            ScreenState.DISPLAY -> {
                when (slotIndex) {
                    in 0..44 -> {
                        val index = currentPage * headsPerPage + slotIndex
                        val head = filteredHeads.getOrNull(index)
                        if (head != null && player is ServerPlayerEntity) {
                            HeadStack.createHeadAsync(head = head, player = player, onReady = {
                                player.inventory.offerOrDrop(paymentItem.copy())
                                if (payment) {
                                    if(player.inventory.contains(paymentItem)){
                                        player.inventory.removeStack(
                                            player.inventory.indexOf(paymentItem),
                                            ConfigManager.config.currencyAmount
                                        )
                                    } else {
                                        player.sendMessage(Text.literal("You need ${paymentItem.count} ${paymentItem.name.string} to buy this head.").formatted(Formatting.RED))
                                        return@createHeadAsync
                                    }
                                }
                                player.giveItemStack(it)
                            }, onError = {
                                player.sendMessage(it)
                            })
                        }
                    }

                    45 -> prevPage()
                    48 -> renderHome()
                    53 -> nextPage()
                }
            }
        }
    }

    private fun switchToFilter(type: HeadType) {
        screenState = ScreenState.DISPLAY
        currentPage = 0
        filteredHeads = when (type) {
            HeadType.Player -> allHeads.filter { it.type == HeadType.Player || it.type == HeadType.ImportedPlayer }
            HeadType.Custom -> allHeads.filter { it.type == HeadType.Custom || it.type == HeadType.ImportedCustom }
            else -> allHeads.filter { it.type == type }
        }
        renderDisplay()
    }

    fun applySearchResults(results: List<HeadData>) {
        screenState = ScreenState.DISPLAY
        currentPage = 0
        filteredHeads = results
        renderDisplay()
    }

    private fun nextPage() {
        if ((currentPage + 1) * headsPerPage < filteredHeads.size) {
            currentPage++
            renderDisplay()
        }
    }

    private fun prevPage() {
        if (currentPage > 0) {
            currentPage--
            renderDisplay()
        }
    }

    private fun renderHome() {
        screenState = ScreenState.HOME
        inventory.clear()
        filteredHeads = allHeads

        makeCategoryHeadAsync(21, "Player", allHeads.findSingleByName("Dragonwhisper92"))
        makeCategoryHeadAsync(23, "Custom", allHeads.findSingleByName("Monitor"))
        makeCategoryHeadAsync(31, "Search", allHeads.findSingleByName(getSeasonalSearchIcon()))

    }

    private fun getSeasonalSearchIcon(): String = when {
        isPride() -> "Books (rainbow)"
        isHalloweenSeason() -> "Jack O'Lantern Books"
        isXmasSeason() -> "Christmas Tree Books"
        else -> "Books"
    }

    private fun renderDisplay() {
        inventory.clear()

        filteredHeads.drop(currentPage * headsPerPage).take(headsPerPage).forEachIndexed { i, head ->
            inventory.setStack(i, ItemStack(Items.BARRIER).apply {
                set(DataComponentTypes.CUSTOM_NAME, Text.literal("Loading...").formatted(Formatting.GRAY))
            })
            HeadStack.createHeadAsync(head = head, player = player, onReady = {
                inventory.setStack(i, it)
            }, onError = {
                inventory.setStack(i, ItemStack(Items.BARRIER).apply {
                    set(DataComponentTypes.CUSTOM_NAME, Text.literal("Failed").formatted(Formatting.RED))
                })
            })
        }

        inventory.setStack(45, makeNavButton("Prev"))
        inventory.setStack(48, makeNavButton("Home"))
        inventory.setStack(50, makePageDisplay(currentPage + 1))
        inventory.setStack(53, makeNavButton("Next"))
    }

    private fun makeCategoryHeadAsync(slot: Int, label: String, head: HeadData?) {
        if (head != null) {
            HeadStack.createHeadAsync(head = head, player = player, onReady = { itemStack ->
                itemStack.set(
                    DataComponentTypes.CUSTOM_NAME,
                    Text.literal(label).formatted(Formatting.DARK_AQUA, Formatting.BOLD)
                )
                itemStack.remove(DataComponentTypes.LORE)
                inventory.setStack(slot, itemStack)
            }, onError = {
                inventory.setStack(slot, ItemStack(Items.BOOK).apply {
                    set(DataComponentTypes.CUSTOM_NAME, Text.literal("Failed to load $label").formatted(Formatting.RED))
                })
            })
        } else {
            inventory.setStack(slot, ItemStack(Items.BOOK).apply {
                set(DataComponentTypes.CUSTOM_NAME, Text.literal(label).formatted(Formatting.GRAY))
            })
        }
    }

    private fun makeNavButton(name: String): ItemStack {
        val item = when (name.lowercase()) {
            "prev" -> Items.ARROW
            "next" -> Items.ARROW
            "home" -> Items.BARRIER
            else -> Items.STONE_BUTTON
        }
        return ItemStack(item).apply {
            set(DataComponentTypes.CUSTOM_NAME, Text.literal(name).formatted(Formatting.YELLOW))
        }
    }

    private fun makePageDisplay(page: Int): ItemStack {
        return ItemStack(Items.PAPER).apply {
            set(DataComponentTypes.CUSTOM_NAME, Text.literal("Page $page").formatted(Formatting.GOLD))
        }
    }

    private fun isPride(): Boolean = currentMonth == 6
    private fun isHalloweenSeason(): Boolean = currentMonth == 10 || (currentMonth == 11 && currentDay <= 7)
    private fun isXmasSeason(): Boolean = currentMonth == 12

    private fun switchToSearchMode() {
        player.openHandledScreen(SimpleNamedScreenHandlerFactory({ syncId, _, _ ->
            HeadSearchScreenHandler(syncId, player)
        }, Text.literal("Search Heads")))
    }


    private fun parseItemStackFromJson(jsonString: String?): ItemStack {
        if (jsonString.isNullOrBlank()) return ItemStack(Items.EMERALD)

        return try {
            val jsonElement = JsonParser.parseString(jsonString)
            ItemStack.CODEC.parse(JsonOps.INSTANCE, jsonElement)
                .resultOrPartial { error -> println("ItemStack parse failed: $error") }
                .orElse(ItemStack(Items.EMERALD))
        } catch (e: Exception) {
            println("Exception while parsing ItemStack: ${e.message}")
            ItemStack(Items.EMERALD)
        }
    }






}
