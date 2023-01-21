package at.hannibal2.skyhanni.features.nether.reputationhelper.miniboss

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.HyPixelData
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.features.nether.reputationhelper.CrimsonIsleReputationHelper
import at.hannibal2.skyhanni.test.GriffinUtils.drawWaypointFilled
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.NEUItems
import at.hannibal2.skyhanni.utils.RenderUtils.drawDynamicText
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.*
import java.util.regex.Pattern

class DailyMiniBossHelper(private val reputationHelper: CrimsonIsleReputationHelper) {

    val miniBosses = mutableListOf<CrimsonMiniBoss>()

    fun init() {
        val repoData = reputationHelper.repoData
        val jsonElement = repoData["MINIBOSS"]
        for ((displayName, extraData) in jsonElement.asJsonObject.entrySet()) {
            val data = extraData.asJsonObject
            val displayItem = data["item"]?.asString
            val patterns = " *§r§6§l${displayName.uppercase()} DOWN!"

            val locationData = data["location"]?.asJsonArray
            val location: LorenzVec? = if (locationData == null || locationData.size() == 0) {
                null
            } else {
                val x = locationData[0].asDouble
                val y = locationData[1].asDouble
                val z = locationData[2].asDouble
                LorenzVec(x, y, z)
            }

            miniBosses.add(CrimsonMiniBoss(displayName, displayItem, location, Pattern.compile(patterns)))
        }
    }

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!HyPixelData.skyBlock) return
        if (LorenzUtils.skyBlockIsland != IslandType.CRIMSON_ISLE) return
        if (!SkyHanniMod.feature.misc.crimsonIsleReputationHelper) return

        val message = event.message
        for (miniBoss in miniBosses) {
            if (miniBoss.pattern.matcher(message).matches()) {
                finished(miniBoss)
            }
        }
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (!LorenzUtils.inSkyBlock) return
        if (LorenzUtils.skyBlockIsland != IslandType.CRIMSON_ISLE) return
        if (!SkyHanniMod.feature.misc.crimsonIsleReputationHelper) return
        if (!SkyHanniMod.feature.misc.crimsonIsleReputationLocation) return

        for (miniBoss in miniBosses) {
            if (!miniBoss.doneToday) {
                val location = miniBoss.location ?: continue
                event.drawWaypointFilled(location, LorenzColor.WHITE.toColor())
                event.drawDynamicText(location, miniBoss.displayName, 1.5)
            }
        }
    }

    private fun finished(miniBoss: CrimsonMiniBoss) {
        LorenzUtils.debug("Detected mini boss death: ${miniBoss.displayName}")
        reputationHelper.questHelper.finishMiniBoss(miniBoss)
        miniBoss.doneToday = true
        reputationHelper.update()
    }

    fun render(display: MutableList<List<Any>>) {
        val done = miniBosses.count { it.doneToday }
        display.add(Collections.singletonList(""))
        display.add(Collections.singletonList("Daily Bosses ($done/5 killed)"))
        if (done != 5) {
            for (miniBoss in miniBosses) {
                val result = if (miniBoss.doneToday) "§7Done" else "§bTodo"
                val displayName = miniBoss.displayName
                val displayItem = miniBoss.displayItem
                if (displayItem == null) {
                    display.add(Collections.singletonList("  $displayName: $result"))
                } else {
                    val lineList = mutableListOf<Any>()
                    lineList.add(" ")
                    lineList.add(NEUItems.readItemFromRepo(displayItem))
                    lineList.add("$displayName: $result")
                    display.add(lineList)
                }
            }
        }
    }

    fun reset() {
        for (miniBoss in miniBosses) {
            miniBoss.doneToday = false
        }
    }

    fun saveConfig() {
        SkyHanniMod.feature.hidden.crimsonIsleMiniBossesDoneToday.clear()

        miniBosses.filter { it.doneToday }
            .forEach { SkyHanniMod.feature.hidden.crimsonIsleMiniBossesDoneToday.add(it.displayName) }
    }

    fun loadConfig() {
        for (name in SkyHanniMod.feature.hidden.crimsonIsleMiniBossesDoneToday) {
            getByDisplayName(name)!!.doneToday = true
        }
    }

    private fun getByDisplayName(name: String) = miniBosses.firstOrNull { it.displayName == name }
}