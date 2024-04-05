package at.hannibal2.skyhanni.features.garden

import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.events.ConfigLoadEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.LorenzToolTipEvent
import at.hannibal2.skyhanni.events.ProfileJoinEvent
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.ConditionalUtils
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.ItemUtils.name
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.format
import at.hannibal2.skyhanni.utils.NumberUtil.formatLong
import at.hannibal2.skyhanni.utils.NumberUtil.romanToDecimalIfNecessary
import at.hannibal2.skyhanni.utils.NumberUtil.toRoman
import at.hannibal2.skyhanni.utils.RenderUtils.renderString
import at.hannibal2.skyhanni.utils.StringUtils
import at.hannibal2.skyhanni.utils.StringUtils.matchMatcher
import at.hannibal2.skyhanni.utils.StringUtils.matches
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.milliseconds

class GardenLevelDisplay {

    private val config get() = GardenAPI.config.gardenLevels

    private val patternGroup = RepoPattern.group("garden.level")
    private val expToNextLevelPattern by patternGroup.pattern(
        "inventory.nextxp",
        ".* §e(?<nextLevelExp>.*)§6/.*"
    )
    private val gardenItemNamePattern by patternGroup.pattern(
        "inventory.name",
        "Garden (?:Desk|Level (?<currentLevel>.*))"
    )
    private val overflowPattern by patternGroup.pattern(
        "inventory.overflow",
        ".*§r §6(?<overflow>.*)"
    )
    private val gardenLevelPattern by patternGroup.pattern(
        "inventory.levelprogress",
        "§7Progress to Level (?<currentLevel>[^:]*).*"
    )
    private val gardenMaxLevelPattern by patternGroup.pattern(
        "inventory.max",
        "§5§o§7§8Max level reached!"
    )
    private val visitorRewardPattern by patternGroup.pattern(
        "chat.increase",
        " {4}§r§8\\+§r§2(?<exp>.*) §r§7Garden Experience"
    )

    private var display = ""

    @SubscribeEvent
    fun onProfileJoin(event: ProfileJoinEvent) {
        update()
    }

    @SubscribeEvent(receiveCanceled = true)
    fun onChat(event: LorenzChatEvent) {
        if (!GardenAPI.inGarden()) return

        visitorRewardPattern.matchMatcher(event.message) {
            addExp(group("exp").toInt())
        }
    }

    private fun addExp(moreExp: Int) {
        val gardenExp = GardenAPI.gardenExp ?: return
        val oldLevel = GardenAPI.getGardenLevel()
        GardenAPI.gardenExp = gardenExp + moreExp
        if (!config.overflowMessages) return
        val newLevel = GardenAPI.getGardenLevel()
        if (newLevel == oldLevel + 1 && newLevel > 15) {
            LorenzUtils.runDelayed(50.milliseconds) {
                ChatUtils.clickableChat(
                    " \n§b§lGARDEN LEVEL UP §8$oldLevel ➜ §b$newLevel\n" +
                        " §8+§aRespect from Elite Farmers and SkyHanni members :)\n ",
                    "/gardenlevels",
                    false
                )
            }
        }
        update()
    }


    @SubscribeEvent
    fun onInventoryOpen(event: InventoryFullyOpenedEvent) {
        if (!GardenAPI.inGarden()) return
        val item = when (event.inventoryName) {
            "Desk" -> event.inventoryItems[4] ?: return
            "SkyBlock Menu" -> event.inventoryItems[10] ?: return
            else -> return
        }
        if (!gardenItemNamePattern.matches(item.name.removeColor())) return
        var nextLevelExp = 0L
        var currentLevel = 0
        for (line in item.getLore()) {
            gardenLevelPattern.matchMatcher(line) {
                currentLevel = group("currentLevel").romanToDecimalIfNecessary() - 1
            }
            if (line == "§7§8Max level reached!") currentLevel = 15
            expToNextLevelPattern.matchMatcher(line) {
                nextLevelExp = group("nextLevelExp").formatLong()
            }
            overflowPattern.matchMatcher(line) {
                val overflow = group("overflow").formatLong()
                GardenAPI.gardenExp = overflow
                update()
                return
            }
        }
        val expForLevel = GardenAPI.getExpForLevel(currentLevel).toInt()
        GardenAPI.gardenExp = expForLevel + nextLevelExp
        update()
    }

    @SubscribeEvent
    fun onTooltip(event: LorenzToolTipEvent) {
        if (!GardenAPI.inGarden()) return
        if (!config.overflow.get()) return
        val inventoryName = InventoryUtils.openInventoryName()
        val slotIndex = event.slot.slotIndex
        println("ping")
        if (!((inventoryName == "Desk" && slotIndex == 4) ||
            (inventoryName == "SkyBlock Menu" && slotIndex == 10))) return
        println("pong")
        val gardenExp = GardenAPI.gardenExp ?: return
        val currentLevel = GardenAPI.getGardenLevel()
        if (currentLevel < 15) return

        val needForLevel = GardenAPI.getExpForLevel(currentLevel).toInt()
        val overflow = (gardenExp - needForLevel).toDouble()
        val overflowTotal = (gardenExp - GardenAPI.getExpForLevel(15)).toInt()
        val needForNextLevel = GardenAPI.getExpForLevel(currentLevel + 1).toInt()
        val needForOnlyNextLvl = needForNextLevel - needForLevel
        println("hello")
        val iterator = event.toolTip.listIterator()
        if (slotIndex == 4 && currentLevel > 15) event.itemStack.name = "§aGarden Level ${currentLevel.toRoman()}"
        var next = false
        println("aaaa")
        for (line in iterator) {
            println(line)
            if (gardenMaxLevelPattern.matches(line)) {
                println("no matcho")
                iterator.set("§7Progress to Level ${(currentLevel+1).toRoman()}")
                next = true
                continue
            }
            if (next && line.contains("                    ")) {
                println("seto")
                val progress = overflow/needForOnlyNextLvl
                val progressBar = StringUtils.progressBar(progress, 20)
                iterator.set("$progressBar §e${overflow.addSeparators()}§6/§e${format(needForOnlyNextLvl)}")
                iterator.add("")
                iterator.add("§b§lOVERFLOW XP:")
                iterator.add("§7▸ ${overflowTotal.addSeparators()}")
                return
            }
        }
    }

    private fun update() {
        display = drawDisplay()
    }

    private fun drawDisplay(): String {
        val gardenExp = GardenAPI.gardenExp ?: return "§aGarden Level ? §cOpen the desk!"
        val currentLevel = GardenAPI.getGardenLevel(config.overflow.get())
        val isMax = !config.overflow.get() && currentLevel == 15
        val needForLevel = GardenAPI.getExpForLevel(currentLevel).toInt()
        val overflow = gardenExp - needForLevel

        return "§aGarden level $currentLevel " + if (isMax) {
            "§7(§e${overflow.addSeparators()}§7)"
        } else {
            val needForNextLevel = GardenAPI.getExpForLevel(currentLevel + 1).toInt()
            val needForOnlyNextLevel = needForNextLevel - needForLevel
            "§7(§e${overflow.addSeparators()}§7/§e${needForOnlyNextLevel.addSeparators()}§7)"
        }
    }

    @SubscribeEvent
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!isEnabled()) return
        if (GardenAPI.hideExtraGuis()) return

        config.pos.renderString(display, posLabel = "Garden Level")
    }

    @SubscribeEvent
    fun onConfigLoad(event: ConfigLoadEvent) {
        ConditionalUtils.onToggle(config.overflow) { update() }
    }

    private fun isEnabled() = GardenAPI.inGarden() && config.display

    @SubscribeEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(3, "garden.gardenLevelDisplay", "garden.gardenLevels.display")
        event.move(3, "garden.gardenLevelPos", "garden.gardenLevels.pos")
    }
}
