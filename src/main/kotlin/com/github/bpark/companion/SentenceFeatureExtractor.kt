package com.github.bpark.companion

import org.apache.commons.io.IOUtils
import java.nio.charset.StandardCharsets

typealias AnalyzedToken = Pair<String, String>

class SentenceFeatureExtractor {

    private val olp = OLP.createInstance().withPosTagger().withTokenizer()

    fun prepare(sentence: String): List<AnalyzedToken> {

        val tokens = olp.tokenize(sentence)
        val tags = olp.tag(sentence)


        val analyzedTokens = mapToAnalyzed(tokens, tags)
        val filteredTokens = filterRelevant(analyzedTokens)
        val shrinkedTokens = shrink(filteredTokens)
        val tokensWithStart = defineStart(shrinkedTokens)
        val filledTokens = fill(tokensWithStart)

        println("{")
        println("    \"sentence\": \"$sentence\",")
        println("    \"tokens\": [" + tokens.joinToString("\", \"", "\"",  "\"") + "],")
        println("    \"tags\": [" + tags.joinToString("\", \"", "\"",  "\"") + "]")
        //println("    \"result\": [" + filledTokens.joinToString("\", \"", "\"",  "\"") + "]")
        if (filledTokens.size > 8) println("**** greater 8")
        //println(analyzedTokens.joinToString(" "))
        //println(filteredTokens.joinToString(" "))
        //println(shrinkedTokens.joinToString(" "))
        //println(tokensWithStart.joinToString(" "))
        println("},")

        return filledTokens

    }

    private fun mapToAnalyzed(tokens: List<String>, tags: List<String>): List<AnalyzedToken> {
        val tokenTags = mutableListOf<Pair<String, String>>()

        for ((index, token) in tokens.withIndex()) {
            var tag = tags[index]
            if (startsWith(tag, listOf("WRB", "WP"))) {
                tag = "WH"
            }
            if (tag.startsWith("VB")) {
                tag = "V"
            }
            if (tag.startsWith("J") || tag == "RB") {
                tag = "J"
            }
            tokenTags.add(Pair(token, tag))
        }

        if (tags.last() != ".") {
            tokenTags.add(AnalyzedToken(".", "."))
        }

        return tokenTags
    }

    private fun filterRelevant(analyzedTokens: List<AnalyzedToken>): List<AnalyzedToken> {
        val filteredTokens = analyzedTokens.map { if (startsWith(it.second, listOf("WH", "V", "J", "."))) it else AnalyzedToken("", "*") }.toMutableList()
        filteredTokens.forEachIndexed { index, pair ->
            run {
                if (pair.second == "V") filteredTokens.set(index, AnalyzedToken("_", "V"))
                if (pair.second == "J" && !listOf<String>("much", "often", "many", "far").contains(pair.first) ) filteredTokens.set(index, AnalyzedToken("", "*"))
            }
        }
        return filteredTokens
    }

    private fun shrink(analyzedTokens: List<AnalyzedToken>): List<AnalyzedToken> {
        val filtered = analyzedTokens.toMutableList()

        for (tag in listOf("WH", "V", "J", ".")) {
            val index = filtered.indexOfFirst { it.second == tag }
            if (index != -1) {
                val first = filtered[index]
                filtered.removeAll { it.second == tag }
                filtered.add(index, first)
            }
        }

        val reduced = mutableListOf<AnalyzedToken>()

        filtered.forEach { if (reduced.lastOrNull() != it) reduced.add(it) }

        return reduced
    }

    private fun defineStart(analyzedTokens: List<AnalyzedToken>): List<AnalyzedToken> {
        val (_, tag) = analyzedTokens.first()

        val tokens = analyzedTokens.toMutableList()

        if (tag == "WH" || tag == "V") {
            tokens.add(0, AnalyzedToken("", "^"))
        } else if (tag != "*") {
            tokens.add(0, AnalyzedToken("", "*"))
        }

        return tokens
    }

    private fun fill(analyzedTokens: List<AnalyzedToken>) : List<AnalyzedToken> {

        val tokens = analyzedTokens.toMutableList()

        while (tokens.size < 8) {
            tokens.add(tokens.size - 1, AnalyzedToken("", "*"))
        }

        return tokens
    }

    private fun startsWith(item: String, list: List<String>): Boolean {
        return list.stream().anyMatch { item.startsWith(it) }
    }

}

fun main(args: Array<String>) {
    val sentenceFeatureExtractor = SentenceFeatureExtractor()

    val content = IOUtils.toString(SentenceFeatureExtractor::class.java.getResourceAsStream("/corpus0.txt"), StandardCharsets.UTF_8)

    val olp = OLP.createInstance().withSentenceDetector()

    val sentences = olp.sentence(content)

    sentences.forEach {
        sentenceFeatureExtractor.prepare(it)
    }

    /*

    sentenceFeatureExtractor.prepare("What is your name?")
    sentenceFeatureExtractor.prepare("The word what introduces a question?")
    sentenceFeatureExtractor.prepare("How do you cook paella?")

    sentenceFeatureExtractor.prepare("How much money will I need?")

    sentenceFeatureExtractor.prepare("How many brothers and sister do you have?")

    sentenceFeatureExtractor.prepare("How often does she study?")

    sentenceFeatureExtractor.prepare("How far is the bus stop from here?")

    sentenceFeatureExtractor.prepare("The word what is a question word.")
    sentenceFeatureExtractor.prepare("The word what is question word.")
    sentenceFeatureExtractor.prepare("Do it now!")
    sentenceFeatureExtractor.prepare("What does the word what mean")
    sentenceFeatureExtractor.prepare("Hello")
    sentenceFeatureExtractor.prepare("This is not true!!!!!!") */
}
