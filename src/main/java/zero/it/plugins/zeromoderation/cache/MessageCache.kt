package zero.it.plugins.zeromoderation.cache

import zero.it.plugins.zeromoderation.ZeroModeration

data class MessageCache(val message: String, val toxic: Boolean) {
    companion object {
        fun create(message: String, toxic: Boolean): MessageCache {
            val cache = MessageCache(message, toxic)
            if(!exists(message.lowercase())) ZeroModeration.messagesCache.add(cache)
            return cache
        }

        fun exists(message: String): Boolean {
            return ZeroModeration.messagesCache.getMsg(message) != null
        }
    }
}

fun Collection<MessageCache>.getMsg(message: String): MessageCache? {
    return this.firstOrNull { it.message == message }
}
