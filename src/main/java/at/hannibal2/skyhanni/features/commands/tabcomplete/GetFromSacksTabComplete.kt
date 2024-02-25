package at.hannibal2.skyhanni.features.commands.tabcomplete

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.GetFromSackAPI
import at.hannibal2.skyhanni.api.GetFromSackAPI.commands
import at.hannibal2.skyhanni.events.MessageSendToServerEvent
import at.hannibal2.skyhanni.utils.ChatUtils.eventWithNewMessage
import at.hannibal2.skyhanni.utils.ChatUtils.senderIsSkyhanni
import at.hannibal2.skyhanni.utils.LorenzUtils

object GetFromSacksTabComplete {

    private val config get() = SkyHanniMod.feature.commands.tabComplete

    fun handleTabComplete(command: String): List<String>? {
        if (!isEnabled()) return null
        if (command !in commands) return null

        return GetFromSackAPI.sackListNames.map { it.replace(" ", "_") }
    }

    //No subscribe since it needs to be called from the GetFromSackAPI
    fun handleUnderlineReplace(event: MessageSendToServerEvent): MessageSendToServerEvent {
        if (!isEnabled()) return event

        if (event.senderIsSkyhanni()) return event

        if (event.splitMessage.isEmpty()) return event

        val rawName = event.splitMessage[1].uppercase()
        val realName = rawName.replace("_", " ")
        if (realName == rawName) return event
        if (realName !in GetFromSackAPI.sackListNames) return event
        return event.eventWithNewMessage(event.message.replace(rawName, realName))
    }

    fun isEnabled() = LorenzUtils.inSkyBlock && config.gfsSack
}
