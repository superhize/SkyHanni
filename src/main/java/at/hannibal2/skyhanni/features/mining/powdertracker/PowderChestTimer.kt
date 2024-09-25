package at.hannibal2.skyhanni.features.mining.powdertracker

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.features.mining.PowderTrackerConfig
import at.hannibal2.skyhanni.data.ClickType
import at.hannibal2.skyhanni.data.HotmData
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.BlockClickEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.LorenzRenderWorldEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.events.PlaySoundEvent
import at.hannibal2.skyhanni.events.ServerBlockChangeEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.BlockUtils.getBlockStateAt
import at.hannibal2.skyhanni.utils.ColorUtils.toChromaColor
import at.hannibal2.skyhanni.utils.EntityUtils
import at.hannibal2.skyhanni.utils.LocationUtils
import at.hannibal2.skyhanni.utils.LocationUtils.distanceToPlayer
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.RecalculatingValue
import at.hannibal2.skyhanni.utils.RenderUtils.draw3DLine
import at.hannibal2.skyhanni.utils.RenderUtils.drawString
import at.hannibal2.skyhanni.utils.RenderUtils.drawWaypointFilled
import at.hannibal2.skyhanni.utils.RenderUtils.exactPlayerEyeLocation
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderable
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.StringUtils
import at.hannibal2.skyhanni.utils.TimeLimitedCache
import at.hannibal2.skyhanni.utils.renderables.Renderable
import net.minecraft.block.BlockChest
import net.minecraft.block.state.IBlockState
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object PowderChestTimer {

    private val config get() = SkyHanniMod.feature.mining.powderTracker.chestTimer

    private var display = Renderable.string("Chest Timer")
    private val chestSet = TimeLimitedCache<LorenzVec, SimpleTimeMark>(61.seconds)
    private const val MAX_CHEST_DISTANCE = 15
    private const val NEAR_PLAYER_DISTANCE = 25
    private var lastSound = SimpleTimeMark.farPast()

    private val arePlayersNearby by RecalculatingValue(5.seconds) {
        EntityUtils.getPlayerEntities().any { it.distanceToPlayer() < NEAR_PLAYER_DISTANCE }
    }

    @SubscribeEvent
    fun onSound(event: PlaySoundEvent) {
        if (!isEnabled()) return
        if (event.soundName == "random.levelup" && event.pitch == 1.0f && event.volume == 1.0f) {
            lastSound = SimpleTimeMark.now()
        }
    }

    @SubscribeEvent
    fun onRenderGui(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!isEnabled()) return

        config.position.renderRenderable(display, posLabel = "Powder Chest Timer")
    }

    @SubscribeEvent
    fun onWorldChange(event: LorenzWorldChangeEvent) {
        if (!isEnabled()) return

        chestSet.clear()
    }

    @SubscribeEvent
    fun onServerBlockChange(event: ServerBlockChangeEvent) {
        if (!isEnabled()) return
        val location = event.location
        if (location.distanceToPlayer() > 15) return
        val isNewChest = event.newState.isChest()
        val isOldChest = event.oldState.isChest()

        if (isNewChest && !isOldChest) {
            if (arePlayersNearby && lastSound.passedSince() > 200.milliseconds) return
            if (location.distanceToPlayer() > MAX_CHEST_DISTANCE) return
            chestSet[location] = SimpleTimeMark.now() + 60500.milliseconds
        } else if (isOldChest && !isNewChest) {
            chestSet.remove(location)
        }
    }

    @SubscribeEvent
    fun onBlockClick(event: BlockClickEvent) {
        if (!isEnabled()) return
        val location = event.position
        if (!location.getBlockStateAt().isChest()) return

        if (HotmData.GREAT_EXPLORER.activeLevel < 20) return

        if (location.isOpened()) return
        if (event.clickType == ClickType.RIGHT_CLICK) {
            chestSet.remove(location)
            return
        }
    }

    @SubscribeEvent
    fun onSecondPassed(event: LorenzTickEvent) {
        if (!isEnabled()) return

        display = drawDisplay()
    }

    private fun drawDisplay(): Renderable {
        if (chestSet.entries().isEmpty()) return Renderable.string("")

        val count = chestSet.entries().size
        val name = StringUtils.pluralize(count, "chest")
        val first = chestSet.minByOrNull { it.value.timeUntil() } ?: return Renderable.string("")

        val timeUntil = first.value.timeUntil()
        val color = timeUntil.getColorBasedOnTime().toChatColor()

        return Renderable.string("$color$timeUntil §8(§e$count §b$name§8)")
    }

    @SubscribeEvent
    fun onRender(event: LorenzRenderWorldEvent) {
        if (!isEnabled()) return
        for ((loc, time) in chestSet) {

            val color = if (config.useStaticColor) config.staticColor.toChromaColor() else time.timeUntil().getColorBasedOnTime()

            if (config.highlightChests)
                event.drawWaypointFilled(loc, color, seeThroughBlocks = false)

            if (config.drawTimerOnChest) {
                val y = if (loc.y <= LocationUtils.playerLocation().y) 1.25 else -0.25
                event.drawString(
                    loc.add(y = y, x = 0.5, z = 0.5), "${time.timeUntil()}",
                    seeThroughBlocks = false,
                    color = color,
                )
            }

            when (config.lineMode) {
                PowderTrackerConfig.ChestTimer.LineMode.OLDEST -> {
                    val sorted = chestSet.sortedBy { it.value.timeUntil() }
                    if (sorted.isNotEmpty()) {
                        val chestToConnect = sorted.take(config.drawLineToChestAmount)
                        if (chestToConnect.isNotEmpty()) {
                            val (firstPos, firstTime) = chestToConnect.first()
                            event.draw3DLine(
                                event.exactPlayerEyeLocation(),
                                firstPos.getMiddle(),
                                firstTime.timeUntil().getColorBasedOnTime(),
                                3,
                                true,
                            )

                            for (i in 0 until chestToConnect.size - 1) {
                                val (current, currentTime) = chestToConnect[i]
                                val (next, _) = chestToConnect[i + 1]

                                val currentUtil = currentTime.timeUntil()

                                val currentColor = currentUtil.getColorBasedOnTime()

                                event.draw3DLine(
                                    current.getMiddle(),
                                    next.getMiddle(),
                                    currentColor,
                                    3,
                                    true,
                                )
                            }
                        }
                    }
                }

                PowderTrackerConfig.ChestTimer.LineMode.NEAREST -> {
                    val sorted = chestSet.sortedBy { it.key.distanceToPlayer() }
                    if (sorted.isNotEmpty()) {
                        val chestToConnect = sorted.take(config.drawLineToChestAmount)
                        if (chestToConnect.isNotEmpty()) {
                            val (firstPos, firstTime) = chestToConnect.first()
                            event.draw3DLine(
                                event.exactPlayerEyeLocation(),
                                firstPos.getMiddle(),
                                firstTime.timeUntil().getColorBasedOnTime(),
                                3,
                                true,
                            )

                            for (i in 0 until chestToConnect.size - 1) {
                                val (current, currentTime) = chestToConnect[i]
                                val (next, _) = chestToConnect[i + 1]

                                val currentUntil = currentTime.timeUntil()

                                event.draw3DLine(
                                    current.getMiddle(),
                                    next.getMiddle(),
                                    currentUntil.getColorBasedOnTime(),
                                    3,
                                    true,
                                )
                            }
                        }
                    }
                }

                else -> {}
            }
        }
    }

    private fun Color.toChatColor(): String {
        return when (this) {
            Color.RED -> "§4"
            Color.GREEN -> "§a"
            Color.ORANGE -> "§6"
            else -> ""
        }
    }

    private fun Duration.getColorBasedOnTime(): Color {
        val totalTime = inWholeSeconds

        return when {
            totalTime > 30 -> {
                Color.GREEN
            }

            totalTime in 11..30 -> {
                Color.ORANGE
            }

            else -> {
                Color.RED
            }
        }
    }

    private fun LorenzVec.getMiddle() = add(0.5, 0.5, 0.5)

    private fun LorenzVec.isOpened() = !chestSet.containsKey(this)

    private fun IBlockState.isChest() = block is BlockChest

    private fun isEnabled() = config.enabled && IslandType.CRYSTAL_HOLLOWS.isInIsland()
}
