package zero.it.plugins.zeromoderation.mods

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import json.JsonString
import zero.it.plugins.zeromoderation.ZeroModeration
import zero.it.plugins.zeromoderation.utils.tac
import java.io.IOException
import java.net.URI
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.util.concurrent.CompletableFuture

class Filter {
    companion object {
        private const val API_KEY = "AIzaSyDBaAmUmfRDgzc-Z1TZeOEuyHOq6lqVOC8"
        private var threshold = 0.5
        private var type = "TOXICITY"

        private val config = ZeroModeration.conf
        private val options = config["options"] as Map<*, *>
        private val prefix = config["prefix"] as String
        private val mods = options["mods"] as Map<*, *>
        private val debug = options["debug"] as Boolean



        fun init(): Boolean {
            if (!(mods["filter"] as Boolean)) {
                println("$prefix Filter mod is disabled, might be an error?".tac())
                return false
            }

            threshold = options["thresholdFilter"] as Double
            type = options["typeFilter"] as String
            return true
        }

        // If you say this is duplicate code, it is
        // But I'm too lazy to write it.

        suspend fun isToxicDebug(message: String): Map<String, Any> {
            val config = ZeroModeration.conf
            val prefix = config["prefix"] as String
            try {
                // Replace with your own API key
                // Replace with your own comment text
                val commentText = message
                    .replace("4", "a")
                    .replace("1", "i")
                    .replace("0", "o")
                    .replace("6", "g")
                    .replace("3", "e")
                    .replace("5", "s")
                    .replace("7", "t")
                    //.replace(" ", "")
                    .replace(".", "")
                    .replace(",", "")

                // Replace with your own desired attributes
                val attributes = "TOXICITY"

                // Construct the API endpoint URL
                val url = "https://commentanalyzer.googleapis.com/v1alpha1/comments:analyze?key=$API_KEY"
                val client = HttpClient()

                val req = client.post(url) {
                    setBody("{\"comment\": {\"text\": \"$commentText\"}, \"requestedAttributes\": {\"$type\": {}}}")
                }

                if (req.status.value >= 400) {
                    return mapOf("error" to req.bodyAsText())
                }

                val json = JsonString(req.bodyAsText()).convert()
                val attributeScores = json["attributeScores"] as Map<*, *>
                val toxicity = attributeScores["TOXICITY"] as Map<*, *>
                val summaryScore = toxicity["summaryScore"] as Map<*, *>
                val value = summaryScore["value"] as Double
                val type = summaryScore["type"] as String

                // Disconnect the connection
                client.close()
                return mapOf(
                    "text" to commentText,
                    "type" to type,
                    "summaryScore" to value,
                    "thresholdPassed" to (0.5 >= value),
                    "percentageToxic" to String.format("%.2f", (value * 100)).replace(",", ".")
                )
            } catch (e: IOException) {
                println("$prefix Unable to filter this message '$message' enable debug to check the error!".tac())
                if (debug) {
                    e.printStackTrace()
                }
                return mapOf("error" to (e.message ?: ""))
            }
        }


        suspend fun isToxic(message: String): Boolean {
            // Hi, don't touch anything please :)
            val config = ZeroModeration.conf
            val prefix = config["prefix"] as String
            try {
                // Replace with your own API key
                // Replace with your own comment text
                val commentText = message
                    .replace("4", "a")
                    .replace("1","i")
                    .replace("0","o")
                    .replace("6", "g")
                    .replace("3", "e")
                    .replace("5", "s")
                    .replace("7", "t")
                    //.replace(" ", "")
                    .replace(".", "")
                    .replace(",", "")

                // Replace with your own desired attributes
                val attributes = "TOXICITY"

                // Construct the API endpoint URL
                val url = "https://commentanalyzer.googleapis.com/v1alpha1/comments:analyze?key=$API_KEY"
                val client = HttpClient()

                val req = client.post(url) {
                    setBody("{\"comment\": {\"text\": \"$commentText\"}, \"requestedAttributes\": {\"$type\": {}}}")
                }

                if(req.status.value >= 400) {
                    println("$prefix An error occurred during an http request, enable debug mode to check the full error: ")
                    if(debug) {
                        println("$prefix [DEBUG] error: ")
                        println(req.bodyAsText())
                    }
                    return false
                }

                val json = JsonString(req.bodyAsText()).convert()
                val attributeScores = json["attributeScores"] as Map<*,*>
                val toxicity = attributeScores["TOXICITY"] as Map<*,*>
                val summaryScore = toxicity["summaryScore"]  as Map<*,*>
                val value = summaryScore["value"] as Double
                val type = summaryScore["type"] as String

                if(debug) {
                    println("Text : $commentText")
                    println("Type : $type")
                    println("Summary Score : $value")
                    // 0.5 -> conversazione civile
                    // 0.7 -> essenziale
                    println("Threshold passed : ${0.5 >= value}")
                    // format the percentage to 2 decimal places
                    val percentage = String.format("%.2f", (value * 100)).replace(",", ".")
                    println("Percentage passed likely to not pass : ${percentage}%")
                }

                // Disconnect the connection
                client.close()
                return threshold >= value
            } catch (e: IOException) {
                println("$prefix Unable to filter this message '$message' enable debug to check the error!")
                if(debug) {
                    e.printStackTrace()
                }
                return false
            }
        }

        fun isToxicJava(message: String): Boolean {
            if(!(mods["filter"] as Boolean)) {
                return false
            }
            val client = java.net.http.HttpClient.newHttpClient()
            val requestBody = "{\"comment\": {\"text\": \"$message\"}, \"requestedAttributes\": {\"$type\": {}}}"
            val request = HttpRequest.newBuilder()
                .uri(URI.create("https://commentanalyzer.googleapis.com/v1alpha1/comments:analyze?key=$API_KEY"))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                .build()

            try {

                val ret = client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply {
                    val body = it.body()
                    if((it.statusCode() ?: 400) >= 400) {
                        println("$prefix An error occurred during an http request, enable debug mode to check the full error: ")
                        if(debug) {
                            println("$prefix [DEBUG] error: ")
                            println(body)
                        }
                        return@thenApply false
                    }
                    val json = JsonString(body).convert()
                    val attributeScores = json["attributeScores"] as Map<*,*>
                    val toxicity = attributeScores["TOXICITY"] as Map<*,*>
                    val summaryScore = toxicity["summaryScore"]  as Map<*,*>
                    val value = summaryScore["value"] as Double
                    val type = summaryScore["type"] as String

                    if(debug) {
                        println("Text : $message")
                        println("Type : $type")
                        println("Summary Score : $value")
                        // 0.5 -> conversazione civile
                        // 0.7 -> essenziale
                        println("Threshold passed : ${0.5 >= value}")
                        // format the percentage to 2 decimal places
                        val percentage = String.format("%.2f", (value * 100)).replace(",", ".")
                        println("Percentage passed likely to not pass : ${percentage}%")
                    }

                    return@thenApply value >= threshold
                }

                return ret.thenApply { return@thenApply it }.get()
            } catch (e: IOException) {
                // Handle IOException
                System.err.println("An I/O error occurred: " + e.message)
            } catch (e: InterruptedException) {
                // Handle InterruptedException
                System.err.println("The operation was interrupted: " + e.message)
            }
            return true
        }
    }
}