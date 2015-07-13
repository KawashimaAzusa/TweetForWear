package com.example.hashimotomika.tweetforwear;

import android.os.AsyncTask;
import android.widget.Toast;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import twitter4j.Twitter;
import twitter4j.TwitterException;

/**
 * Created by hashimotomika on 7/13/15.
 */
public class MessageService extends WearableListenerService {

    private Twitter mTwitter;

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        String msg;
        mTwitter = TwitterUtils.getTwitterInstance(this);
        try {
            msg = new String(messageEvent.getData(), "UTF-8");
        } catch (Exception e) {
            return;
        }
        showToast(msg);
        tweet(msg);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

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
}
