package com.github.bpark.companion

import edu.mit.jwi.Dictionary
import edu.mit.jwi.morph.WordnetStemmer
import java.io.File


/*
VB	Verb, base form
28.	VBD	Verb, past tense
29.	VBG	Verb, gerund or present participle
30.	VBN	Verb, past participle
31.	VBP	Verb, non-3rd person singular present
32.	VBZ	Verb, 3rd person singular present
 */

data class WordInfo(val token: String, val tag: String, val lemma: String?) {

    fun map(index: Int): String {
        return when {
            (tag == "VBP" || tag == "VBZ") && lemma == "be" -> "$tag($lemma,$index)"
            (tag == "VBP" || tag == "VBZ") && lemma == "have" -> "$tag($lemma,$index)"
            (tag == "VBD" || tag == "VBN") && lemma == "be" -> "$tag($lemma,$index)"
            (tag == "VBD" || tag == "VB") && lemma == "have" -> "$tag($lemma,$index)"
            tag == "VBG" && token == "going" -> "$tag($token,$index)"
            tag == "MD" && (token == "will" || token == "would") -> "$tag($token,$index)"
            tag == "VBP" || tag == "VBG" || tag == "VBZ" -> "$tag($index)"
            else -> tag
        }
    }
}

object TenseDetection {

    val olp = OLP.createInstance().withTokenizer().withPosTagger()

    val path = "dict"
    val dictionary = Dictionary(File(path))
    val stemmer = WordnetStemmer(dictionary)

    init {
        dictionary.open()

    }

    fun buildBag(wordinfos: List<WordInfo>): List<String> {
        val verbs = wordinfos.filter {
                    it.tag.startsWith("V") ||
                    (it.tag == "MD" && (it.token == "will" || it.token == "would")) ||
                    it.tag == "TO"
        }

        return verbs.mapIndexed { index, verb -> verb.map(index) }

    }

    fun loadSentences(): Map<String, List<String>> {

        val sentenceMap = mutableMapOf<String, MutableList<String>>()

        File("./src/main/resources/tenses.txt").useLines { lines -> lines.forEach {
            val splits = it.split(",")
            sentenceMap.putIfAbsent(splits[0], mutableListOf())?.add(splits[1]) ?: sentenceMap[splits[0]]?.add(splits[1])
        } }

        return sentenceMap
    }

    fun buildWordInfo(sentence: String): List<WordInfo> {
        val tags = olp.tag(sentence)
        val tokens = olp.tokenize(sentence)

        val wordInfos = mutableListOf<WordInfo>()

        tags.forEachIndexed { index, tag ->
            run {
                val token = tokens[index].toLowerCase()
                val word = removeContractions(token)
                var lemma: String? = null

                val pos = PosType.byPennTag(tag)
                if (pos != null) {

                    val stems = stemmer.findStems(word, pos)
                    val stem = if (stems != null && stems.size > 0) stems[0] else word

                    val idxWord = dictionary.getIndexWord(stem, pos)

                    if (idxWord?.wordIDs != null && idxWord.wordIDs.size > 0) {

                        val wordID = idxWord.wordIDs.first() // 1st meaning
                        val dictionaryWord = dictionary.getWord(wordID)
                        lemma = dictionaryWord.lemma
                    }
                }

                wordInfos.add(WordInfo(word, tag, lemma))

            }
        }

        return wordInfos
    }

    fun removeContractions(inputString: String): String {

        var normalForm = inputString

        normalForm = normalForm.replace("^wo$".toRegex(), "will")
        normalForm = normalForm.replace("n't".toRegex(), "not")
        normalForm = normalForm.replace("'re".toRegex(), "are")
        normalForm = normalForm.replace("'m".toRegex(), " am")
        normalForm = normalForm.replace("'ll".toRegex(), "will")
        normalForm = normalForm.replace("'ve".toRegex(), "have")

        // conversional
        normalForm = normalForm.replace("'d".toRegex(), "would")
        normalForm = normalForm.replace("'s".toRegex(), "is")

        return normalForm
    }


}

fun main(args: Array<String>) {

    val sentenceMap = TenseDetection.loadSentences()

    val content = mutableListOf<String>()

    sentenceMap.forEach { tense, sentences -> run {
        println(tense)
        sentences.forEach {
            val wordinfos = TenseDetection.buildWordInfo(it.replace("\"", ""))
            val bags = TenseDetection.buildBag(wordinfos)
            println("$it: $bags -> $wordinfos")
            content.add("$tense,\"" + bags.joinToString(" ") + "\"")
        }
        println()
    } }

    File("tenses.arff").writeText(content.joinToString("\n"))

}
