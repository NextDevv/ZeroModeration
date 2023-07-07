package zero.it.plugins.zeromoderation.mods

import zero.it.plugins.zeromoderation.ZeroModeration
import java.net.HttpURLConnection
import java.net.URL

class UrlBlock {
    companion object {
        private val config = ZeroModeration.conf
        private val options = config["options"] as Map<*, *>
        private val prefix = config["prefix"] as String
        private val mods = options["mods"] as Map<*, *>
        private val debug = options["debug"] as Boolean

        fun isActive(): Boolean {
            return mods["antiLink"].toString().toBoolean()
        }

        /**
         * Returns true if the given [url] is a valid URL.
         * @param url the URL to check
         * @return true if the given [url] is a valid URL
         */
        fun validate(url: String): Boolean {
            // Not the best way to check if the URL is valid, but it works
            var retries = 0
            while (retries < 3) {
                try {
                    var u = url
                    if(!url.startsWith("http://") || !url.startsWith("https://")) {
                        when(retries) {
                            0 -> u = "http://$u"
                            1 -> u = "https://$u"
                        }
                    }
                    val url2 = URL(u)
                    val httpURLConnection = url2.openConnection() as HttpURLConnection
                    httpURLConnection.requestMethod = "HEAD"
                    val responseCode = httpURLConnection.responseCode
                    return responseCode != 404
                }catch (e: Exception) { retries++ }
            }
            return false
        }
    }
}