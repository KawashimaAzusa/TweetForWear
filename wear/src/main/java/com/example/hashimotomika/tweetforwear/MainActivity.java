package com.example.hashimotomika.tweetforwear;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.Calendar;
import java.util.Random;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class MainActivity extends Activity{

    private GoogleApiClient mGoogleApiClient;
    public static int cntTouch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupGoogleApiClient();
        cntTouch = 0;

        Button button = (Button) findViewById(R.id.tweet);
        button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                String action = "";
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        cntTouch++;
                        if (cntTouch == 2) {
                            sendMessage(makeInputText());
                            cntTouch = 0;
                        }
                        action = "ACTION_DOWN";
                        break;
                    case MotionEvent.ACTION_UP:
                        action = "ACTION_UP";
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        action = "ACTION_CANCEL";
                        break;
                    case MotionEvent.ACTION_MOVE:
                        action = "ACTION_MOVE";
                        break;
                }
                Log.d("MotionEvent", "action = " + action + ", (" + event.getX() + ", " + event.getY() + ")");
                return false;
            }
        });

    }

    /**
     * GoogleApiClient の定義
     */
    private void setupGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle connectionHint) {
                        //接続が完了した時
                    }
                    @Override
                    public void onConnectionSuspended(int cause) {
                        //一時的に切断された時
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {

                    }
                })
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();
    }

    /**
     * Handheld側に渡す文字列を生成
     * @return
     */
    private String makeInputText(){
        Random rand = new Random();
        int n = rand.nextInt(1000);
        String mInputText = "ﾌﾘﾌﾘ~(ｏ^-^)[" + Integer.toString(n) + "]";
        return mInputText;
    }

    /**
     * Handheld側にmessageを渡す
     * @param message
     */
    private void sendMessage(String message) {
        if (message == null) return;
        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {
                String message = params[0];
                byte[] bytes;
                try {
                    bytes = message.getBytes("UTF-8");
                } catch (Exception e) {
                    return null;
                }
                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
                for (com.google.android.gms.wearable.Node node : nodes.getNodes()) {
                    Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), "/post/message", bytes)
                            .setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                                @Override
                                public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                                    //sendMessageResult.toString();
                                }
                            });
                }
                return null;
            }
        }.execute(message);
    }

}
