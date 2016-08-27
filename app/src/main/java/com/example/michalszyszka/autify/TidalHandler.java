package com.example.michalszyszka.autify;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.util.Pair;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by michalszyszka on 20.03.2016.
 */
public class TidalHandler implements RecognitionListener {

    private final String TAG = this.getClass().getSimpleName();

    private SpeechRecognizer speech;
    private List<Pair<String, String>> playlists;
    private TidalHandlerFinishedListener handlerListener;
    private boolean alreadyHandled;

    private AudioManager audioManager;
    private int volume;

    private static TidalHandler instance;
    private static Random random;
    private final static Object synchroObj = new Object();

    private TidalHandler(){
        random = new Random();
    }

    public static TidalHandler getInstance(){
        if(instance == null){
            instance = new TidalHandler();
        }

        return instance;
    }

    public synchronized void prepare(Context context){
        String[] cmd = {"cp /data/data/com.aspiro.tidal/databases/wimp.db /data/data/com.example.michalszyszka.autify/databases/wimp.db"};

        try {
            runAsRoot(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        }

        DBHelper dbHelper = new DBHelper(context);
        try{
            playlists = dbHelper.getAllOfflinePlaylists();
        } catch (Exception ex) {
            Log.d(TAG, ex.getMessage());
            return;
        }

        for(Pair<String, String> pair : playlists) {
            Log.d(TAG, pair.first + " " + pair.second);
        }

        speech = SpeechRecognizer.createSpeechRecognizer(context);
        speech.setRecognitionListener(this);
    }

    public synchronized void startPlay(Context context, TidalHandlerFinishedListener handlerListener,
                                       AudioManager audioManager, int volume){

        this.handlerListener = handlerListener;
        this.audioManager = audioManager;
        this.volume = volume;

        alreadyHandled = false;

        Intent recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
                "en");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                context.getPackageName());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);

        if(speech != null){
            speech.startListening(recognizerIntent);
        }
    }

    public void stopListening(){

        if(speech != null){
            speech.stopListening();
        }
    }

    public void destroy(){

        if(speech != null){
            speech.destroy();
        }
    }

    private void runAsRoot(String[] cmds) throws IOException {
        Process p = Runtime.getRuntime().exec("su");
        DataOutputStream os = new DataOutputStream(p.getOutputStream());

        for (String tmpCmd : cmds) {
            os.writeBytes(tmpCmd+"\n");
        }

        os.writeBytes("exit\n");
        os.flush();

        try {
            p.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onReadyForSpeech(Bundle params) {
        //Intentionally left blank
    }

    @Override
    public void onBeginningOfSpeech() {
        //Intentionally left blank
    }

    @Override
    public void onRmsChanged(float rmsdB) {
        //Intentionally left blank
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        //Intentionally left blank
    }

    @Override
    public void onEndOfSpeech() {
        speech.stopListening();
    }

    @Override
    public void onError(int error) {
        synchronized (synchroObj){
            Log.i(TAG, "error: " + error);
            speech.stopListening();
            speech.setRecognitionListener(null);

            if(!alreadyHandled){
                startPlaying(new ArrayList<String>());
            }
        }
    }

    @Override
    public void onResults(Bundle results) {
        Log.i(TAG, "onResults");
        ArrayList<String> matches = results
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        String text = "";
        for (String result : matches)
            text += result + "\n";

        Log.d(TAG, text);

        startPlaying(matches);
    }

    @Override
    public void onPartialResults(Bundle partialResults) {
        //Intentionally left blank
    }

    @Override
    public void onEvent(int eventType, Bundle params) {
        //Intentionally left blank
    }

    private void startPlaying(ArrayList<String> matches) {
        synchronized(synchroObj){

            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, AudioManager.FLAG_VIBRATE);

            String foundPlaylist = "";
            for (Pair<String, String> playlistPair : playlists){
                for (String recognizedWord : matches){
                    if(playlistPair.first.toLowerCase().contains(recognizedWord.toLowerCase())){

                        Log.d(TAG, "found playlist: " + playlistPair.first);
                        foundPlaylist = playlistPair.second;
                        break;

                    }
                }
            }

            if(foundPlaylist.equals("")){
                Log.d(TAG, "no playlist found, randomizing playlist to play" );

                int playlistToPlay = random.nextInt(playlists.size());
                foundPlaylist = playlists.get(playlistToPlay).second;
            }

            String commandToRun = String.format("am start -n com.aspiro.tidal/com.aspiro.wamp.LoginFragmentActivity -d \"tidal://play/playlist/%s\"", foundPlaylist);
            String[] firstSet = {commandToRun};

            try {
                Log.d(TAG, commandToRun);
                runAsRoot(firstSet);

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                alreadyHandled = true;

                if(handlerListener != null){
                    handlerListener.onTidalHandlerFinished();
                }
            }
        }
    }
}
