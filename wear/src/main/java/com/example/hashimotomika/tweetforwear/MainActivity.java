package com.example.hashimotomika.tweetforwear;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AnalogClock;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.List;
import java.util.Random;

public class MainActivity extends Activity implements SensorEventListener{

    private GoogleApiClient mGoogleApiClient;
    public static int cntTouch;

    private SensorManager sensorManager;
    private long mFirstTouch = 0;
    private long MIN_COVERED_DURATION = 3000;
    private long MAX_COVERED_DURATION = 4000;
    private int LIGHT_INTENSITY = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupGoogleApiClient();
        cntTouch = 0;

        AnalogClock analogClock = (AnalogClock) findViewById(R.id.AnalogClock);
        analogClock.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                String action = "";
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        cntTouch++;
                        if (cntTouch == 2) {
                            sendMessage("TOUCH: " + makeInputText());
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

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

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

    @Override
    protected void onStop() {
        super.onStop();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_LIGHT);
        if (sensors.size() > 0) {
            Sensor s = sensors.get(0);
            sensorManager.registerListener(this, s, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        long now = System.currentTimeMillis();
        float light = event.values[0];
        Log.d("LIGHT", String.valueOf(light));
        if (light > LIGHT_INTENSITY) {
            long duration = now - mFirstTouch;
            if ((duration >= MIN_COVERED_DURATION) && (duration <= MAX_COVERED_DURATION)) {
                sendMessage("SHAKE: " + makeInputText());
            }
            mFirstTouch = 0;
        } else {
            if (mFirstTouch == 0) {
                mFirstTouch = now;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

}
