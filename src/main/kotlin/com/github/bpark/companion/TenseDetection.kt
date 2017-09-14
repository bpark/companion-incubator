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
            tag == "VBG" && token == "going" -> "$tag($token)"
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

    val goingToFuture = listOf<String>("I'm going to work.", "I'm not going to work.", "Am I going to work?", "He's going to work.", "He's not going to work.",
            "Is he going to work?", "I'm going to go.", "I'm not going to go.", "Am I going to go?", "He's going to go.", "He's not going to go.",
            "Is he going to go?")

    val simpleFuturePerfect = listOf<String>("I'll have worked.", "I won't have worked.", "Will I have worked?", "He'll have worked.", "He won't have worked.",
            "Will he have worked?", "I'll have gone.", "I won't have gone.", "Will I have gone?", "He'll have gone.", "He won't have gone.", "Will he have gone?")

    val futurePerfectProgressive = listOf<String>("I'll have been working.", "I won't have been working.", "Will I have been working?", "He'll have been working.",
            "He won't have been working.", "Will he have been working?", "I'll have been going.", "I won't have been going.", "Will I have been working?",
            "He'll have been going.", "He won't have been going.", "Will he have been working?")

    val conditionalSimple = listOf<String>("I would work.", "I wouldn't work.", "Would I work?", "He would work.", "He wouldn't work.", "Would he work?",
            "I would go.", "I wouldn't go.", "Would I go?", "He would go.", "He wouldn't go.", "Would he go?")

    val conditionalProgressive = listOf<String>("I would be working.", "I wouldn't be working.", "Would I be working?", "He would be working.",
            "He wouldn't be working.", "Would he be working?", "I would be going.", "I wouldn't be going.", "Would I be going?", "He would be going.",
            "He wouldn't be going.", "Would he be going?")

    val conditionalPerfect = listOf<String>("I would have worked.", "I wouldn't have worked.", "Would I have worked?", "He would have worked.",
            "He wouldn't have worked.", "Would he have worked?", "I would have gone.", "I wouldn't have gone.", "Would I have gone?",
            "He would have gone.", "He wouldn't have gone.", "Would I have gone?")

    val conditionalPerfectProgressive = listOf<String>("I would have been working.", "I wouldn't have been working.", "Would I have been working?",
            "He would have been going.", "He wouldn't have been going.", "Would he have been working?", "I would have been going.",
            "I wouldn't have been going.", "Would I have been going?", "He would have been going.", "He wouldn't have been going.",
            "Would he have been going?")

    val tensePhrases = listOf<List<String>>(simplePresent, presentProgressive, simplePast, pastProgressive,
            simplePresentPerfect, presentPerfectProgressive, simplePastPerfect, pastPerfectProgressive, willFuture,
            goingToFuture)

    val path = "dict"
    val dictionary = Dictionary(File(path))
    val stemmer = WordnetStemmer(dictionary)

    init {
        dictionary.open()

    }

    fun buildBag(wordinfos: List<WordInfo>): List<String> {
        val verbs = wordinfos.filter {
                    it.tag.startsWith("V") ||
                    (it.tag == "MD" && it.token == "will") ||
                    it.tag == "TO"
        }

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
