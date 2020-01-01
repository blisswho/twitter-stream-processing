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

    epoch_seconds = request.json['time']
    epoch_minutes = epoch_seconds // 60
    key = str(epoch_minutes)

    # delete the entry that is exactly 24hrs prior
    past_epoch_minutes = epoch_minutes - 24*60
    r.delete(str(past_epoch_minutes))

    with r.pipeline() as pipe:
        while True:
            try:
                pipe.watch(key)
                # get the existing sentiment tuple, if it exists
                if not r.exists(key):
                    sentiment_tuple = (sentiment_score, 1)
                else:
                    old_score, old_count = eval(pipe.get(key))
                    sentiment_tuple = (old_score + sentiment_score, old_count + 1)

                print(sentiment_tuple)

                pipe.multi()
                pipe.set(key, str(sentiment_tuple))
                pipe.execute()
                break
                
            except redis.WatchError:
                continue
            finally:
                pipe.close()
                break
    
    return ""


@app.route('/getSentimentAverages', methods=['GET'])
def getSentimentAverages():

    response = dict()
    for key in r.scan_iter():
        time = int(key.decode('utf-8'))
        score, count = eval(r.get(key))
        response[time] = score/count

    return response

if __name__ == '__main__':
    app.run(debug=True, port=5005)