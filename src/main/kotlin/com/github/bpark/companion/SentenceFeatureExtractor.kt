package com.github.bpark.companion

typealias AnalyzedToken = Pair<String, String>

interface FeatureExtractor {

    fun extract(tokenTags: List<Pair<String, String>>, features: MutableList<Pair<String, String>> = mutableListOf()): MutableList<Pair<String, String>>
}

class StartFeatureExtractor : FeatureExtractor {

    private val start = Pair("", "^")
    private val any = Pair("", "*")
    private val startTags = listOf("WP", "WRB", "VB")

    override fun extract(tokenTags: List<Pair<String, String>>, features: MutableList<Pair<String, String>>): MutableList<Pair<String, String>> {
        val (_, tag) = tokenTags.first()

        if (startTags.stream().anyMatch { tag.startsWith(it) }) features.add(start) else features.add(any)

        return features;
    }

}

class FirstValueFeatureExtractor : FeatureExtractor {

    override fun extract(tokenTags: List<Pair<String, String>>, features: MutableList<Pair<String, String>>): MutableList<Pair<String, String>> {
        val (token, tag) = tokenTags.first()


        if (tag.startsWith("WP") || tag.startsWith("WRB")) {
            features.add(Pair("WH", token));
        } else if (tag.startsWith("VB")) {
            features.add(Pair("V", "_"))
        } else {
            features.add(Pair("", "*"))
        }

        return features;
    }

}

class SecondValueFeatureExtractor : FeatureExtractor {

    override fun extract(tokenTags: List<Pair<String, String>>, features: MutableList<Pair<String, String>>): MutableList<Pair<String, String>> {

        val (id, _) = features.last();

        val hasWh = id.startsWith("WH")
        var hasJ = false;
        var hasV = features.contains(Pair("V", "_"));


        for ((token, tag) in tokenTags.drop(1)) {
            if (hasWh && !hasJ && (tag == "JJ" || tag == "RB")) {
                features.add(Pair("J", token))
                hasJ = true
            } else if (hasWh && !hasV && tag.startsWith("VB")) {
                if (features.last() != Pair("", "*") && features.last().first != "J") {
                    features.add(Pair("", "*"))
                }
                features.add(Pair("V", "_"))
                hasV = true
            } else if (features.last() != Pair("", "*") && tag != "*") {
                features.add(Pair("", "*"))
            }
        }
        return features
    }

}


class SentenceFeatureExtractor {

    private val olp = OLP.createInstance().withPosTagger().withTokenizer().withSentenceDetector()

    private val keepTokens = listOf("WP", "WRB", "VB", "JJ", "RB", ".")

    fun prepare(sentence: String) {

        val tokens = olp.tokenize(sentence)
        val tags = olp.tag(sentence)

        val tokenTags = mapToAnalyzed(tokens, tags)

        println()
        println(sentence)
        println(tokenTags.joinToString(" "))

        val featureExtractors = listOf(StartFeatureExtractor(), FirstValueFeatureExtractor(), SecondValueFeatureExtractor())

        val features = mutableListOf<Pair<String, String>>()

        featureExtractors.forEach { it.extract(tokenTags, features) }

        val pattern = features.joinToString(" ");

        println(pattern)
    }

    private fun mapToAnalyzed(tokens: List<String>, tags: List<String>): List<AnalyzedToken> {
        val tokenTags = mutableListOf<Pair<String, String>>()

        for ((index, token) in tokens.withIndex()) {
            val tag = tags[index]
            tokenTags.add(Pair(token, tag))
        }

        return tokenTags;
    }

    private fun filterRelevant(analyzedTokens: List<AnalyzedToken>) {
        
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
}
