package com.example.shiyu.client;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Looper;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.TextView;


public class FullscreenActivity extends Thread
        implements SensorEventListener, OnTouchListener{

    private static final String TAG = FullscreenActivity.class.getSimpleName();
    private SensorManager mSensorManager;
    private Sensor accelerometer;
    public TextView textviewX;
    public TextView textviewY;
    public TextView textviewZ;
    public TextView textviewF;
    public Button mButton;

    private MainActivity main;

    private GestureDetector mGesture;

    public Button flingButton;

    public Button gravityButton;

    public boolean gravityMode = false;

    private int FREQUENCY = 10;

    private int counter = 0;

    public FullscreenActivity(MainActivity main) {
        this.main = main;
        flingButton = (Button)main.findViewById(R.id.flingButton);
        gravityButton = (Button)main.findViewById(R.id.gravity);

        gravityButton.setText("Gravity off");

        gravityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!gravityMode) {
                    gravityButton.setText("Gravity on");
                    gravityMode = true;
                }
                else {
                    gravityButton.setText("Gravity off");
                    gravityMode = false;
                }
            }
        });

        textviewX = (TextView) main.findViewById(R.id.textView);
        textviewY = (TextView) main.findViewById(R.id.textView5);
        textviewZ = (TextView) main.findViewById(R.id.textView3);
        textviewF = (TextView) main.findViewById(R.id.textView4);

        mButton = (Button) main.findViewById(R.id.flingButton);
        mButton.setOnTouchListener(this);
    }


    @Override
    public void run() {

        textviewX = (TextView) main.findViewById(R.id.textView);
        textviewY = (TextView) main.findViewById(R.id.textView5);
        textviewZ = (TextView) main.findViewById(R.id.textView3);
        textviewF = (TextView) main.findViewById(R.id.textView4);


        mSensorManager = (SensorManager) main.getSystemService(main.SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if (null == mSensorManager) {
            Log.d(TAG, "Device not support SensorManager");
        }

        mSensorManager.registerListener(this, accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL);


        mButton = (Button) main.findViewById(R.id.flingButton);
        mButton.setOnTouchListener(this);
        if(Looper.myLooper() == null) {
            Looper.prepare();
        }
        mGesture = new GestureDetector(main, new GestureListener());
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.e("ActivityDemo", "Touch~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        return mGesture.onTouchEvent(event);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}


    public String controlCommand(int x, int y) {
        x = x > 4 ? 4 : x;
        y = y > 4 ? 4 : y;
        x = x < -4 ? -4 : x;
        y = y < -4 ? -4 : y;

        String s = new String();

        int basicValue = x > 0 ? x : -x;
        int biasValue = y > 0 ? y : -y;
        int leftValue, rightValue;
        if (y > 0) {
            leftValue = basicValue;
            rightValue = basicValue - biasValue;
            rightValue = rightValue > 0 ? rightValue : 0;
        } else {
            rightValue = basicValue;
            leftValue = basicValue - biasValue;
            leftValue = leftValue > 0 ? leftValue : 0;
        }
        if (x >= 0) {
            s = "" + leftValue + rightValue + "\n";
        } else {
            s = "" + (5 + leftValue) + (5 + rightValue) + "\n";
        }
        return s;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == null) {
            return;
        }

        float x, y;
        int ix, iy;

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
        {

            ++counter;
            x = event.values[SensorManager.DATA_X];
            y = event.values[SensorManager.DATA_Y];

            ix = ((int) x) / 2;
            iy = ((int) y) / 2;
            textviewX.setText(String.valueOf(ix));
            textviewY.setText(String.valueOf(iy));

        if(gravityMode && (counter % FREQUENCY) == 0) {
            if (ix > 0 && iy == 0) {
                sendCommand(((Integer) (4 + ix)).toString() + ((Integer) (4 + ix)).toString() + "\n");
            } else if (ix < 0 && iy == 0) {
                sendCommand(((Integer) (0 - ix)).toString() + ((Integer) (0 - ix)).toString() + "\n");
            } else if (ix == 0) {
                sendCommand("00\n");
            } else {
                if (iy > 0 && ix > 0) {
                    sendCommand(((Integer) 0).toString() + ((Integer) (ix)).toString() + "\n");
                } else if (iy > 0 && ix < 0) {
                    sendCommand(((Integer) (0)).toString() + ((Integer) (5 + iy)).toString() + "\n");
                } else if (iy < 0 && ix > 0) {
                    sendCommand(((Integer) (5 - iy)).toString() + ((Integer) (0)).toString() + "\n");
                } else if (iy < 0 && ix < 0) {
                    sendCommand(((Integer) (0 - iy)).toString() + ((Integer) (0)).toString() + "\n");
                }
            }
        }
        }
    }

    class GestureListener extends SimpleOnGestureListener {

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float x1 = e1.getX(), x2 = e2.getX(), y1 = e1.getY(), y2 = e2.getY();
            Log.i("Test", "e1.x" + x1 + "e2.x" + x2);
            Log.i("Test", "e1.y" + y1 + "e2.y" + y2);
            int dx = (int)((x2 - x1) / 160.0f);
            int dy = (int)((y2 - y1) / 80.0f);
            textviewF.setText(controlCommand(-dy, dx));

            sendCommand(controlCommand(-dy, dx) + "\n");

            return false;
        }
    }

    public void sendCommand(String msg) {
        Boolean eq = msg == "Forward\n";
        String st = eq.toString();
        Log.e("ActivityDemo", st);
        if(msg.equals("Forward")) {
            Log.e("ActivityDemo", "Here###########");
            main.blueTooth.sendInformation(main.CarSocket, "22\n");
        }
        else if(msg.equals("Stop")) {
            Log.e("ActivityDemo", "Here###########");
            main.blueTooth.sendInformation(main.CarSocket, "00\n");
        }
        else if(msg.equals("Back")) {
            Log.e("ActivityDemo", "Here###########");
            main.blueTooth.sendInformation(main.CarSocket, "88\n");
        }
        else if(msg.equals("Left-Forward")) {
            Log.e("ActivityDemo", "Here###########");
            main.blueTooth.sendInformation(main.CarSocket, "02\n");
        }
        else if(msg.equals("Right-Forward")) {
            Log.e("ActivityDemo", "Here###########");
            main.blueTooth.sendInformation(main.CarSocket, "20\n");
        }
        else if(msg.equals("Right-Back")) {
            Log.e("ActivityDemo", "Here###########");
            main.blueTooth.sendInformation(main.CarSocket, "65\n");
        }
        else if(msg.equals("Left-Back")) {
            Log.e("ActivityDemo", "Here###########");
            main.blueTooth.sendInformation(main.CarSocket, "56\n");
        }
        else if(msg.equals("向前")) {
            Log.e("ActivityDemo", "向前");
            main.blueTooth.sendInformation(main.CarSocket, "33\n");
        }
        else if(msg.equals("左前方")) {
            Log.e("ActivityDemo", "左前方");
            main.blueTooth.sendInformation(main.CarSocket, "03\n");
        }
        else if(msg.equals("右前方")) {
            Log.e("ActivityDemo", "右前方");
            main.blueTooth.sendInformation(main.CarSocket, "30\n");
        }
        else if(msg.equals("右后方")) {
            Log.e("ActivityDemo", "右后方");
            main.blueTooth.sendInformation(main.CarSocket, "85\n");
        }
        else if(msg.equals("左后方")) {
            Log.e("ActivityDemo", "左后方");
            main.blueTooth.sendInformation(main.CarSocket, "58\n");
        }
        else if(msg.equals("停止")) {
            Log.e("ActivityDemo", "停止");
            main.blueTooth.sendInformation(main.CarSocket, "00\n");
        }
        else if(msg.equals("向后")) {
            Log.e("ActivityDemo", "向后");
            main.blueTooth.sendInformation(main.CarSocket, "88\n");
        } else  {
            main.blueTooth.sendInformation(main.CarSocket, msg);
        }

    }
}
