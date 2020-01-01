from kafka import KafkaConsumer
from vaderSentiment.vaderSentiment import SentimentIntensityAnalyzer
import requests
import json

url = 'http://localhost:5005/updateCache'

consumer = KafkaConsumer('replicated-topic', bootstrap_servers='localhost:9092')
analyzer = SentimentIntensityAnalyzer()

for payload_str in consumer:
    payload = json.loads(payload_str)
    text = payload['message']
    time = payload['time']

    score = analyzer.polarity_scores(text)['compound']

    payload = {'score': score, 'time': time}
    requests.post(url=url, data=payload)