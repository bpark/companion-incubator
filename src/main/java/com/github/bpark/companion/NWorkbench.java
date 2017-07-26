package com.github.bpark.companion;

import opennlp.tools.parser.Parse;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author ksr
 */
public class NWorkbench {

    private OLP olp = OLP.createInstance().withTokenizer().withPosTagger().withParser().withSentenceDetector();

    public void analyze(String content) {

        List<String> sentences = olp.sentence(content);

        Pattern pattern = Pattern.compile("\\(TOP\\s\\((SQ|SBAR|SBARQ)\\s.*");

        sentences.forEach(sentence -> {

            List<Parse> topParses = olp.parse(sentence);
            topParses.forEach(p -> {
                StringBuffer sb = new StringBuffer();
                p.show(sb);
                System.out.println(sb);

                Matcher m = pattern.matcher(sb);
                boolean b = m.matches();

                System.out.println(b);

            });


            List<String> tokens = olp.tokenize(sentence);
            List<String> posTags = olp.tag(sentence);

            System.out.println(String.join(", ", tokens));
            System.out.println(String.join(", ", posTags));

            System.out.println("interrogative: " + detectInterrogative(tokens, posTags));

            System.out.println(QuestionType.evaluate(tokens, posTags));
        });
    }

    private boolean detectInterrogative(List<String> tokens, List<String> posTags) {

        return (hasStartingSequence(posTags, "WRB")
                || hasStartingSequence(posTags, "MD")
                || hasStartingSequence(posTags, "WDT")
                || hasStartingSequence(posTags, "WP")
                || hasStartingSequence(posTags, "VBP", "PRP")
                || hasStartingSequence(posTags, "WP", "VBP", "PRP", "VB"))
                && !tokens.get(tokens.size() - 1).equals("!");
    }

    private boolean hasStartingSequence(List<String> posTags, String... sequence) {

        boolean hasSequence = false;

        if (posTags.get(0).equals(sequence[0])) {

            hasSequence = true;

            int lastPosition = 0;
            int currentPosition = 0;
            for (int i = 1; i < sequence.length; i++) {
                String seq = sequence[i];
                for (int j = 1; j < posTags.size(); j++) {
                    String posTag = posTags.get(j);
                    if (seq.equals(posTag)) {
                        currentPosition = j;
                        break;
                    }
                }
                if (currentPosition > lastPosition) {
                    lastPosition = currentPosition;
                } else {
                    hasSequence = false;
                    break;
                }
            }
        }

        return hasSequence;
    }

    public static void main(String[] args) throws Exception {
        NWorkbench nWorkbench = new NWorkbench();

        List<String> questions = Arrays.asList(
                "Who is the best football player in the world?",
                "Who are your best friends?",
                "Who is that strange guy over there?",

                "Where is the library?",
                "Where do you live?",
                "Where are my shoes?",

                "When do the shops open?",
                "When is his birthday?",
                "When are we going to finish?",

                "Why do we need a nanny?",
                "Why are they always late?",
                "Why does he complain all the time?",

                "What is your name?",
                "What is her favourite colour?",
                "What is the time?",

                "Which drink did you order?",
                "Which day do you prefer for a meeting",
                "Which is better - this one or that one?",

                "How do you cook paella?",
                "How does he know the answer?",
                "How can I learn English quickly?",

                "How much money will I need?",
                "How much time do you have to finish the test?",
                "How much is the jacket on display in the window?",

                "How many brothers and sister do you have?",
                "How many days are there in April?",
                "How many people live in this city?",

                "How often does she study?",
                "How often do you visit your grandmother?",
                "How often are you sick?",

                "How far is the bus stop from here?",
                "How far is the university from your house?"
                );

        for (String question : questions) {
            nWorkbench.analyze(question);
            System.out.println();
        }
    }

}
