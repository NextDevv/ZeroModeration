package zero.it.plugins.zeromoderation.events

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import org.bukkit.Bukkit
import org.bukkit.entity.HumanEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerChatEvent
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.scheduler.BukkitRunnable
import zero.it.plugins.zeromoderation.ZeroModeration
import zero.it.plugins.zeromoderation.cache.MessageCache
import zero.it.plugins.zeromoderation.cache.getMsg
import zero.it.plugins.zeromoderation.mods.Filter
import zero.it.plugins.zeromoderation.mods.UrlBlock
import java.util.*

class OnChatEvent : Listener {
    private val config = ZeroModeration.conf
    private val options = config["options"] as Map<*, *>
    private val prefix = config["prefix"] as String
    private val mods = options["mods"] as Map<*, *>
    private val debug = options["debug"] as Boolean
    private val messages = config["messages"] as Map<*,*>

    private val lastSentMessage = mutableMapOf<UUID, Long?>()
    private val lastSentCommand = mutableMapOf<UUID, Long>()

    private val notCancelledMessages = hashMapOf<UUID, String?>()

    @EventHandler
    fun onChatEvent(e: PlayerChatEvent) {
        val player = e.player
        val uuid = player.uniqueId
        val message = e.message

        // Don't remove it!
        // it's to check if the message is re-sent
        if(notCancelledMessages[uuid] != null) {
            notCancelledMessages[uuid] = null
            return
        }

        if(ZeroModeration.messagesCache.getMsg(message.lowercase())?.toxic == true) {
            val msg =  messages["filter"] as List<*>
            player.msg(msg.random().toString(), prefix)
            return
        }else if(ZeroModeration.messagesCache.getMsg(message.lowercase())?.toxic == false) { return }

        Bukkit.getOnlinePlayers().forEach { it1 ->
            if(it1.uniqueId == uuid) return@forEach
            e.recipients.remove(it1)
        }

        if(player.hasPermission("zeromod.bypass") && !debug) {
            return
        }

        object : BukkitRunnable() {
            override fun run() {
                var cancelled = false
                val start = System.currentTimeMillis()

                // Uuh yeah, so it seems that this needs to be cast first to double then to long
                // I don't fucking know why, but gson wants to convert to double all the numbers
                val timeoutSpam = options["timeoutSpam"].toString().toDouble().toLong()
                if(lastSentMessage[uuid] == null) {
                    lastSentMessage[uuid] = System.currentTimeMillis()
                }else if(System.currentTimeMillis() - (lastSentMessage[uuid] ?: 0L) > timeoutSpam) {
                    lastSentMessage[uuid] = null
                }else {
                    player.msg("Aspetta ${timeoutSpam/1000} secondi per inviare un altro messaggio", prefix)
                    cancelled = true
                    return
                }

                val allowedLinks = options["allowedLinks"] as List<*>
                if(allowedLinks.contains(message)) {
                    return
                }
                message.split(" ").forEach { word ->
                    val blocked = UrlBlock.validate(word)
                    if(blocked) {
                        val msg =  messages["link"] as List<*>
                        player.msg(msg.random().toString(), prefix)
                        cancelled = true
                        return
                    }
                }

                // Fuck you HTTP delay
                val notPassed = Filter.isToxicJava(message)
                if(notPassed) {
                    val msg =  messages["filter"] as List<*>
                    player.msg(msg.random().toString(), prefix)
                    cancelled = true
                    MessageCache.create(message.lowercase(), true)
                    return
                }

                if(!cancelled) {
                    notCancelledMessages[uuid] = message
                    object : BukkitRunnable() {
                        override fun run() {
                            player.chat(message)
                            MessageCache.create(message.lowercase(), false)
                        }
                    }.runTask(ZeroModeration.plugin)
                    return
                }
                val finish = System.currentTimeMillis()

                println("Took ${finish - start}ms to process")
            }
        }.runTaskAsynchronously(ZeroModeration.plugin)
    }

    @EventHandler
    fun onCommand(e: PlayerCommandPreprocessEvent) {
        val player = e.player
        val uuid = player.uniqueId

        if(player.hasPermission("zeromod.bypass") &&!debug) {
            return
        }

        val timeoutSpam = options["commandTimeoutSpam"] as Long
        if(lastSentCommand[uuid] == null) {
            lastSentCommand[uuid] = System.currentTimeMillis()
        }else if(System.currentTimeMillis() - lastSentCommand[uuid]!! > timeoutSpam) {
            val msg =  messages["commands"] as List<*>
            player.msg(msg.random().toString(), prefix)
            e.isCancelled = true
        }
    }
}

// Stack overflow error, like wtf?
// Makes no sense
private fun Any?.toBoolean(): Boolean {
    return this.toString().toBoolean()
}

private fun HumanEntity.msg(tac: String, prefix: String = "") {
    val style = Style.style(NamedTextColor.GOLD)

    this.sendMessage(Component.text()
        .append(Component.text(tac.split(" ")[0].replace("[prefix]", prefix), style))
        .append(Component.text(tac.split(" ").subList(1, tac.split(" ").size).joinToString(" ")))
        .build()
    )
}
