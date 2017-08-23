package com.github.bpark.companion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class QuestionClassificationPreparator {

    private static final OLP olp = OLP.createInstance().withTokenizer().withPosTagger().withSentenceDetector();

    // pattern: begin WP(x) * VBZ(_) * .(?)
    // pattern * WP(who) * VBZ(?) * .(?)

    public void prepare(String question) {
        List<String> tokens = olp.tokenize(question);
        List<String> tags = olp.tag(question);

        List<TokenTag> tokenTags = new ArrayList<>();

        for (int i=0; i<tokens.size(); i++) {
            String token = tokens.get(i);
            String tag = tags.get(i);
            tokenTags.add(new TokenTag(tag, token));
        }

        String feature1 = determineFeatureOne(tokenTags);
        String feature2 = determineFeatureTwo(tokenTags);
        System.out.println(feature1 + " " + feature2);

        List<String> merged = new ArrayList<>();

        for (TokenTag tokenTag : tokenTags) {
            String tag = tokenTag.getTag();
            String token = tokenTag.getToken();
            if (tag.startsWith("WP") || tag.startsWith("WRB")) {
                merged.add(tag + "(" + token.toLowerCase() + ")");
                merged.add("*");
            } else if (tag.startsWith("VB")) {
                merged.add(tag + "(_)");
            } else if (tag.startsWith(".")) {
                merged.add(tag + "(" + token.toLowerCase() + ")");
            } else {
                if (!merged.isEmpty() && !merged.get(merged.size() - 1).equals("*")) {
                    merged.add("*");
                }
            }
        }

        System.out.println(question);
        System.out.println(String.join(" ", tokens));
        System.out.println(String.join(" ", tags));
        System.out.println(String.join(" ", merged));
        System.out.println();
    }

    private String determineFeatureOne(List<TokenTag> tokenTags) {
        TokenTag tokenTag = tokenTags.get(0);
        if (tokenTag.getTag().startsWith("WP") || tokenTag.getTag().startsWith("WRB") || tokenTag.getTag().startsWith("VB")) {
            return "^";
        } else {
            return "*";
        }
    }

    private String determineFeatureTwo(List<TokenTag> tokenTags) {
        TokenTag tokenTag = tokenTags.get(0);
        if (tokenTag.getTag().startsWith("WP") || tokenTag.getTag().startsWith("WRB")) {
            return tokenTag.getTag() + "(" + tokenTag.getToken().toLowerCase() + ")";
        } else if (tokenTag.getTag().startsWith("VB")) {
            return tokenTag.getTag() + "(_)";
        } else {
            return "*";
        }
    }

    public static void main(String[] args) {
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
                "How far is the university from your house?",

                "Call the Police!",
                "The house is green",
                "No"
        );

        QuestionClassificationPreparator preparator = new QuestionClassificationPreparator();

        for (String question : questions) {
            preparator.prepare(question);
        }
    }
}
