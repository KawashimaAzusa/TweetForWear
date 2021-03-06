package com.example.hashimotomika.tweetforwear;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import twitter4j.Twitter;
import twitter4j.TwitterException;


public class MessageService extends WearableListenerService {

    private Twitter mTwitter;

    private static final int TWEET_DURATION = 60000;
    private long mLastTweet;

    private String TAG = "Handler";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        long now = System.currentTimeMillis();
        if (now - mLastTweet > TWEET_DURATION) {
            String msg;
            mTwitter = TwitterUtils.getTwitterInstance(this);
            try {
                msg = new String(messageEvent.getData(), "UTF-8");
            } catch (Exception e) {
                return;
            }
            showToast(msg);
            Log.d(TAG, "tweet");
            tweet(msg);
            /*
            sleep(300000);
            CallRequest.sendCallRequest(this);
            */
            mLastTweet = now;
        }
    }

    /**
     * messageをToastする（ログ用）
     * @param message
     */
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * messageをTweetする
     * @param message
     */
    private void tweet(String message) {
        AsyncTask<String, Void, Boolean> task = new AsyncTask<String, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(String... params) {
                try {
                    mTwitter.updateStatus(params[0]);
                    return true;
                } catch (TwitterException e) {
                    e.printStackTrace();
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean result) {
            }
        };
        task.execute(message);
    }

    public synchronized void sleep(long msec) {
        try {
            wait(msec);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
