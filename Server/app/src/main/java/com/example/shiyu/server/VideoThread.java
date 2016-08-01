package com.example.shiyu.server;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.camera2.params.Face;
import android.media.FaceDetector;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by shiyu on 15/12/18.
 */
public class VideoThread extends Thread {

    private MainActivity main;
    public SurfaceHolder surfaceHolder;
    private boolean isPreview;
    private final static int CAMERA_PORT = 8686;
    private final static int PREVIEW_WIDTH = 200, PREVIEW_HEIGHT = 200;
    private static int counter = 0;
    private static final int PERIOD = 5;
    private android.hardware.Camera camera;
    private FaceRecognizer faceRecognizer;
    private Bitmap bitmap;
    private Bitmap tmpBmp;
    private byte[] byteArray;

    public VideoThread(MainActivity main) {
        this.main = main;
    }

    @Override
    public void run() {
        if(Looper.myLooper() == null) {
            Looper.prepare();
        }

        surfaceHolder = ((SurfaceView) main.findViewById(R.id.surfaceView)).getHolder();

        faceRecognizer = new FaceRecognizer(main);

        surfaceHolder.setKeepScreenOn(true);

        surfaceHolder.addCallback(new SurfaceHolder.Callback() {

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                initCamera();
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {}
        });

        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @SuppressLint("NewApi")
    private void initCamera() {
        if (!isPreview) {
            camera = android.hardware.Camera.open();
        }
        if (camera != null && !isPreview) {
            try {
                Camera.Parameters params = camera.getParameters();
                android.hardware.Camera.Parameters parameters = camera.getParameters();
                parameters.setPreviewSize(PREVIEW_WIDTH, PREVIEW_HEIGHT);
                parameters.setPreviewFpsRange(20, 30);
                parameters.setPictureFormat(ImageFormat.NV21);
                parameters.setPictureSize(PREVIEW_WIDTH, PREVIEW_HEIGHT);
                camera.setDisplayOrientation(90);
                camera.setPreviewDisplay(surfaceHolder);
                camera.setParameters(params);
                camera.setPreviewCallback(new StreamIt(main.clientIP));
                camera.startPreview();
                camera.autoFocus(null);

            } catch (Exception e) {
                e.printStackTrace();
            }
            isPreview = true;
        }
    }

    class StreamIt implements android.hardware.Camera.PreviewCallback {
        private String ipname;

        public StreamIt(String ipname) {
            this.ipname = ipname;
        }

        @Override
        public void onPreviewFrame(byte[] data, android.hardware.Camera camera) {
            android.hardware.Camera.Size size = camera.getParameters().getPreviewSize();
            try {
                YuvImage image = new YuvImage(data, ImageFormat.NV21, size.width, size.height, null);

                if (image != null) {
                    ByteArrayOutputStream outstream = new ByteArrayOutputStream();
                    image.compressToJpeg(new Rect(0, 0, size.width, size.height), 80, outstream);
                    Thread th = new SendVideoThread(outstream, ipname);
                    th.start();
                    outstream.flush();
                }
            } catch (Exception ex) {
                Log.e("Sys", "Error:" + ex.getMessage());
            }
        }
    }

    class SendVideoThread extends Thread {
        private byte byteBuffer[] = new byte[1024];
        private OutputStream outsocket;
        private ByteArrayOutputStream myoutputstream;

        public SendVideoThread(ByteArrayOutputStream myoutputstream, String ipname) {
            this.myoutputstream = myoutputstream;
            try {
                myoutputstream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            try {
                    if (main.clientIP != null) {
                        main.clientIP = main.clientIP.substring(main.clientIP.lastIndexOf("/") + 1);
                    }

                    byteArray = myoutputstream.toByteArray();
                    ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArray);
                if(!main.isFacing) {
                    Socket tempSocket = new Socket(main.clientIP, CAMERA_PORT);

                    outsocket = tempSocket.getOutputStream();
                    int amount;
                    while ((amount = inputStream.read(byteBuffer)) != -1) {
                        outsocket.write(byteBuffer, 0, amount);
                    }
                    myoutputstream.flush();
                    myoutputstream.close();
                    tempSocket.close();
                }


                counter = (counter + 1) % PERIOD;

                Log.e("ActivityDemo", ((Integer) counter).toString());

                if(counter == 0 && main.isFacing) {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                try {
                    bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length, options);
                } catch (IllegalArgumentException e) {
                    bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                }
                    tmpBmp = bitmap.copy(Bitmap.Config.RGB_565, true);
                    faceRecognizer.setBitmap(tmpBmp);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}


