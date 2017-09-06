package com.github.bpark.companion


/*
VB	Verb, base form
28.	VBD	Verb, past tense
29.	VBG	Verb, gerund or present participle
30.	VBN	Verb, past participle
31.	VBP	Verb, non-3rd person singular present
32.	VBZ	Verb, 3rd person singular present
 */

object TenseDetection {

    val olp = OLP.createInstance().withTokenizer().withPosTagger();

    val simplePresent = listOf<String>("I work", "I don't work.", "Do I work?", "He works.", "He doesn't work.", "Does he work?",
            "I go", "I don't go.", "Do I go?", "He goes.", "He doesn't go.", "Does he go?");

    val presentProgressive = listOf<String>("I'm working.", "I'm not working.", "Am I working?", "He's working.", "He isn't working.",
            "Is he working?", "I'm going.", "I'm not going.", "Am I going?", "He's going.", "He isn't going.", "Is he going?")


}

fun main(args: Array<String>) {

    TenseDetection.simplePresent.forEach { println("$it -> " + TenseDetection.olp.tag(it)) }
    println()
    TenseDetection.presentProgressive.forEach { println("$it -> " + TenseDetection.olp.tag(it)) }
}
