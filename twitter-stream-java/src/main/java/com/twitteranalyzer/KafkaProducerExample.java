package com.twitteranalyzer;

import com.google.gson.Gson;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import java.util.Properties;

public class KafkaProducerExample {
    public static void main(String[] args){
        System.out.println("Hello, world!");
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092, localhost:9093, localhost:9094");
        props.put("acks", "all");
        props.put("key.serializer", StringSerializer.class.getName());
        props.put("value.serializer", StringSerializer.class.getName());

        Producer<String, String> producer = new KafkaProducer<String, String>(props);
        for (int i = 100; i < 200; i++) {
            System.out.println("doing stuff");
            producer.send(new ProducerRecord<String, String>("my-replicated-topic", Integer.toString(i), Integer.toString(i)));
        }
        producer.close();
        System.out.println("closing producer");
    }
}
