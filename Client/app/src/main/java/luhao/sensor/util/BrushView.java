package luhao.sensor.util;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.shiyu.client.MainActivity;
import com.example.shiyu.client.MainActivity.*;

import android.bluetooth.BluetoothSocket;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by lenovo on 2015/12/28.
 */
public class BrushView extends View {
    private Paint brush = new Paint();
    private Path path = new Path();
    private float lastx, lasty;
    private int cnt;
    private Queue<Pair<Float, Float> > queue;
    private float product, lastAbs, abs, angle;
    private Pair<Float, Float> pair;

    private MainActivity main;

    public Button resetButton;
    public ViewGroup.LayoutParams params;

    private static final float rotateRatio = 2 * (float)Math.PI / (float)(1.75);

    public BrushView(Context context, Button it) {
        super(context);
        main = (MainActivity)context;
        brush.setAntiAlias(true);
        brush.setColor(Color.GREEN);
        brush.setStyle(Paint.Style.STROKE);
        brush.setStrokeJoin(Paint.Join.ROUND);
        brush.setStrokeWidth(10f);

        resetButton = it;
        resetButton.setText("Reset");
        //params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
        //       ViewGroup.LayoutParams.WRAP_CONTENT);
        //resetButton.setLayoutParams(params);

        resetButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("AT:", "onClick of reset");
                if (!queue.isEmpty()) {
                    pair = queue.remove();
                    lastx = pair.first;
                    lasty = pair.second;
                    product = pair.first * pair.second - pair.first * pair.second;
                    abs = (float) Math.sqrt(pair.first * pair.first + pair.second * pair.second);
                    lastAbs = abs;
                    float time;
                    time = abs * 10;

                    sendCommand("33\n");

                    /*if(Looper.myLooper() == null) {
                        Looper.prepare();
                    }
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            sendCommand("00\n");
                        }
                    }, (int)(time));*/
                    try {
                        Thread.currentThread().sleep((int) time);
                    } catch (Exception e) {}
                    sendCommand("00\n");

                    Log.e("Go time ", ((Float) time).toString());

                    while (!queue.isEmpty()) {
                        Pair<Float, Float> pair = queue.remove();
                        Log.e("QUEUE", "(" + pair.first + "," + pair.second + ")");
                        product = pair.first * lasty - lastx * pair.second;
                        abs = (float) Math.sqrt(pair.first * pair.first + pair.second * pair.second);
                        angle = (float) Math.asin(product / abs / lastAbs);

                        lastx = pair.first;
                        lasty = pair.second;

                        time = Math.abs(angle / rotateRatio) * 1000;
                        if(angle > 0) {
                            sendCommand("30\n");
                            /*if(Looper.myLooper() == null) {
                                Looper.prepare();
                            }
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    sendCommand("33\n");
                                }
                            }, (int)(time));*/
                            try {
                                Thread.currentThread().sleep((int) time);
                            } catch (Exception e) {}
                            Log.e("Turn right  ", ((Float) time).toString());
                        } else {
                            sendCommand("03\n");
                            /*if(Looper.myLooper() == null) {
                                Looper.prepare();
                            }
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    sendCommand("33\n");
                                }
                            }, (int)(time));*/
                            try {
                                Thread.currentThread().sleep((int) time);
                            } catch (Exception e) {}
                            Log.e("Turn left ", ((Float) time).toString());
                        }
                        sendCommand("33\n");

                        time = abs * 10;

                        Log.e("Go time ", ((Float)time).toString());

                        /*if(Looper.myLooper() == null) {
                            Looper.prepare();
                        }
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                sendCommand("00\n");
                            }
                        }, (int)(time));*/

                        try {
                            Thread.currentThread().sleep((int) time);
                        } catch (Exception e) {}
                        sendCommand("00\n");

                        lastAbs = abs;
                    }
                }
            }
        });

        queue = new LinkedList<>();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                path.moveTo(x, y);
                cnt = 0;
                queue.clear();
                lastx = x;
                lasty = y;
                return true;
            case MotionEvent.ACTION_MOVE:
                path.lineTo(x, y);
                ++cnt;
                if (cnt % 8 == 0) {
                    //Log.e("Brush", " (x, y): (" + x + "," + y + ")");
                    queue.add(new Pair<>(x - lastx, lasty - y));
                    lastx = x;
                    lasty = y;
                }
                break;
            default:
                Log.e("Brush", "cnt: " + cnt);
                return false;
        }

        postInvalidate();
        return false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawPath(path, brush);
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

