package zero.it.plugins.zeromoderation

import io.papermc.paper.event.player.AsyncChatEvent
import json.JsonFile
import json.JsonString
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import zero.it.plugins.zeromoderation.cache.MessageCache
import zero.it.plugins.zeromoderation.commands.TestingCommands
import zero.it.plugins.zeromoderation.events.OnChatEvent
import zero.it.plugins.zeromoderation.mods.AntiSpam
import zero.it.plugins.zeromoderation.mods.Filter
import zero.it.plugins.zeromoderation.mods.UrlBlock
import zero.it.plugins.zeromoderation.utils.tac
import java.io.File
import java.nio.file.Files
import kotlin.io.path.Path
import kotlin.properties.Delegates

// DISCALAMER!
// I fucking hate paper api stuff
// So if I get something wrong, fuck you
class ZeroModeration : JavaPlugin() {

    companion object {
        var conf by Delegates.notNull<JsonFile>()
            private set
        var plugin by Delegates.notNull<ZeroModeration>()
            private set
        // Just, just leave it as is for now.
        var pluginDataFolder: String = ZeroModeration::class.java.protectionDomain.codeSource.location.path
            private set
        val messagesCache: MutableList<MessageCache> = mutableListOf()

        fun log(message: String) {
            println("[ZeroModeration] $message")
        }

        fun initLog(message: String) {
            log("\t$message")
        }
    }

    init {
        plugin = this
        // DON'T TOUCH THESE, PLEASE!
        // It works somehow, but it's not the best way to do it.
        val dataFolder = File(File(pluginDataFolder).parent, "ZeroModeration")
        Files.createDirectories(Path(dataFolder.path))
        if (!dataFolder.exists()) {
            dataFolder.mkdir()
        }
        pluginDataFolder = dataFolder.absolutePath
    }

    override fun onEnable() {
        // Plugin startup logic
        log("================= ENABLING =================")
        initLog("Getting the configurations...")
        conf = JsonFile("${dataFolder.path}${File.separator}config.json")
        if(!conf.exists()) {
            initLog("No configuration file found, creating one...")
            // Am scared if I should get the config from the resource folder in the jar
            // IDK, just leave it as is for now.
            val jsonString = JsonString("{\n" +
                    "  \"prefix\": \"[ZeroMod] \",\n" +
                    "  \"options\": {\n" +
                    "    \"debug\": false,\n" +
                    "    \"mods\": {\n" +
                    "      \"filter\": true,\n" +
                    "      \"antiSpam\": true,\n" +
                    "      \"antiLink\": true,\n" +
                    "      \"antiSpamCommands\": true\n" +
                    "    },\n" +
                    "    \"thresholdFilter\": 0.5,\n" +
                    "    \"typeFilter\": \"TOXICITY\",\n" +
                    "    \"timeoutSpam\": 1000,\n" +
                    "    \"commandTimeoutSpam\": 500,\n" +
                    "    \"allowedLinks\": [\n" +
                    "      \"google.com\",\n" +
                    "      \"youtube.com\",\n" +
                    "      \"https://www.google.com\",\n" +
                    "      \"https://www.youtube.com\"\n" +
                    "    ],\n" +
                    "    \"beta\": {\n" +
                    "      \"enabled\": false,\n" +
                    "      \"censored\": true,\n" +
                    "      \"autoPunishments\": true\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"messages\": {\n" + // Funny messages :))
                    "    \"filter\": [\n" +
                    "      \"[prefix] Non vuoi essere il re dei cafoni? Controlla bene quello che scrivi. Le tue parole potrebbero scatenare una guerra mondiale.\",\n" +
                    "      \"[prefix] Un buon trucco per capire se il tuo messaggio è offensivo è immaginare di riceverlo tu stesso. Ti farebbe ridere o arrabbiare?\",\n" +
                    "      \"[prefix] Se hai dei dubbi sul tono del tuo messaggio, chiedi a un amico di fiducia di leggerlo prima d'inviarlo. Se anche lui si mette le mani nei capelli, forse è meglio che lo cancelli.\",\n" +
                    "      \"[prefix] Prima d'inviare, pensa: \\\"Questo messaggio farà arrabbiare qualcuno?\\\" Se sì, cancellalo e scrivi più dolce.\",\n" +
                    "      \"[prefix] Prima d'inviare, ricordati di controllare se hai offeso qualcuno. Non vorrai mica finire nei guai per una battuta!\",\n" +
                    "      \"[prefix] Se vuoi evitare di offendere qualcuno con il tuo messaggio, fai una pausa e rileggilo. Magari scopri che non era una buona idea scrivere che la sua torta era immangiabile.\",\n" +
                    "      \"[prefix] Prima di premere invio, ricorda che il destinatario potrebbe non apprezzare il tuo umorismo nero. O forse sì, ma non vuoi rischiare di scoprirlo.\",\n" +
                    "      \"[prefix] Ricorda che la chat non è un ring di boxe. Non usare parole che potrebbero ferire gli altri. Se vuoi sfogarti, vai a correre o scrivi un diario.\",\n" +
                    "      \"[prefix] Prima di premere invio, fai un respiro profondo e chiediti: \\\"Questo messaggio potrebbe far piangere qualcuno?\\\" Se la risposta è sì, cancellalo subito!\",\n" +
                    "      \"[prefix] Non scrivere in chat ciò che non diresti a voce. Leggi il tuo messaggio ad alta voce. Se ti vergogni o ti penti, cambialo.\",\n" +
                    "      \"[prefix] Dietro ogni schermo c'è una persona. Mettiti nei suoi panni e immagina la sua reazione. Se pensi che si ferisca o si deluda, riscrivilo.\"\n" +
                    "    ],\n" +
                    "    \"spam\": [\n" +
                    "      \"[prefix] WOW, rallenta un attimo!\",\n" +
                    "      \"[prefix] Se continui a spammare così, la tua tastiera si scioglierà!\",\n" +
                    "      \"[prefix] Sai che ogni messaggio che invii consuma un po' di ossigeno? Pensa al pianeta!\",\n" +
                    "      \"[prefix] Mi piace leggere i tuoi messaggi, ma non tutti in una volta. Dammene uno al giorno, come una vitamina.\",\n" +
                    "      \"[prefix] Non c'è bisogno di ripeterti, ti ho capito al primo messaggio. Anzi, al primo carattere.\",\n" +
                    "      \"[prefix] Ti prego, smetti di spammare. Mi stai facendo venire il mal di testa. E anche il mal di schiena. E anche il mal di denti.\",\n" +
                    "      \"[prefix] Spammare nella chat è come mangiare troppa pizza: ti fa venire il mal di pancia e poi ti penti. Sii più moderato!\",\n" +
                    "      \"[prefix] Se continui a spammare nella chat, ti mando un virus che ti fa esplodere il computer. Scherzo, ma per favore smettila!\",\n" +
                    "      \"[prefix] Spammare nella chat è una cosa da bambini. Se vuoi essere preso sul serio, devi imparare a comunicare in modo educato e intelligente. O almeno provarci!\",\n" +
                    "      \"[prefix] Spammare nella chat è inutile. Nessuno legge i tuoi messaggi ripetitivi e noiosi. Se vuoi attirare l'attenzione, devi essere originale e creativo!\",\n" +
                    "      \"[prefix] Spammare nella chat è una mancanza di rispetto verso gli altri utenti. Non ti piacerebbe se qualcuno ti interrompesse ogni volta che parli. Quindi perché lo fai tu?\"\n" +
                    "    ],\n" +
                    "    \"link\": [\n" +
                    "      \"[prefix] Non ci interessa il tuo link. Preferiamo parlare di cose più importanti, come il tempo, il calcio o il nuovo album di Tiziano Ferro.\",\n" +
                    "      \"[prefix] Postare dei link in chat è come lanciare delle monetine in una fontana. Spererai che si avverino i tuoi desideri, ma in realtà stai solo sprecando tempo e risorse.\",\n" +
                    "      \"[prefix] Se vuoi condividere un link con noi, assicurati che sia sicuro e interessante. Altrimenti, rischi di ricevere una valanga di emoji arrabbiate.\",\n" +
                    "      \"[prefix] Prima di postare un link, chiediti: \\\"È davvero necessario?\\\". Se la risposta è no, allora non farlo. Se la risposta è sì, allora probabilmente stai mentendo a te stesso.\",\n" +
                    "      \"[prefix] Ricorda che i link sono come le caramelle: vanno bene ogni tanto, ma se ne abusi ti rovinano i denti. E anche la reputazione.\",\n" +
                    "      \"[prefix] Non postare link a meno che non siano assolutamente indispensabili per la conversazione. E no, non lo sono mai. Nemmeno quelli dei gattini.\",\n" +
                    "      \"[prefix] Se vuoi condividere qualcosa d'interessante con gli altri, usa le tue parole. I link sono pigri, impersonali e noiosi. E spesso pericolosi.\"\n" +
                    "    ],\n" +
                    "    \"commands\": [\n" +
                    "      \"[prefix] Non spammare comandi che potrebbero danneggiare il server, altrimenti ti ritroverai con un ban e un sacco di nemici.\",\n" +
                    "      \"[prefix] Se non vuoi fare nulla, va bene, ma non rompere le scatole agli altri utenti con i tuoi tentativi di sabotaggio. Non sei un hacker, sei solo un troll.\",\n" +
                    "      \"[prefix] Evita di spammare comandi inutili o pericolosi, perché non solo rischi di rovinare l'esperienza di tutti, ma anche di far arrabbiare gli admin. E credimi, non vuoi farli arrabbiare.\",\n" +
                    "      \"[prefix] Se ti piace spammare comandi pericolosi, forse dovresti chiederti perché lo fai. Forse hai bisogno di più attenzione o di più amici. O forse sei solo un po' scemo.\",\n" +
                    "      \"[prefix] Ricorda che il server è un luogo di divertimento e di condivisione, non di distruzione e di caos. Se vuoi fare il troll, vai altrove.\",\n" +
                    "      \"[prefix] Se non vuoi fare nulla, va bene, ma non rompere le scatole agli altri con i tuoi comandi inutili. Non sei mica il capo del server.\"\n" +
                    "    ]\n" +
                    "  }\n" +
                    "}")
            conf.create(
                jsonString.convert().getContent() as HashMap<String, Any?>
            )
        }
        conf.reload()
        initLog("Loading mods...")
        // just tell the owner that he's an idiot
        val init = Filter.init()
        if(!init) initLog("Failed to initialize the filter mod!")
        if(!UrlBlock.isActive()) initLog("Failed to initialize the UrlBlock mod!")
        if(!AntiSpam.antiMessageSpamActive()) initLog("Failed to initialize the AntiSpam mod!")
        if(!AntiSpam.antiCommandSpamActive()) initLog("Failed to initialize the AntiCommandSpam mod!")

        initLog("Loading events...")
        Bukkit.getPluginManager().registerEvents(OnChatEvent(), this)

        initLog("Loading commands...")
        getCommand("zm-test")?.setExecutor(TestingCommands())

        initLog("Loading caches...")
        val cacheFolder = File(dataFolder.path, "cache")
        if(!cacheFolder.exists())
            cacheFolder.mkdirs()
        val messageCache = JsonFile(cacheFolder.path, "messages")
        if(!messageCache.exists()) {
            messageCache.create(
                linkedMapOf(
                    "messages" to messagesCache + listOf(MessageCache("porco dio", true))
                )
            )
            messageCache.save()
        }
        messageCache.reload()
        val jsonCache = messageCache["messages"] as List<*>
        jsonCache.forEach { cache ->
            if(cache is MessageCache) {
                messagesCache.add(cache)
            }
        }

        log("================= FINISHED =================")
    }

    override fun onDisable() {
        // save cache
        log("================= CLOSING =================")

        initLog("Saving caches...")
        val cacheFolder = File(dataFolder.path, "cache")
        if(!cacheFolder.exists())
            cacheFolder.mkdirs()
        val messageCache = JsonFile(cacheFolder.path, "messages")
        if(!messageCache.exists()) {
            messageCache.create(linkedMapOf(
                "messages" to messagesCache
            ))
        }else {
            messageCache["messages"] = messagesCache
        }
        messageCache.save()

        log("================= FINISHED =================")
    }
}