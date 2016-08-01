package com.example.shiyu.client;

import android.os.AsyncTask;
import android.widget.TextView;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by shiyu on 15/11/20.
 */
public class ConnectClientTask extends AsyncTask<Void, Void, Void> {

    private TextView textResponse;
    String dstAddress;
    int dstPort = 8080;
    String response = "";

    ConnectClientTask(String addr, int port, TextView tResponse) {
        dstAddress = addr;
        dstPort = port;
        textResponse = tResponse;
    }

    @Override
    protected Void doInBackground(Void... arg0) {

        Socket socket = null;

        try {
            socket = new Socket(dstAddress, dstPort);
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

    @Override
    protected void onPostExecute(Void result) {
        textResponse.setText(response);
        super.onPostExecute(result);
    }
}
