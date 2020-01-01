package com.twitteranalyzer;

import com.google.gson.Gson;
import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Client;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.HttpHosts;
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;
import org.apache.commons.text.StringEscapeUtils;

import java.util.Collections;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TwitterStreamExample {

    public static final String CONSUMER_API_KEY = "nM1YFxK3uEqnzo50zMIu4RMeG";
    public static final String CONSUMER_API_SECRET_KEY = "umQHP7BJap15khFNmuCxmmrBiKPnDwRTGyyyAyZp98dr97h9r7";
    public static final String ACCESS_TOKEN = "1003359802125438976-E6hAVHk2XVI3PbQeYUU5K8RsAYx7hy";
    public static final String ACCESS_TOKEN_SECRET = "2cBspUqBVB3RUqo3lGaV0iooMzm90uafeKFYunX8U0lE5";
    public static final String HASHTAG = "#trump";

    public static void main(String[] args){
        Authentication hosebirdAuth = new OAuth1(
                CONSUMER_API_KEY,
                CONSUMER_API_SECRET_KEY,
                ACCESS_TOKEN,
                ACCESS_TOKEN_SECRET);

        // Track list of terms here
        StatusesFilterEndpoint hosebirdEndpoint = new StatusesFilterEndpoint();
        hosebirdEndpoint.trackTerms(Collections.singletonList(HASHTAG));

        BlockingQueue<String> msgQueue = new LinkedBlockingQueue<String>(10000);

        //Twitter Client Configuration
        ClientBuilder builder = new ClientBuilder()
                .hosts(new HttpHosts(Constants.STREAM_HOST))
                .authentication(hosebirdAuth)
                .endpoint(hosebirdEndpoint)
                .processor(new StringDelimitedProcessor(msgQueue));

        Client hosebirdClient = builder.build();
        Gson gson = new Gson();

        //Twitter Client Execution
        hosebirdClient.connect();
        System.out.println("Hosebird Client Connected");

        while(!hosebirdClient.isDone()) {
            try {
                String take = msgQueue.take();
                String msg = "";
                String full_text = "\"full_text\"";
                String display_text = "display_text_range";
                Tweet tweet = gson.fromJson(take, Tweet.class);

                System.out.println(take);

                if(take.contains(full_text)){
                    int start = take.indexOf(full_text) + (full_text.length()) + 1;
                    String new_str = take.substring(start);
                    int end = new_str.indexOf(display_text)-2;
                    System.out.println(StringEscapeUtils.unescapeJava(new_str.substring(0, end)));
                }else{
                    System.out.println(tweet.getText());
                }

                System.out.println("===================================");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        hosebirdClient.stop();
        System.out.println("Hosebird Client Stopped");
    }
}
