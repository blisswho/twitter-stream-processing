package com.twitteranalyzer;

public class TweetPayload {
    private String message;
    private long time;
    public TweetPayload (String message, long time) {
        this.message = message;
        this.time = time;
    }
}
