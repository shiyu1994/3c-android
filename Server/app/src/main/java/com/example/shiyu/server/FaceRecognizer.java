package com.example.shiyu.server;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.media.FaceDetector;
import android.os.Looper;
import android.util.Log;

import android.os.Handler;

/**
 * Created by shiyu on 15/12/25.
 */
public class FaceRecognizer {
    private Bitmap bitmap;
    private MainActivity main;
    private final static int MAX_FACES = 1;
    private RectF[] faceRects = new RectF[MAX_FACES];
    boolean detected = false;
    String message = "";

    public FaceRecognizer(MainActivity main) {
        this.main = main;
    }

    private void faceDetector(final Bitmap bitmap) {
        FaceDetector faceDet = new FaceDetector(bitmap.getWidth(), bitmap.getHeight(), MAX_FACES);
        FaceDetector.Face[] faceList = new FaceDetector.Face[MAX_FACES];
        faceDet.findFaces(bitmap, faceList);

        detected = false;

        for (int i=0; i < faceList.length; i++) {
            FaceDetector.Face face = faceList[i];
            Log.d("FaceDet", "Face [" + face + "]");
            if (face != null) {
                Log.d("FaceDet", "Face ["+i+"] - Confidence ["+face.confidence()+"]");
                PointF pf = new PointF();
                face.getMidPoint(pf);
                Log.d("FaceDet", "\t Eyes distance ["+face.eyesDistance()+"] - Face midpoint ["+pf.x+"&"+pf.y+"]");
                RectF r = new RectF();
                r.left = pf.x - face.eyesDistance() / 2;
                r.right = pf.x + face.eyesDistance() / 2;
                r.top = pf.y - face.eyesDistance() / 2;
                r.bottom = pf.y + face.eyesDistance() / 2;
                Log.e("ActivityDemo", ((Float) face.eyesDistance()).toString());
                faceRects[i] = r;
                detected = true;
                message += "left: " + r.left + "  right:" + r.right + "  top:" + r.top + "  bottom:" + r.bottom + " distance:" + face.eyesDistance() + " " + face.confidence() + "\n";
                main.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        main.msg.setText(message);
                    }
                });
                if(face.confidence() > 0.52) {
                    main.voiceGenerator.speak("你好");
                    if (face.eyesDistance() > 200) {
                        main.serverThread.sendCommand("Stop");
                    } else {
                        if (r.left > 700 && r.left < 1000) {
                            SendClientTask sendClientTask = new SendClientTask(main.clientIP, 8080, "Right-Forward");
                            sendClientTask.execute();
                            message += "Turn Right\n";
                            main.runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    main.msg.setText(message);
                                }
                            });
                            if(Looper.myLooper() == null) {
                                Looper.prepare();
                            }
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    SendClientTask sendClientTask = new SendClientTask(main.clientIP, 8080, "Forward");
                                    sendClientTask.execute();
                                }
                            }, 10);
                        } else if (r.left > 0 && r.left < 300) {
                            SendClientTask sendClientTask = new SendClientTask(main.clientIP, 8080, "Left-Forward");
                            sendClientTask.execute();
                            message += "Turn Left\n";
                            main.runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    main.msg.setText(message);
                                }
                            });
                            if(Looper.myLooper() == null) {
                                Looper.prepare();
                            }
                            new Handler().postDelayed(new Runnable() {

                                @Override
                                public void run() {
                                    SendClientTask sendClientTask = new SendClientTask(main.clientIP, 8080, "Forward");
                                    sendClientTask.execute();
                                }
                            }, 10);
                        } else {
                            SendClientTask sendClientTask = new SendClientTask(main.clientIP, 8080, "Forward");
                            sendClientTask.execute();
                        }
                    }
                }
                else {
                    SendClientTask sendClientTask = new SendClientTask(main.clientIP, 8080, "Stop");
                    sendClientTask.execute();
                }
            }
            else {
                SendClientTask sendClientTask = new SendClientTask(main.clientIP, 8080, "Stop");
                sendClientTask.execute();
            }
        }
        Log.e("ActivityDemo", "Face " + detected);
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = RotateBitmap(bitmap, 90);
        faceDetector(this.bitmap);
    }

    public static Bitmap RotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        Bitmap result = Bitmap.createBitmap(source, 0, 0, source.getWidth(),
                source.getHeight(), matrix, false);
        source.recycle();
        return result;
    }
}

