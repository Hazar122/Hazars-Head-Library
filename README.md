# 🤯 Hazar's Head Library

A modern, Kotlin-powered Minecraft mod for collecting and giving custom heads!  

## 📦 Features

* 🔍 In-game searchable head index (Dex style!)
* 👥 Supports:
  * Vanilla player heads (via player name or UUID)
  * Custom texture heads (via base64 `textureValue`)
  * Custom player heads (via Player Entities)
* ⚡ Async profile fetching (non-blocking, smooth AF)
* 🛠️ Extensible data format (add your own heads via JSON)

## 🧪 How to Install

1. **Get the mods**:
   * Install [Fabric Loader](https://fabricmc.net/use/)
   * Drop in [Fabric API](https://modrinth.com/mod/fabric-api) and [Kotlin for Fabric](https://modrinth.com/mod/fabric-language-kotlin)
2. **Download this mod**:
   * Grab the right version from [Releases](https://github.com/Hazar122/Hazars-Head-Library/releases)
   * Use the right branch for your Minecraft version (e.g., `1.21.6`)
3. **Drop it in your `mods/` folder**
   * Like literally, just yeet it in there
4. **Launch Minecraft** with Fabric selected
   * If done right, you’ll see the `/heads` command ready in creative
## 🔮 How to Use (1.21.6)
### ✅ Prereqs
* Minecraft `1.21.6`
* [Fabric Loader](https://fabricmc.net/) + [Fabric API](https://modrinth.com/mod/fabric-api)
* `kotlin-language-fabric` enabled
* You must be in **Creative mode** to access the head menu!
### 🧠 In-Game Commands
```mcfunction
/heads
```
* Opens the searchable head UI.
* You can type a name like `bee`, `creeper`, or a player's name.
* Click the result to get a head delivered to your inventory.
### 🎨 Head Types
```kotlin
enum class HeadType(val label: String) {
    Player("Player"),
    Custom("Custom"),
    ImportedPlayer("Player"),
    ImportedCustom("Custom")
}
```
## 📚 Submitting New Heads
Wanna contribute a cool head texture? Create a **pull request** that edits the JSON file in `branches/main/HeadAPIData/heads.json`.
### ✅ JSON Format
```json
{
  "name": "Cute Bee",
  "category": "Mobs",
  "type": "Custom",
  "textureValue": "base64-encoded-texture-value"
}
```
You can also use `playerName` or `uuidString` if it's a player head:
```json
{
  "name": "Dream's Head",
  "category": "Creators",
  "type": "Player",
  "playerName": "Dream"
}
```
## 🌱 Versioning & Branches
Each Minecraft version is on its own branch:
* `1.21.6` → current stable version
* Older versions will live in branches like `1.20.1`, `1.19.4`, etc.
📌 Read the `README.md` in that branch for accurate usage info!
---
## 🛠 Dev API
Need to programmatically create a head?
### ✅ Async Fetch & Give
```kotlin
import dev.hazar.hazarsheadlibrary.item.HeadStack
import dev.hazar.hazarsheadlibrary.data.HeadData
import net.minecraft.server.network.ServerPlayerEntity

val head = HeadData(
    name = "Cool Pig",
    category = "Mobs",
    type = HeadType.Custom,
    textureValue = "base64..."
)

HeadStack.createHeadAsync(
    head = head,
    player = player,
    onReady = { stack ->
        player.giveItemStack(stack)
    },
    onError = { msg ->
        player.sendMessage(msg)
    }
)
```
💡 Works with texture-based heads, player names, UUIDs, playerEntities — all handled under the hood.

### 🧠 Working with `headList`
The mod maintains a global head registry:
```kotlin
val allHeads = HazarsHeadLibrary.headList
```
You can:
* Add custom heads: `headList.add(HeadData(...))`
* Clear and reset to default:
  ```kotlin
  headList.clear()
  headList.addAll(HazarsHeadLibrary.defaultHeadList)
  ```
### 🔍 Filtering
Use these extension methods from `HeadListExtension.kt`:
```kotlin
val creeperHeads = headList.filterByName("creeper")
val mobs = headList.filterByCategory("Mobs")
val customs = headList.filterByType(HeadType.Custom)
val one = headList.findSingleByName("Bee")
```
### 🌐 Loading Custom Heads
To fetch additional head sets:
```kotlin
modScope.launch {
    try {
        val moreHeads = fetchCustomHeadsFromUrl("https://example.com/my_heads.json")
        headList.addAll(moreHeads)
    } catch (e: Exception) {
        LOGGER.error("Failed to load custom heads: ${'$'}{e.message}")
    }
}
```
## 🤝 Credits
* Made with 💀 by Hazar.
---

> This README is for branch: `1.21.6`. Switch branches to view other version guides.
