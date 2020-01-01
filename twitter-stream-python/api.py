from flask import Flask, request, render_template
import json
import redis
import time

app = Flask(__name__)
r = redis.Redis()
"""
epoch minutes ->  (sentiment sum,  count)
"""
@app.route('/updateCache', methods=['POST'])
def updateCache():
    sentiment_score = request.json['score']
    time = request.json['time']

    epoch_seconds = int(time.time())
    epoch_minutes = epoch_seconds // 60

    # delete the entry that is exactly 24hrs prior
    past_epoch_minutes = epoch_minutes - 24*60
    r.delete(past_epoch_minutes)

    with r.pipeline() as pipe:
        while True:
            try:
                pipe.watch(epoch_minutes)
                # get the existing sentiment tuple, if it exists
                if not r.exists(epoch_minutes):
                    sentiment_tuple = (sentiment_score, 1)
                else:
                    old_score, old_count = pipe.get(epoch_minutes)
                    sentiment_tuple = (old_score + sentiment_score, old_count + 1)

                pipe.multi()
                pipe.set(epoch_minutes, sentiment_tuple)
                pipe.execute()
                
            except redis.WatchError:
                continue
            finally:
                pipe.close()


@app.route('/getSentimentAverages', methods=['GET'])
def getSentimentAverages():

    response = dict()
    for key in r.scan_iter():
        score, count = r.get(key)
        response[key] = score/count

    return response

if __name__ == '__main__':
    app.run(debug=True, port=5005)