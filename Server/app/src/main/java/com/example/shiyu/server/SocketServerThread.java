package com.example.shiyu.server;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by shiyu on 15/12/18.
 */
public class SocketServerThread extends Thread {

    static final int SocketServerPORT = 8080;
    private MainActivity main;
    private ServerSocket serverSocket;
    private Socket phoneSocket;
    private String clientIP;
    private String message;
    int count = 0;

    public SocketServerThread(MainActivity main) {
        this.main = main;
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket();
            serverSocket.setReuseAddress(true);
            serverSocket.bind(new InetSocketAddress(SocketServerPORT));
            main.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    main.BluetoothText.setText("I'm waiting here: "
                            + serverSocket.getLocalPort());
                }
            });

            while (true) {
                phoneSocket = serverSocket.accept();

                clientIP = phoneSocket.getInetAddress().toString();
                main.clientIP = clientIP;

                count++;

                Log.e("ActivityDemo", "Getting Input");

                InputStream inputStream = phoneSocket.getInputStream();

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

                Log.e("ActivityDemo", "Buffer Read");

                //sendCommand(getMessage);

                if(!getMessage.startsWith("$")) {
                    message += "#" + count + " from " + clientIP + ":" + phoneSocket.getPort() + "\n";
                    main.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            main.msg.setText(message);
                        }
                    });
                    main.simSimi.sendMessage(getMessage);
                    message += getMessage + "\n";
                    main.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            main.msg.setText(message);
                        }
                    });
                } else {
                    if(getMessage.contains("Face")) {
                        main.isFacing = true;
                    }
                    getMessage = getMessage.substring(1);
                }
                sendCommand(getMessage);
                Log.e("ActivityDemo", "simSim sent " + getMessage);

            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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
