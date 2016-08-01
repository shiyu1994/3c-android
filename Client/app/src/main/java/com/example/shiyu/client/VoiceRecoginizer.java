package com.example.shiyu.client;

import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.baidu.speech.VoiceRecognitionService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by shiyu on 15/12/19.
 */
public class VoiceRecoginizer extends Thread {

    public android.speech.SpeechRecognizer mSpeechRecognizer;
    public Button startButton;
    public TextView resultList;
    private MainActivity main;

    public VoiceRecoginizer(MainActivity main) {
        this.main = main;
    }

    public Intent recognizerIntent;

    @Override
    public void run() {
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(main, new ComponentName(main, VoiceRecognitionService.class));
        startButton = (Button)main.findViewById(R.id.speak);
        resultList = (TextView)main.findViewById(R.id.result);
        RecognitionListener listener = new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                startButton.setText("Ready");
                Log.e("ActivityDebug", "Start Listening");
            }

            @Override
            public void onBeginningOfSpeech() {
                startButton.setText("Speaking");
                Log.e("ActivityDebug", "Start speaking");
            }

            @Override
            public void onRmsChanged(float rmsdB) {

            }

            @Override
            public void onBufferReceived(byte[] buffer) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int error) {
                switch (error) {
                    case SpeechRecognizer.ERROR_AUDIO:
                        Log.e("ActivityDemo", "音频问题");
                        break;
                    case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                        Log.e("ActivityDemo", "没有语音输入");
                        break;
                    case SpeechRecognizer.ERROR_CLIENT:
                        Log.e("ActivityDemo", "其它客户端错误");
                        break;
                    case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                        Log.e("ActivityDemo", "权限不足");
                        break;
                    case SpeechRecognizer.ERROR_NETWORK:
                        Log.e("ActivityDemo", "网络问题");
                        break;
                    case SpeechRecognizer.ERROR_NO_MATCH:
                        Log.e("ActivityDemo", "没有匹配的识别结果");
                        break;
                    case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                        Log.e("ActivityDemo", "引擎忙");
                        break;
                    case SpeechRecognizer.ERROR_SERVER:
                        Log.e("ActivityDemo", "服务端错误");
                        break;
                    case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                        Log.e("ActivityDemo", "连接超时");
                        break;
                }
            }

            @Override
            public void onResults(Bundle results) {
                String allResults = "";
                ArrayList<String> bestResults = results.getStringArrayList("results_recognition");
                for(String result : bestResults) {
                    allResults += result;
                    allResults += "\n";
                }
                resultList.setText(allResults + "\n");
                SendClientTask sendMessage = new SendClientTask(main.serverIP, 8080, (String)bestResults.toArray()[0]);
                sendMessage.execute();

                sendCommand((String)bestResults.toArray()[0]);
            }

            @Override
            public void onPartialResults(Bundle partialResults) {

            }

            @Override
            public void onEvent(int eventType, Bundle params) {

            }
        };

        mSpeechRecognizer.setRecognitionListener(listener);

        recognizerIntent = new Intent();

        bindParams(recognizerIntent);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(main);
        {
            String args = sp.getString("args", "");
            if (null != args) {
                recognizerIntent.putExtra("args", args);
            }
        }

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSpeechRecognizer.startListening(recognizerIntent);
            }
        });
    }

    public void bindParams(Intent intent) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(main);
        if (sp.getBoolean("tips_sound", true)) {
            intent.putExtra(Constant.EXTRA_SOUND_START, R.raw.bdspeech_recognition_start);
            intent.putExtra(Constant.EXTRA_SOUND_END, R.raw.bdspeech_speech_end);
            intent.putExtra(Constant.EXTRA_SOUND_SUCCESS, R.raw.bdspeech_recognition_success);
            intent.putExtra(Constant.EXTRA_SOUND_ERROR, R.raw.bdspeech_recognition_error);
            intent.putExtra(Constant.EXTRA_SOUND_CANCEL, R.raw.bdspeech_recognition_cancel);
        }
        if (sp.contains(Constant.EXTRA_INFILE)) {
            String tmp = sp.getString(Constant.EXTRA_INFILE, "").replaceAll(",.*", "").trim();
            intent.putExtra(Constant.EXTRA_INFILE, tmp);
        }
        if (sp.getBoolean(Constant.EXTRA_OUTFILE, false)) {
            intent.putExtra(Constant.EXTRA_OUTFILE, "sdcard/outfile.pcm");
        }
        if (sp.contains(Constant.EXTRA_SAMPLE)) {
            String tmp = sp.getString(Constant.EXTRA_SAMPLE, "").replaceAll(",.*", "").trim();
            if (null != tmp && !"".equals(tmp)) {
                intent.putExtra(Constant.EXTRA_SAMPLE, Integer.parseInt(tmp));
            }
        }
        if (sp.contains(Constant.EXTRA_LANGUAGE)) {
            String tmp = sp.getString(Constant.EXTRA_LANGUAGE, "").replaceAll(",.*", "").trim();
            if (null != tmp && !"".equals(tmp)) {
                intent.putExtra(Constant.EXTRA_LANGUAGE, tmp);
            }
        }
        if (sp.contains(Constant.EXTRA_NLU)) {
            String tmp = sp.getString(Constant.EXTRA_NLU, "").replaceAll(",.*", "").trim();
            if (null != tmp && !"".equals(tmp)) {
                intent.putExtra(Constant.EXTRA_NLU, tmp);
            }
        }

        if (sp.contains(Constant.EXTRA_VAD)) {
            String tmp = sp.getString(Constant.EXTRA_VAD, "").replaceAll(",.*", "").trim();
            if (null != tmp && !"".equals(tmp)) {
                intent.putExtra(Constant.EXTRA_VAD, tmp);
            }
        }
        String prop = null;
        if (sp.contains(Constant.EXTRA_PROP)) {
            String tmp = sp.getString(Constant.EXTRA_PROP, "").replaceAll(",.*", "").trim();
            if (null != tmp && !"".equals(tmp)) {
                intent.putExtra(Constant.EXTRA_PROP, Integer.parseInt(tmp));
                prop = tmp;
            }
        }

        // offline asr
        {
            intent.putExtra(Constant.EXTRA_OFFLINE_ASR_BASE_FILE_PATH, "/sdcard/easr/s_1");
            intent.putExtra(Constant.EXTRA_LICENSE_FILE_PATH, "/sdcard/easr/license-tmp-20150530.txt");
            if (null != prop) {
                int propInt = Integer.parseInt(prop);
                if (propInt == 10060) {
                    intent.putExtra(Constant.EXTRA_OFFLINE_LM_RES_FILE_PATH, "/sdcard/easr/s_2_Navi");
                } else if (propInt == 20000) {
                    intent.putExtra(Constant.EXTRA_OFFLINE_LM_RES_FILE_PATH, "/sdcard/easr/s_2_InputMethod");
                }
            }
            intent.putExtra(Constant.EXTRA_OFFLINE_SLOT_DATA, buildTestSlotData());
        }
    }

    private String buildTestSlotData() {
        JSONObject slotData = new JSONObject();
        JSONArray name = new JSONArray().put("李涌泉").put("郭下纶");
        JSONArray song = new JSONArray().put("七里香").put("发如雪");
        JSONArray artist = new JSONArray().put("周杰伦").put("李世龙");
        JSONArray app = new JSONArray().put("手机百度").put("百度地图");
        JSONArray usercommand = new JSONArray().put("关灯").put("开门");
        try {
            slotData.put(Constant.EXTRA_OFFLINE_SLOT_NAME, name);
            slotData.put(Constant.EXTRA_OFFLINE_SLOT_SONG, song);
            slotData.put(Constant.EXTRA_OFFLINE_SLOT_ARTIST, artist);
            slotData.put(Constant.EXTRA_OFFLINE_SLOT_APP, app);
            slotData.put(Constant.EXTRA_OFFLINE_SLOT_USERCOMMAND, usercommand);
        } catch (JSONException e) {

        }
        return slotData.toString();
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
        } else {
            main.blueTooth.sendInformation(main.CarSocket, "40\n");
            if(Looper.myLooper() == null) {
                Looper.prepare();
            }
            try {
                Thread.currentThread().sleep(2000);
            } catch (Exception e) {}
            main.blueTooth.sendInformation(main.CarSocket,"00\n");
        }

    }

}
