package com.udacity.qinfeng.sportifystreamer.trackplay;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;

import java.io.IOException;


public class MusicPlayService extends Service implements
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnSeekCompleteListener

{
    public static final String AUDIO_URL="audio_url";
    public static final String BUFFERING ="buffering_state";
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private String audioUrl;

    //set up broadcast identifier & intent
    public static final String BROADCAST_BUFFER
            ="com.udacity.qinfeng.sportifystreamer.trackplay.broadcastbuffer";
    private Intent bufferIntent;

    //variables for seekbar processing
    public static final String BROADCAST_SEEKBAR
            ="com.udacity.qinfeng.sportifystreamer.trackplay.broadcastseekbar";
    public static final String MEDIA_POSITION = "mediaPosition";
    public static final String MEDIA_MAX = "mediaMax";
    public static final String SOND_ENDED = "sondEnded";
    private int seekPosInMS;
    private int mediaPosition;
    private int mediaMax;
    private boolean songEnded;
    private final Handler handler = new Handler();
    private Intent seekIntent;




    @Override
    public void onCreate() {
        super.onCreate();

        //for buffering when music starts
        bufferIntent = new Intent(BROADCAST_BUFFER);
        //for seekbar
        seekIntent = new Intent(BROADCAST_SEEKBAR);

        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnSeekCompleteListener(this);
        mediaPlayer.reset();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        registerReceiver(seekbarPosBroadcastReceiver,
                new IntentFilter(TrackPlayFragment.SEEKBAR_POS_BROADCAST));
        registerReceiver(musicPlayActionBroadcastReceiver,
                new IntentFilter(TrackPlayFragment.MUSIC_PLAY_BROADCAST));


        audioUrl = intent.getExtras().getString(AUDIO_URL);
        mediaPlayer.reset();

        if(!mediaPlayer.isPlaying()){
            try {
                mediaPlayer.setDataSource(audioUrl);
                sendBufferingBroadcast();
                mediaPlayer.prepareAsync();
            } catch (IOException e) {

            }
        }

        return START_STICKY;
    }

    private void sendBufferingBroadcast() {
        bufferIntent.putExtra(BUFFERING, BufferingState.ON_GOING.toString());
        sendBroadcast(bufferIntent);
    }

    private void sendBufferingCompleteBroadcast(){
        bufferIntent.putExtra(BUFFERING, BufferingState.DONE.toString());
        sendBroadcast(bufferIntent);
    }

    private void setUpHandler(){
        handler.removeCallbacks(sendUpdateToUI);
        handler.post(sendUpdateToUI);
    }

    private Runnable sendUpdateToUI = new Runnable() {
        @Override
        public void run() {
            logAndSendMediaPlayInfo();
            handler.postDelayed(this, 1000); //1 seconde
        }
    };

    private void logAndSendMediaPlayInfo(){
        if(mediaPlayer.isPlaying()){
            mediaPosition = mediaPlayer.getCurrentPosition();
            mediaMax      = mediaPlayer.getDuration();
            seekIntent.putExtra(MEDIA_POSITION, mediaPosition);
            seekIntent.putExtra(MEDIA_MAX, mediaMax);
            sendBroadcast(seekIntent);
        }
    }

    private BroadcastReceiver seekbarPosBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateSeekPos(intent);
        }
    };

    private BroadcastReceiver musicPlayActionBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateMediaStatus(intent);
        }
    };

    private void updateMediaStatus(Intent intent) {
        if(MusicPlayAction.Pause.toString().equals(
                intent.getStringExtra(TrackPlayFragment.MUSIC_PLAY_ACTION))){
            mediaPlayer.pause();
        }else if(MusicPlayAction.Resume.toString().equals(
                intent.getStringExtra(TrackPlayFragment.MUSIC_PLAY_ACTION))) {
            mediaPlayer.start();
        }
    }

    private void updateSeekPos(Intent intent) {
        seekPosInMS = intent.getIntExtra(TrackPlayFragment.SEEKBAR_POS,0);
        if(mediaPlayer.isPlaying()){
            handler.removeCallbacks(sendUpdateToUI);
            mediaPlayer.seekTo(seekPosInMS);
            setUpHandler();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mediaPlayer!=null){
            if(mediaPlayer.isPlaying()){
                mediaPlayer.stop();
            }
            mediaPlayer.release();
        }

        handler.removeCallbacks(sendUpdateToUI);
        unregisterReceiver(seekbarPosBroadcastReceiver);
        unregisterReceiver(musicPlayActionBroadcastReceiver);
    }

    private void playMedia(){
        if(!mediaPlayer.isPlaying()){
            mediaPlayer.start();
        }
    }

    private void stopMedia(){
        if(mediaPlayer.isPlaying()){
            mediaPlayer.stop();
        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        seekIntent.putExtra(SOND_ENDED, true);
        sendBroadcast(seekIntent);
        stopMedia();
        stopSelf();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        sendBufferingCompleteBroadcast();
        setUpHandler();
        playMedia();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {

    }
}
