package com.example.shiyu.server;

import android.util.Log;

import com.baidu.tts.answer.auth.AuthInfo;
import com.baidu.tts.client.SpeechError;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;

/**
 * Created by shiyu on 15/12/25.
 */
public class VoiceGenerator extends Thread {
    private SpeechSynthesizer speechSynthesizer;
    private MainActivity main;

    public VoiceGenerator(MainActivity main) {
        this.main = main;
    }

    @Override
    public void run() {
        Log.e("", "a");
        speechSynthesizer = SpeechSynthesizer.getInstance();
        speechSynthesizer.setContext(main);

        speechSynthesizer.setSpeechSynthesizerListener(new SpeechSynthesizerListener() {
            @Override
            public void onSpeechStart(String string) {}

            @Override
            public void onSynthesizeStart(String string) {}

            @Override
            public void onSynthesizeDataArrived(String string, byte[] bytes, int num) {}

            @Override
            public void onSpeechProgressChanged(String string, int num) {}

            @Override
            public void onSynthesizeFinish(String string) {}

            @Override
            public void onSpeechFinish(String string) {}

            @Override
            public void onError(String string, SpeechError error) {
                Log.e("ActivityDemo", string);
                Log.e("ActivityDemo", "Error: " + error);
            }
        });

        speechSynthesizer.setAppId("7550395");
        speechSynthesizer.setApiKey("3Rovqla8yMim1HycW3u45SZY", "08105b2978a7404db6e93a38c8c6a919");

        speechSynthesizer.initTts(TtsMode.ONLINE);

        speechSynthesizer.setParam(SpeechSynthesizer.PARAM_VOLUME, "5");
        speechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEED, "5");
        speechSynthesizer.setParam(SpeechSynthesizer.PARAM_PITCH, "5");
        speechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEAKER,
                SpeechSynthesizer .SPEAKER_FEMALE);
        speechSynthesizer.setParam(SpeechSynthesizer.PARAM_AUDIO_ENCODE,
                SpeechSynthesizer.AUDIO_ENCODE_AMR);
        speechSynthesizer.setParam(SpeechSynthesizer.PARAM_AUDIO_RATE,
                SpeechSynthesizer.AUDIO_BITRATE_AMR_15K85);
        speechSynthesizer.setParam(SpeechSynthesizer.PARAM_VOCODER_OPTIM_LEVEL, "0");
    }

    public void speak(String message) {
        Log.e("ActivityDemo", "speaking " + message);
        int result = this.speechSynthesizer.speak(message);
        if (result < 0) {
            Log.e("ActivityDemo", "error" + result +",please look up error code in doc or URL:http://yuyin.baidu.com/docs/tts/122 ");
        }
    }
}
