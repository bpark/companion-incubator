package com.github.bpark.companion;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

/**
 * @author ksr
 */
public enum QuestionType {

    PEOPLE ("who", "WP"),
    LOCATION ("where", "WRB"),
    OCCASION ("when", "WRB"),
    REASON ("why", "WRB"),
    INFORMATION ("what", "WP"),
    CHOICE ("which", "WDT"),
    QUANTITY (asList("how", "much"), asList("WRB", "JJ")),
    FREQUENCY (asList("how", "often"), asList("WRB", "RB")),
    DISTANCE (asList("how", "far"), asList("WRB", "RB")),
    DESCRIPTION ("how", "WRB");

    private List<String> tokenSequence;

    private List<String> tagSequence;

    QuestionType(List<String> tokenSequence, List<String> tagSequence) {
        this.tokenSequence = tokenSequence;
        this.tagSequence = tagSequence;
    }

    QuestionType(String token, String tag) {
        this(singletonList(token), singletonList(tag));
    }

    public static QuestionType evaluate(List<String> tokens, List<String> tags) {

        List<String> loweredTokens = tokens.stream().map(String::toLowerCase).collect(Collectors.toList());

        QuestionType resultType = null;
        for (QuestionType questionType : QuestionType.values()) {
            if (questionType.containsTagSequence(tags) && questionType.containsTokenSequence(loweredTokens)) {
                resultType = questionType;
                break;
            }
        }
        return resultType;
    }

    private boolean containsTokenSequence(List<String> tokens) {
        return Collections.indexOfSubList(tokens, tokenSequence) != -1;
    }

    private boolean containsTagSequence(List<String> tags) {
        return Collections.indexOfSubList(tags, tagSequence) != -1;
    }

}
