# 🤯 Hazar's Head Library

A modern, Kotlin-powered Minecraft mod for collecting and giving custom heads!  
Designed for Multiple Fabric Versions (will have more versions and launchers soon) with full async profile fetching and a snappy GUI.

## ✨ What It Does
- 🗂️ Browse categories of custom heads
- 👤 Player-based and custom texture-based support
- ⚡ Fully async and non-blocking
- 🎯 Designed for creative decor/dev tools

---

## 🎁 Adding Your Own Heads

We accept community head submissions!

1. Fork the repo.
2. Add your head(s) to `heads.json` (in the `main` branch).
3. Create a Pull Request with new heads (must follow the field Guide):
```
| Field          | Description                                                    |
| -------------- | -------------------------------------------------------------- |
| `name`         | Display name of the head                                       |
| `category`     | Category it shows up under (ex: Food, Mobs, etc.)              |
| `type`         | One of: `Custom`, `Player`                                     |
| -------------- | -------------------------------------------------------------- |
| PICK ONE       | -------------------------------------------------------------- |
| `textureValue` | Base64-encoded skin texture (for custom heads)                 |
| `playerName`   | Minecraft username (for player heads)                          |
| `uuidString`   | UUID string (for legacy/offline heads)                         |
```
Example head entry:

```json
{
  "name": "Bread Boi",
  "category": "Food",
  "type": "Custom",
  "textureValue": "base64-encoded-texture-value-here"
}
```

🙌 PRs are welcome! Keep it appropriate and unique.

🏷️ Game Versions
- All game-version-specific branches are named like: 1.21.6, 1.20.1, etc.
- Check the branch for:
   - The mod source for that version
   - Its README.md for build instructions or usage
🔁 If you're browsing just for heads or editing the heads.json, use the main branch.

📜 License:
- [Attribution-NonCommercial-ShareAlike 4.0 International](https://creativecommons.org/licenses/by-nc-sa/4.0/)
- You are free to:
  - Share — copy and redistribute the material in any medium or format
  - Adapt — remix, transform, and build upon the material
  - The licensor cannot revoke these freedoms as long as you follow the license terms.
  - Under the following terms:
      - Attribution — You must give appropriate credit , provide a link to the license, and indicate if changes were made . You may do so in any reasonable manner, but not in any way that suggests the licensor endorses you or your use.
      - NonCommercial — You may not use the material for commercial purposes.
      - ShareAlike — If you remix, transform, or build upon the material, you must distribute your contributions under the same license as the original.
      - No additional restrictions — You may not apply legal terms or technological measures that legally restrict others from doing anything the license permits.

Made with 💀 by Hazar.
