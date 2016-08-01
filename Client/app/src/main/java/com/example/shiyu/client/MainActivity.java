package com.example.shiyu.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import luhao.sensor.util.BrushView;

/**
 * Created by shiyu on 15/12/13.
 */

public class MainActivity extends Activity {
    public static final int CAMERA_PORT = 8686;
    private Bitmap bitmap;
    private ImageView imageView;
    private ServerSocket cameraSocket;
    private ServerSocket messageSocket;
    public static  Handler handler;
    public String serverIP;
    public static final int SERVER_PORT = 8080;
    private VoiceRecoginizer voiceRecognizer;
    private FullscreenActivity fullscreenActivity;
    private Button seeTraceButton;

    private TextActivity textActivity;

    public static BluetoothSocket CarSocket;

    public BlueTooth blueTooth;

    public Button traceButton;

    public Button backButton;

    public Button runButton;

    private View.OnClickListener backClick;

    private View.OnClickListener traceClick;

    private Button faceButton;

    private Socket fromServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);
        textActivity = new TextActivity(this);
        textActivity.start();
        imageView = (ImageView) findViewById(R.id.imageView);

        traceButton = (Button)findViewById(R.id.brush);

        faceButton = (Button)findViewById(R.id.face);

        faceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendClientTask sendMessage = new SendClientTask(serverIP, 8080, "$Face");
                sendMessage.execute();
            }
        });

        backClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContentView(R.layout.activity_main);

                imageView = (ImageView) findViewById(R.id.imageView);

                traceButton = (Button)findViewById(R.id.brush);
                traceButton.setOnClickListener(traceClick);

                seeTraceButton = (Button)findViewById(R.id.seeTrace);
                seeTraceButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.setClassName(MainActivity.this, "com.example.shiyu.client.TraceActivity");
                        startActivity(intent);
                    }
                });
                reloadAllUI();
            }
        };

        traceClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContentView(R.layout.brush_content);
                backButton = (Button)findViewById(R.id.back);
                backButton.setOnClickListener(backClick);
                runButton = (Button)findViewById(R.id.run);
                View brushView = new BrushView(MainActivity.this, runButton);
                RelativeLayout area = (RelativeLayout)findViewById(R.id.area);
                area.addView(brushView);
            }
        };

        traceButton.setOnClickListener(traceClick);

        seeTraceButton = (Button)findViewById(R.id.seeTrace);
        seeTraceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClassName(MainActivity.this, "com.example.shiyu.client.TraceActivity");
                startActivity(intent);
            }
        });

        ReceiveVideo videoDisplayer = new ReceiveVideo();
        videoDisplayer.start();

        ReceiveMessage receiveMessage = new ReceiveMessage();
        receiveMessage.start();

        voiceRecognizer = new VoiceRecoginizer(this);
        voiceRecognizer.run();

        blueTooth = new BlueTooth(this);
        blueTooth.start();



        handler=new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if(bitmap!=null && msg.arg1 == 100)
                    imageView.setImageBitmap(bitmap);
                Log.e("ActivityDemo", "ShowImage");

                super.handleMessage(msg);
            }

        };

        fullscreenActivity = new FullscreenActivity(this);
        fullscreenActivity.start();
    }

    class ReceiveMessage extends Thread {

        @Override
        public void run() {
            try {
                messageSocket = new ServerSocket(8080);
            } catch (Exception e) {}

            while (true) {
                try {
                    fromServer = messageSocket.accept();

                    InputStream inputStream = fromServer.getInputStream();

                    Log.e("ActivityDemo", "Input Get");

                    ByteArrayOutputStream byteArrayOutputStream =
                            new ByteArrayOutputStream(1024);
                    byte[] buffer = new byte[1024];

                    int bytesRead;

                    String getMessage = "";

                    while ((bytesRead = inputStream.read(buffer)) != -1){
                        byteArrayOutputStream.write(buffer, 0, bytesRead);
                        getMessage += byteArrayOutputStream.toString("UTF-8");
                    }

                    sendCommand(getMessage);

                } catch (Exception e) {

                }
            }
        }
    }

    class ReceiveVideo extends Thread{

        private int length = 0;
        private int num = 0;
        private byte[] buffer = new byte[2048];
        private byte[] data = new byte[204800];

        @Override
        public void run(){
            try{
                //Log.e("video ", "video thread");
                cameraSocket = new ServerSocket(CAMERA_PORT);
                while(true){
                    //Log.e("video ", "video thread");
                    Socket socket = cameraSocket.accept();
                    //Log.e("video ", "video accept");
                    try{
                        InputStream input = socket.getInputStream();
                        num = 0;
                        do{
                            length = input.read(buffer);
                            if(length >= 0){
                                System.arraycopy(buffer,0,data,num,length);
                                num += length;
                            }
                        }while(length >= 0);

                        new setImageThread(data,num).start();
                        input.close();
                    }catch(Exception e){
                        e.printStackTrace();
                    }finally{
                        socket.close();
                    }
                }

            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    class setImageThread extends Thread{

        private byte[]data;
        private int num;
        public setImageThread(byte[] data, int num){
            this.data = data;
            this.num = num;
        }

        @Override
        public void run(){
            bitmap = BitmapFactory.decodeByteArray(data, 0, num);
            //bitmap = RotateBitmap(bitmap, 90);
            Message msg=new Message();
            msg.arg1 = 100;
            handler.sendMessage(msg);
        }
    }

    public static Bitmap RotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        //Bitmap.Config
        Bitmap result = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, false);
        source.recycle();
        return result;
    }

    private void reloadAllUI() {

        Log.e("a", ((Boolean)(textActivity.directionButtons == null)).toString());

        textActivity.directionButtons.editTextAddress = (EditText)findViewById(R.id.address);
        textActivity.directionButtons.buttonConnect = (Button)findViewById(R.id.connect);

        Log.e("a", "reload");

        textActivity.directionButtons.buttonLeftFront = (ImageButton) findViewById(R.id.buttonLeftFront);
        textActivity.directionButtons.buttonFront = (ImageButton) findViewById(R.id.buttonFront);
        textActivity.directionButtons.buttonRightFront = (ImageButton) findViewById(R.id.buttonRightFront);
        textActivity.directionButtons.buttonRightBack = (ImageButton) findViewById(R.id.buttonRightBack);
        textActivity.directionButtons.buttonBack = (ImageButton) findViewById(R.id.buttonBack);
        textActivity.directionButtons.buttonLeftBack = (ImageButton) findViewById(R.id.buttonLeftBack);
        textActivity.directionButtons.buttonStop = (ImageButton) findViewById(R.id.buttonStop);

        UIOptimization.setButtonFocusChanged(textActivity.directionButtons.buttonLeftFront);
        UIOptimization.setButtonFocusChanged(textActivity.directionButtons.buttonFront);
        UIOptimization.setButtonFocusChanged(textActivity.directionButtons.buttonRightFront);
        UIOptimization.setButtonFocusChanged(textActivity.directionButtons.buttonRightBack);
        UIOptimization.setButtonFocusChanged(textActivity.directionButtons.buttonBack);
        UIOptimization.setButtonFocusChanged(textActivity.directionButtons.buttonLeftBack);
        UIOptimization.setButtonFocusChanged(textActivity.directionButtons.buttonStop);

        textActivity.directionButtons.buttonLeftFront.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*SendClientTask sendDirection = new SendClientTask(
                        serverIP, main.SERVER_PORT, "Left-Forward");

                sendDirection.execute();*/
                blueTooth.sendInformation(CarSocket, "03\n");
            }
        });

        textActivity.directionButtons.buttonFront.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*SendClientTask sendDirection = new SendClientTask(
                        serverIP, main.SERVER_PORT, "Forward");

                sendDirection.execute();*/
                //Log.e("", "here$$$$$$$$$$$$");
                blueTooth.sendInformation(CarSocket, "33\n");
            }
        });

        textActivity.directionButtons.buttonRightFront.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*SendClientTask sendDirection = new SendClientTask(
                        serverIP, main.SERVER_PORT, "Right-Forward");

                sendDirection.execute();*/
                blueTooth.sendInformation(CarSocket, "30\n");
            }
        });

        textActivity.directionButtons.buttonRightBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*SendClientTask sendDirection = new SendClientTask(
                        serverIP, main.SERVER_PORT, "Right-Back");

                sendDirection.execute();*/
                blueTooth.sendInformation(CarSocket, "85\n");
            }
        });

        textActivity.directionButtons.buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*SendClientTask sendDirection = new SendClientTask(
                        serverIP, main.SERVER_PORT, "Back");

                sendDirection.execute();*/
                blueTooth.sendInformation(CarSocket, "88\n");
            }
        });

        textActivity.directionButtons.buttonLeftBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*SendClientTask sendDirection = new SendClientTask(
                        serverIP, main.SERVER_PORT, "Left-Back");

                sendDirection.execute();*/
                blueTooth.sendInformation(CarSocket, "58\n");
            }
        });

        textActivity.directionButtons.buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("ActivityDemo", "Stop Click");
                /*SendClientTask sendDirection = new SendClientTask(
                        serverIP, main.SERVER_PORT, "Stop");

                sendDirection.execute();*/
                blueTooth.sendInformation(CarSocket, "00\n");
            }
        });

        textActivity.directionButtons.editTextAddress.setVisibility(View.GONE);

        textActivity.directionButtons.buttonConnect.setVisibility(View.GONE);

        textActivity.directionButtons.buttonLeftFront.setVisibility(View.VISIBLE);
        textActivity.directionButtons.buttonFront.setVisibility(View.VISIBLE);
        textActivity.directionButtons.buttonRightFront.setVisibility(View.VISIBLE);
        textActivity.directionButtons.buttonRightBack.setVisibility(View.VISIBLE);
        textActivity.directionButtons.buttonBack.setVisibility(View.VISIBLE);
        textActivity.directionButtons.buttonLeftBack.setVisibility(View.VISIBLE);
        textActivity.directionButtons.buttonStop.setVisibility(View.VISIBLE);

        fullscreenActivity.flingButton = (Button)findViewById(R.id.flingButton);
        fullscreenActivity.gravityButton = (Button)findViewById(R.id.gravity);

        fullscreenActivity.gravityButton.setText("Gravity off");

        fullscreenActivity.gravityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!fullscreenActivity.gravityMode) {
                    fullscreenActivity.gravityButton.setText("Gravity on");
                    fullscreenActivity.gravityMode = true;
                } else {
                    fullscreenActivity.gravityButton.setText("Gravity off");
                    fullscreenActivity.gravityMode = false;
                }
            }
        });

        fullscreenActivity.textviewX = (TextView) findViewById(R.id.textView);
        fullscreenActivity.textviewY = (TextView) findViewById(R.id.textView5);
        fullscreenActivity.textviewZ = (TextView) findViewById(R.id.textView3);
        fullscreenActivity.textviewF = (TextView) findViewById(R.id.textView4);

        fullscreenActivity.mButton = (Button) findViewById(R.id.flingButton);
        fullscreenActivity.mButton.setOnTouchListener(fullscreenActivity);

        voiceRecognizer.startButton = (Button)findViewById(R.id.speak);
        voiceRecognizer.resultList = (TextView)findViewById(R.id.result);
        voiceRecognizer.startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                voiceRecognizer.mSpeechRecognizer.startListening(voiceRecognizer.recognizerIntent);
            }
        });
    }

    public void sendCommand(String msg) {
        Boolean eq = msg == "Forward\n";
        String st = eq.toString();
        Log.e("ActivityDemo", st);
        if(msg.equals("Forward")) {
            Log.e("ActivityDemo", "Here###########");
            blueTooth.sendInformation(CarSocket, "33\n");
        }
        else if(msg.equals("Stop")) {
            Log.e("ActivityDemo", "Here###########");
            blueTooth.sendInformation(CarSocket, "00\n");
        }
        else if(msg.equals("Back")) {
            Log.e("ActivityDemo", "Here###########");
            blueTooth.sendInformation(CarSocket, "88\n");
        }
        else if(msg.equals("Left-Forward")) {
            Log.e("ActivityDemo", "Here###########");
            blueTooth.sendInformation(CarSocket, "03\n");
        }
        else if(msg.equals("Right-Forward")) {
            Log.e("ActivityDemo", "Here###########");
            blueTooth.sendInformation(CarSocket, "30\n");
        }
        else if(msg.equals("Right-Back")) {
            Log.e("ActivityDemo", "Here###########");
            blueTooth.sendInformation(CarSocket, "65\n");
        }
        else if(msg.equals("Left-Back")) {
            Log.e("ActivityDemo", "Here###########");
            blueTooth.sendInformation(CarSocket, "56\n");
        }
        else if(msg.equals("向前")) {
            Log.e("ActivityDemo", "向前");
            blueTooth.sendInformation(CarSocket, "33\n");
        }
        else if(msg.equals("左前方")) {
            Log.e("ActivityDemo", "左前方");
            blueTooth.sendInformation(CarSocket, "03\n");
        }
        else if(msg.equals("右前方")) {
            Log.e("ActivityDemo", "右前方");
            blueTooth.sendInformation(CarSocket, "30\n");
        }
        else if(msg.equals("右后方")) {
            Log.e("ActivityDemo", "右后方");
            blueTooth.sendInformation(CarSocket, "85\n");
        }
        else if(msg.equals("左后方")) {
            Log.e("ActivityDemo", "左后方");
            blueTooth.sendInformation(CarSocket, "58\n");
        }
        else if(msg.equals("停止")) {
            Log.e("ActivityDemo", "停止");
            blueTooth.sendInformation(CarSocket, "00\n");
        }
        else if(msg.equals("向后")) {
            Log.e("ActivityDemo", "向后");
            blueTooth.sendInformation(CarSocket, "88\n");
        } else  {
            blueTooth.sendInformation(CarSocket, msg);
        }

    }
}
