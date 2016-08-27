package com.example.michalszyszka.autify;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends Activity implements TidalHandlerFinishedListener{

    private final String TAG = this.getClass().getSimpleName();

    private AudioManager audioManager;
    private int streamVolume;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        audioManager =
                (AudioManager)getSystemService(Context.AUDIO_SERVICE);

        streamVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), AudioManager.FLAG_VIBRATE);

        TidalHandler.getInstance().prepare(this);
    }

    @Override
    protected void onDestroy() {
        TidalHandler.getInstance().destroy();
        super.onDestroy();

        Log.d(TAG, "onDestroy");
    }

    @Override
    protected void onResume() {
        super.onResume();
        TidalHandler.getInstance().startPlay(this, this, audioManager, streamVolume);

        Log.d(TAG, "onResume");
    }

    @Override
    protected void onPause() {
        TidalHandler.getInstance().stopListening();
        super.onPause();

        Log.d(TAG, "onPause");
    }

    @Override
    public void onTidalHandlerFinished() {
        Log.d(TAG, "finishing");

        if(Build.VERSION.SDK_INT >= 21){
            finishAndRemoveTask();
        } else {
            finish();
        }
    }
}

interface TidalHandlerFinishedListener{
    void onTidalHandlerFinished();
}
