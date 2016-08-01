package com.example.shiyu.server;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by shiyu on 15/12/24.
 */
public class SimSimi extends Thread {
    private SimsimiAPI simsimiAPI;
    private MainActivity main;

    private String result = "Fail";
    private BufferedReader bufferReader = null;
    private InputStreamReader inputStreamReader = null;
    private String buffer = null;

    public SimSimi(MainActivity main) {
        this.main = main;
    }

    @Override
    public void run() {

    }

    public void sendMessage(String message) {
        simsimiAPI = new SimsimiAPI(message);
        simsimiAPI.execute((TextView)main.findViewById(R.id.textView));
    }

    public class SimsimiAPI extends AsyncTask<TextView, Void, String> {

        private TextView response;
        private String text = "";

        public SimsimiAPI(String message) {
            text = message;
        }

        protected String doInBackground(TextView... params) {

            this.response = params[0];
            return makeHttpRequest();

        }

        final String makeHttpRequest() {
            String key = "fb8d5492-bb87-4881-b730-cff2c00dd2de";
            String lc = "ch";
            double ft = 0.0;

            try {

                Log.e("ActivityDemo", "I say " + text.trim());
                for(char ch : text.toCharArray()) {
                    Log.e("ActivityDemo", "char: " + ch + "");
                }
                Log.e("ActivityDemo", "From: " + text);
                String encodeText = URLEncoder.encode(text, "UTF-8");
                String url = "http://sandbox.api.simsimi.com/request.p?key="
                        + key + "&lc=" + lc + "&ft=" + ft + "&text=" + encodeText;
                URL urlLink = new URL(url);
                HttpURLConnection urlConnection = (HttpURLConnection)urlLink.openConnection();
                urlConnection.setRequestProperty("User-Agent", "");
                urlConnection.setRequestMethod("GET");
                urlConnection.setDoInput(true);
                urlConnection.connect();

                inputStreamReader = new InputStreamReader(urlConnection.getInputStream());
                bufferReader = new BufferedReader(inputStreamReader);

                while ((buffer = bufferReader.readLine()) != null) {
                    if (buffer.length() > 1) {
                        result = buffer;
                    }
                }
            } catch (UnsupportedEncodingException e) {
                System.out
                        .println("UnsupportedEncodingException is generated.");
            } catch (IOException e) {

                System.out.println("IOException is generated.");

            } finally {
                if (inputStreamReader != null)
                    try {
                        inputStreamReader.close();
                    } catch (IOException e) {
                        System.out.println("InputStreamReader is not closed.");
                    }

                if (bufferReader != null)
                    try {
                        bufferReader.close();
                    } catch (IOException e) {
                        System.out.println("BufferedReader is not closed.");
                    }
            }

            return result;
        }

        protected void onPostExecute(String page) {
            String message;
            message = page.substring(page.indexOf(":\"") + 2);
            message = message.substring(0, message.indexOf("\""));
            main.voiceGenerator.speak(message);
            response.setText(message);
            Log.e("ActivityDemo", "Simsim " + message);
        }
    }
}
