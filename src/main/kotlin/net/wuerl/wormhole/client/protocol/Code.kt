package net.wuerl.wormhole.client.protocol

import com.beust.klaxon.JsonArray
import java.io.StringReader
import java.security.SecureRandom
import kotlin.collections.set

data class Code(
    val nameplate: String,
    val password: String,
)

class CodeFactory(
    private val randomWordFactory: RandomWordFactory = RandomWordFactory()
) {
    fun create(nameplate: String): Code {
        val password = randomWordFactory.getOdd() + "-" + randomWordFactory.getEven()
        return Code(nameplate, password)
    }
}

class RandomWordFactory(
    private val wordlist: Wordlist = Wordlist(),
    private val random: SecureRandom = SecureRandom(),
) {

    fun getEven(): String {
        return wordlist.getEven(getRandomIndex())!!
    }

    fun getOdd(): String {
        return wordlist.getOdd(getRandomIndex())!!
    }

    private fun getRandomIndex(): Int = random.nextInt(256)
}

class Wordlist(
    private val wordsResource: String = "/pgpwords.json"
) {
    /*
    see https://en.wikipedia.org/wiki/PGP_word_list
     */

    lateinit var oddWords: Map<Int, String>
    lateinit var evenWords: Map<Int, String>

    init {
        val resource = Code::class.java.getResource(wordsResource)
            ?: throw IllegalStateException("could not get wordllist resource")
        val fileContent = resource.readText()
        val jsonObject = klaxon.parseJsonObject(StringReader(fileContent));
        val oddWords = mutableMapOf<Int, String>()
        val evenWords = mutableMapOf<Int, String>()
        jsonObject.forEach { key, value ->
            if (value is JsonArray<*>) {
                val index = Integer.valueOf(key, 16)
                evenWords[index] = value[0] as String
                oddWords[index] = value[1] as String
            }
        }
        this.oddWords = oddWords.toMap()
        this.evenWords = evenWords.toMap();
    }

    fun getOdd(index: Int): String? {
        return oddWords[index]
    }

    fun getEven(index: Int): String? {
        return evenWords[index]
    }
}