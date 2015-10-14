package com.example.hashimotomika.tweetforwear;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AnalogClock;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextClock;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.LogRecord;

public class MainActivity extends Activity implements SensorEventListener{

    private GoogleApiClient mGoogleApiClient;
    public static int cntTouch;

    private static final int FORCE_THRESHOLD = 0;
    private static final int TIME_THRESHOLD = 100;
    private static final int SHAKE_TIMEOUT = 500;
    private static final int SHAKE_DURATION = 100;
    private static final int SHAKE_COUNT = 3;

    private SensorManager sensorManager;
    private float lastX = -1.0f, lastY = -1.0f, lastZ = -1.0f;
    private int mShakeCount = 0;
    private long mLastForce;
    private long mLastShake;
    private long mLastTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupGoogleApiClient();
        cntTouch = 0;

        AnalogClock textClock = (AnalogClock) findViewById(R.id.AnalogClock);
        textClock.setOnTouchListener(new OnTouchListener() {
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
        List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
        if (sensors.size() > 0) {
            Sensor s = sensors.get(0);
            sensorManager.registerListener(this, s, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if(event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) {
            return;
        }

        long now = System.currentTimeMillis();
        if ((now - mLastForce) > SHAKE_TIMEOUT) {
            mShakeCount = 0;
        }
        if ((now - mLastTime) > TIME_THRESHOLD) {
            long elapsedTime = now - mLastTime;
            float data_x = event.values[0];
            float data_y = event.values[1];
            float data_z = event.values[2];
            float speed = Math.abs(data_x + data_y + data_z - lastX -lastY - lastZ) / elapsedTime * 10000;
            Log.d("Speed:", String.valueOf(speed));

            if (speed > FORCE_THRESHOLD) {
                if ((++mShakeCount >= SHAKE_COUNT) && (now - mLastShake > SHAKE_DURATION)) {
                    mLastShake = now;
                    mShakeCount = 0;
                    sendMessage("SHAKE: " + makeInputText());
                }
                mLastForce = now;
            }
            lastX = data_x;
            lastY = data_y;
            lastZ = data_z;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

}
