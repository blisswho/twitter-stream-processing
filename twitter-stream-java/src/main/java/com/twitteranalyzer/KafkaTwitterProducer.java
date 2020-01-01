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

import java.util.Collections;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import java.util.Properties;

import static com.twitteranalyzer.ConfigKeys.*;

public class KafkaTwitterProducer {

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

        //Kafka Producer Configuration
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092, localhost:9093, localhost:9094");
        props.put("acks", "all");
        props.put("key.serializer", StringSerializer.class.getName());
        props.put("value.serializer", StringSerializer.class.getName());

        Producer<String, String> producer = new KafkaProducer<String, String>(props);

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
                if(take.contains(full_text)){
                    int start = take.indexOf(full_text) + (full_text.length()) + 1;
                    String new_str = take.substring(start);
                    int end = new_str.indexOf(display_text)-2;
                    msg = StringEscapeUtils.unescapeJava(new_str.substring(0, end));
                }else{
                    msg = tweet.getText();
                }
                System.out.println(msg);
                TweetPayload payload = new TweetPayload(msg, System.currentTimeMillis() / 1000l);
                producer.send(new ProducerRecord<String, String>("my-replicated-topic", Long.toString(tweet.getId()), gson.toJson(payload)));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
