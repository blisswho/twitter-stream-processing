from kafka import KafkaConsumer
from vaderSentiment.vaderSentiment import SentimentIntensityAnalyzer
import requests
import json

url = 'http://localhost:5005/updateCache'

consumer = KafkaConsumer('my-replicated-topic', bootstrap_servers='localhost:9092, localhost:9093, localhost:9094', value_deserializer=lambda v: v.decode('utf-8'))
analyzer = SentimentIntensityAnalyzer()

for payload_str in consumer:
    payload = json.loads(payload_str.value)
    text = payload['message']
    time = payload['time']

    score = analyzer.polarity_scores(text)['compound']

    payload = {'score': score, 'time': time}
    print(payload)
    requests.post(url=url, json=payload)