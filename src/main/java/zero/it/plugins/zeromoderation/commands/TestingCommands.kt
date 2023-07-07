package zero.it.plugins.zeromoderation.commands

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import zero.it.plugins.zeromoderation.ZeroModeration
import zero.it.plugins.zeromoderation.mods.Filter
import zero.it.plugins.zeromoderation.mods.UrlBlock
import zero.it.plugins.zeromoderation.utils.tac

class TestingCommands : CommandExecutor {
    private val config = ZeroModeration.conf
    private val options = config["options"] as Map<*, *>
    private val prefix = config["prefix"] as String
    private val mods = options["mods"] as Map<*, *>
    private val debug = options["debug"] as Boolean
    private val messages = config["messages"] as Map<*,*>

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if(args.isNullOrEmpty()) {
            sender.sendMessage("Args must not be empty")
            return false
        }
        when(args[0].lowercase()) {
            "filter" -> {
                GlobalScope.launch {
                    val message = try { args.toList().subList(1, args.size).joinToString(" ") } catch (e: ArrayIndexOutOfBoundsException) { "Fuck you" }
                    val filter = Filter.isToxicDebug(message)

                    if(sender !is ConsoleCommandSender) {
                        sender.msg("[&eResults&f]")
                        sender.msg("&eText&f: ${filter["text"]}")
                        sender.msg("&eType&f: ${filter["type"]}")
                        sender.msg("&eSummary Score&f: ${filter["summaryScore"]}")
                        sender.msg("&eThreshold Passed&f: ${if(filter["thresholdPassed"] == true) "&atrue" else "&cfalse"}")
                        sender.msg("&ePercentage Toxic&f: ${filter["percentageToxic"]}%")
                        sender.msg("[&eEnd&f]")
                        sender.msg("The result may not be accurate.")
                    }else {
                        sender.sendMessage("[Results]")
                        sender.sendMessage("Text: ${filter["text"]}")
                        sender.sendMessage("Type: ${filter["type"]}")
                        sender.sendMessage("Summary Score: ${filter["summaryScore"]}")
                        sender.sendMessage("Threshold Passed: ${if(filter["thresholdPassed"] == true) "true" else "false"}")
                        sender.sendMessage("Percentage Toxic: ${filter["percentageToxic"]}%")
                        sender.sendMessage("[End]")
                        sender.sendMessage("The result may not be accurate.")
                    }

                }
            }

            "link" -> {
                val arg = try { args[1] } catch (e: ArrayIndexOutOfBoundsException) { "www.google.com" }
                GlobalScope.launch {
                    val blocked = UrlBlock.validate(arg)
                    if(sender !is ConsoleCommandSender) {
                        sender.msg("[&eResults&f]")
                        sender.msg("&eLink&f: $arg")
                        sender.msg("&eBlocked&f: ${if(blocked) "&atrue" else "&cfalse"}")
                        sender.msg("[&eEnd&f]")
                        sender.msg("The result may not be accurate.")
                    }else {
                        sender.sendMessage("[Results]")
                        sender.sendMessage("Link: $arg")
                        sender.sendMessage("Blocked: ${if(blocked) "true" else "false"}")
                        sender.sendMessage("[End]")
                        sender.sendMessage("The result may not be accurate.")
                    }
                }
            }

            "msgs" -> {
                val spam =  messages["spam"] as List<*>
                val commands =  messages["commands"] as List<*>
                val filter =  messages["filter"] as List<*>
                val link =  messages["link"] as List<*>

                if(sender !is ConsoleCommandSender) {

                    sender.msg("[&eResults&f]")
                    sender.msg("&eSpam&f: ${spam.random().toString()}")
                    sender.msg("&eCommands&f: ${commands.random().toString()}")
                    sender.msg("&eFilter&f: ${filter.random().toString()}")
                    sender.msg("&eLink&f: ${link.random().toString()}")
                    sender.msg("[&eEnd&f]")
                }else {
                    sender.sendMessage("[Results]")
                    sender.sendMessage("Spam: ${spam.random().toString()}")
                    sender.sendMessage("Commands: ${commands.random().toString()}")
                    sender.sendMessage("Filter: ${filter.random().toString()}")
                    sender.sendMessage("Link: ${link.random().toString()}")
                    sender.sendMessage("[End]")
                }
            }
        }
        return true
    }
}

private fun CommandSender.msg(s: String) {
    this.sendMessage(s.tac())
}
