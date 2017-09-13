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

    fun map(): String {
        return when {
            (tag == "VBP" || tag == "VBZ") && lemma == "be" -> "$tag($lemma)"
            tag == "VBD" && lemma == "be" -> "$tag($lemma)"
            tag == "VBP" || tag == "VBG" || tag == "VBZ" -> tag
            else -> tag
        }
    }
}

object TenseDetection {

    val olp = OLP.createInstance().withTokenizer().withPosTagger();

    val simplePresent = listOf<String>("I work", "I don't work.", "Do I work?", "He works.", "He doesn't work.", "Does he work?",
            "I go", "I don't go.", "Do I go?", "He goes.", "He doesn't go.", "Does he go?");

    val presentProgressive = listOf<String>("He's there", "I'm working.", "I'm not working.", "Am I working?", "He's working.", "He isn't working.",
            "Is he working?", "I'm going.", "I'm not going.", "Am I going?", "He's going.", "He isn't going.", "Is he going?")

    val simplePast = listOf<String>("I worked.", "I didn't work.", "Did I work?", "He worked.", "He didn't work.", "Did he work?",
            "I went.", "I didn't go.", "Did I go?", "He went.", "He didn't go.", "Did he go?")

    val pastProgressive = listOf<String>("I was working.", "I wasn't working.", "Was I working?", "He was working.", "He wasn't working.",
            "Was he working?", "I was going.", "I wasn't going.", "Was I going?", "He was going.", "He wasn't going.", "Was he going?")

    val path = "dict"
    val dictionary = Dictionary(File(path))
    val stemmer = WordnetStemmer(dictionary)

    init {
        dictionary.open()

    }

    fun buildBag(wordinfos: List<WordInfo>): List<String> {
        val verbs = wordinfos.filter { it.tag.startsWith("V") }

        return verbs.mapIndexed { index, verb -> verb.map() }

    }

    fun buildWordInfo(sentence: String): List<WordInfo> {
        val tags = olp.tag(sentence)
        val tokens = olp.tokenize(sentence)

        val wordInfos = mutableListOf<WordInfo>()

        tags.forEachIndexed { index, tag ->
            run {
                val token = tokens[index]
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

        normalForm = normalForm.replace("n't".toRegex(), " not")
        normalForm = normalForm.replace("'re".toRegex(), " are")
        normalForm = normalForm.replace("'m".toRegex(), " am")
        normalForm = normalForm.replace("'ll".toRegex(), " will")
        normalForm = normalForm.replace("'ve".toRegex(), " have")

        // conversional
        normalForm = normalForm.replace("'d".toRegex(), "would")
        normalForm = normalForm.replace("'s".toRegex(), "is")

        return normalForm
    }


}

fun main(args: Array<String>) {

    TenseDetection.simplePresent.forEach {
        val wordinfos = TenseDetection.buildWordInfo(it)
        val bags = TenseDetection.buildBag(wordinfos)
        println("$it: $bags -> $wordinfos")
    }
    println()
    TenseDetection.presentProgressive.forEach {
        val wordinfos = TenseDetection.buildWordInfo(it)
        val bags = TenseDetection.buildBag(wordinfos)
        println("$it: $bags -> $wordinfos")
    }
    println()
    TenseDetection.simplePast.forEach {
        val wordinfos = TenseDetection.buildWordInfo(it)
        val bags = TenseDetection.buildBag(wordinfos)
        println("$it: $bags -> $wordinfos")
    }
    println()
    TenseDetection.pastProgressive.forEach {
        val wordinfos = TenseDetection.buildWordInfo(it)
        val bags = TenseDetection.buildBag(wordinfos)
        println("$it: $bags -> $wordinfos")
    }

}
