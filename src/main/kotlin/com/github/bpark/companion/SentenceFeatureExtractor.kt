package com.github.bpark.companion


class SentenceFeatureExtractor {

    val start = "^"
    val any = "*"
    val startTags = listOf("WP", "WRB", "VB")

    fun prepare(sentence: String) {
        val olp = OLP.createInstance().withPosTagger().withTokenizer().withSentenceDetector()

        val tokens = olp.tokenize(sentence)
        val tags = olp.tag(sentence)

        val tokenTags = mutableListOf<Pair<String, String>>()

        for ((index, token) in tokens.withIndex()) {
            val tag = tags[index]
            tokenTags.add(Pair(token, tag))
        }

        val startFeature = extractStartFeature(tokenTags)
        val valueFeature = extractFirstValueFeature(tokenTags)

        println("$startFeature $valueFeature")
    }

    fun extractStartFeature(tokenTags: List<Pair<String, String>>): String {
        val (_, tag) = tokenTags.first()

        return if (startTags.stream().anyMatch { tag.startsWith(it) }) start else any

    }

    fun extractFirstValueFeature(tokenTags: List<Pair<String, String>>): String {
        val (token, tag) = tokenTags.first()

        return if (tag.startsWith("WP") || tag.startsWith("WRB")) {
            "$tag($token)"
        } else if (tag.startsWith("VB")) {
            "$tag(_)"
        } else {
            "*"
        }
    }
}

fun main(args: Array<String>) {
    val sentenceFeatureExtractor = SentenceFeatureExtractor()
    sentenceFeatureExtractor.prepare("What is your name?")
    sentenceFeatureExtractor.prepare("The word what introduces a question?")
}
