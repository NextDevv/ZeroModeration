package zero.it.plugins.zeromoderation.utils

import org.bukkit.ChatColor


fun String.tac(code:Char='&'):String {
    return net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', code.toString())
}