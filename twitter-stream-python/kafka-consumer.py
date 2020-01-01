from kafka import TopicPartition
from kafka import KafkaConsumer

consumer = KafkaConsumer('my-replicated-topic', bootstrap_servers='localhost:9092, localhost:9093, localhost:9094', value_deserializer=lambda v: v.decode('utf-8'))

for msg in consumer:
    print (msg.value)