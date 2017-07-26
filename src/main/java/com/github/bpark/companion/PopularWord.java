package com.github.bpark.companion;

import edu.mit.jwi.item.POS;

import java.util.Objects;

/**
 * @author ksr
 */
public final class PopularWord {

    private String word;

    private POS pos;

    public PopularWord(String word, POS pos) {
        this.word = word;
        this.pos = pos;
    }

    public String getWord() {
        return word;
    }

    public POS getPos() {
        return pos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PopularWord that = (PopularWord) o;
        return Objects.equals(word, that.word) &&
                pos == that.pos;
    }

    @Override
    public int hashCode() {
        return Objects.hash(word, pos);
    }
}
