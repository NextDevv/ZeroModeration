package zero.it.plugins.zeromoderation.mods

import zero.it.plugins.zeromoderation.ZeroModeration

class AntiSpam {
    companion object {
        private val config = ZeroModeration.conf
        private val options = config["options"] as Map<*, *>
        private val prefix = config["prefix"] as String
        private val mods = options["mods"] as Map<*, *>
        private val debug = options["debug"] as Boolean

        fun antiMessageSpamActive(): Boolean {
            return mods["antiSpam"].toString().toBoolean()
        }

        fun antiCommandSpamActive(): Boolean {
            return mods["antiSpamCommands"].toString().toBoolean()
        }
    }
}