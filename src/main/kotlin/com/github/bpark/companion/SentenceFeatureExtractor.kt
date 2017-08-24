package com.github.bpark.companion

typealias AnalyzedToken = Pair<String, String>

class SentenceFeatureExtractor {

    private val olp = OLP.createInstance().withPosTagger().withTokenizer().withSentenceDetector()

    fun prepare(sentence: String) {

        val tokens = olp.tokenize(sentence)
        val tags = olp.tag(sentence)

        val analyzedTokens = mapToAnalyzed(tokens, tags)
        val filteredTokens = filterRelevant(analyzedTokens)
        val shrinkedTokens = shrink(filteredTokens)
        val tokensWithStart = defineStart(shrinkedTokens)

        println()
        println(sentence)
        println(analyzedTokens.joinToString(" "))
        println(filteredTokens.joinToString(" "))
        println(shrinkedTokens.joinToString(" "))
        println(tokensWithStart.joinToString(" "))

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
            if (tag == "JJ" || tag == "RB") {
                tag = "J"
            }
            tokenTags.add(Pair(token, tag))
        }

        return tokenTags
    }

    private fun filterRelevant(analyzedTokens: List<AnalyzedToken>): List<AnalyzedToken> {
        return analyzedTokens.map { if (startsWith(it.second, listOf("WH", "V", "J"))) it else AnalyzedToken("", "*") }
    }

    private fun shrink(analyzedTokens: List<AnalyzedToken>): List<AnalyzedToken> {
        val filtered = analyzedTokens.toMutableList()

        for (tag in listOf("WH", "V", "J")) {
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

    /*
    private fun fill(analyzedTokens: List<AnalyzedToken>) : List<AnalyzedToken> {
        analyzedTokens
        analyzedTokens.toMutableList().
    }*/

    private fun startsWith(item: String, list: List<String>): Boolean {
        return list.stream().anyMatch { item.startsWith(it) }
    }

}

fun main(args: Array<String>) {
    val sentenceFeatureExtractor = SentenceFeatureExtractor()
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
}
