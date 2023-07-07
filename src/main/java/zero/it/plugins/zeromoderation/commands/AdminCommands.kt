package zero.it.plugins.zeromoderation.commands

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor

// I don't remember how the class called that contains both of these, but leave it or do something IDK
class AdminCommands : CommandExecutor, TabExecutor {
    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>?
    ): MutableList<String> {
        return mutableListOf("perspective-api", "reload")
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if(args.isNullOrEmpty()) {
            sender.sendMessage("Args: perspective-api")
            return true
        }

        when(args[0]) {
            "perspective-api" -> {
                if(args.size < 2) {
                    sender.sendMessage("Usage: /perspective-api <args>")
                    sender.sendMessage("Args: feedback")
                    return true
                }

                when(args[1]) {
                    "feedback" -> {
                        GlobalScope.launch {
                            // TODO give feedback to the api
                            // Again too lazy to do this right now
                        }
                    }
                }
            }

            "reload" -> {
                // TODO reload the config file
                // I'm too lazy to do this right now
            }
        }
        return true
    }
}