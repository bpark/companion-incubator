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
            (tag == "VBP" || tag == "VBZ") && lemma == "have" -> "$tag($lemma)"
            (tag == "VBD" || tag == "VBN") && lemma == "be" -> "$tag($lemma)"
            tag == "VBD" && lemma == "have" -> "$tag($lemma)"
            tag == "VBP" || tag == "VBG" || tag == "VBZ" -> tag
            tag == "MD" && token == "will" -> "$tag($token)"
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

    val simplePresentPerfect = listOf<String>("I have worked.", "I haven't worked.", "Have I worked?", "He has worked.", "He hasn't worked.", "Has he worked?",
            "I have gone.", "I haven't gone.", "Have I gone?", "He has gone.", "He hasn't gone.", "Has he gone?")

    val presentPerfectProgressive = listOf<String>("I have been working.", "I haven't been working.", "Have I been working?", "He has been working.",
            "He hasn't been working.", "Has he been working?", "I have been going.", "I haven't been going.", "Have I been going?", "He has been going.",
            "He hasn't been going.", "Has he been going?")

    val simplePastPerfect = listOf<String>("I had worked.", "I hadn't worked.", "Had I worked?", "He had worked.", "He hadn't worked.", "Had he worked?",
            "I had gone.", "I hadn't gone.", "Had I gone?", "He had gone.", "He hadn't gone.", "Had he gone?")

    val pastPerfectProgressive = listOf<String>("I had been working.", "I hadn't been working.", "Had I been working?", "He had been working.",
            "He hadn't been working.", "Had he been working?", "I had been going.", "I hadn't been going.", "Had I been going?", "He had been going.",
            "He hadn't been going.", "Had he been going?")

    val willFuture = listOf<String>("I'll work.", "I won't work.", "Will I work?", "He'll work.", "He won't work.", "Will he work?", "I'll go.",
            "I won't go.", "Will I go?", "He'll go.", "He won't go.", "Will he go?")

    val tensePhrases = listOf<List<String>>(simplePresent, presentProgressive, simplePast, pastProgressive,
            simplePresentPerfect, presentPerfectProgressive, simplePastPerfect, pastPerfectProgressive, willFuture)

    val path = "dict"
    val dictionary = Dictionary(File(path))
    val stemmer = WordnetStemmer(dictionary)

    init {
        dictionary.open()

    }

    fun buildBag(wordinfos: List<WordInfo>): List<String> {
        val verbs = wordinfos.filter { it.tag.startsWith("V") || (it.tag == "MD" && it.token == "will") }

        return verbs.mapIndexed { index, verb -> verb.map() }

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

    TenseDetection.tensePhrases.forEach {
        it.forEach {
            val wordinfos = TenseDetection.buildWordInfo(it)
            val bags = TenseDetection.buildBag(wordinfos)
            println("$it: $bags -> $wordinfos")
        }
        println()
    }

}
