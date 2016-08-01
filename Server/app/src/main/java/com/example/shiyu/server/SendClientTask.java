package com.example.shiyu.server;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class SendClientTask extends AsyncTask<Void, Void, Void> {
    String dstAddress;
    int dstPort;
    String message = "";
    String response = "";

    SendClientTask(String addr, int port, String msg) {
        dstAddress = addr;
        dstPort = port;
        message = msg;
    }

    @Override
    protected Void doInBackground(Void... params) {
        Socket socket = null;

        try {
            socket = new Socket(dstAddress, dstPort);

            Log.e("ActivityDemo", "Sending message " + message);

            OutputStream outputStream = socket.getOutputStream();
            PrintStream printStream = new PrintStream(outputStream);
            printStream.print(message);
            printStream.close();


        } catch (UnknownHostException e) {
            e.printStackTrace();
            response = "UnknownHostException: " + e.toString();
        } catch (IOException e) {
            e.printStackTrace();
            response = "IOException: " + e.toString();
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }
}