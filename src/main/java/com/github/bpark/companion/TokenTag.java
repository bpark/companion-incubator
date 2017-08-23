package com.github.bpark.companion;

/**
 * @author ksr
 */
public class TokenTag {

    private String tag;

    private String token;

    public TokenTag(String tag, String token) {
        this.tag = tag;
        this.token = token;
    }

    public String getTag() {
        return tag;
    }

    public String getToken() {
        return token;
    }
}
