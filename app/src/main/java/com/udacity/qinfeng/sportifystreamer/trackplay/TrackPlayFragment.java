package com.udacity.qinfeng.sportifystreamer.trackplay;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.udacity.qinfeng.sportifystreamer.R;
import com.udacity.qinfeng.sportifystreamer.model.SSTrack;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;


public class TrackPlayFragment extends DialogFragment {

    public static final String PARAM_SELECTED_TRACK_ID="trackId";
    public static final String TRACK_LIST ="trackList";

    private OnFragmentInteractionListener mListener;

    private String selectedTrackId;
    private String previousTrackId;
    private String nextTrackId;
    private ArrayList<SSTrack> trackList;

    public static final String SELECTED_TRACK_ID="selectedTrackId";
    public static final String SEEKBAR_POS = "seekbarPos";

    private TextView artistNameTextView;
    private TextView albumNameTextView;
    private ImageView artworkImageView;
    private TextView trackNameTextView;

    //play control
    private SeekBar musicControlBar;
    private TextView musicCurrentTime;
    private TextView musicEndTime;
    private ImageButton previousTrackBtn;
    private ImageButton playBtn;
    private ImageButton nextTrackBtn;
    private boolean playModeDialog=false;

    private boolean musicSuspended = false;
    private boolean musicPlaying = false;
    private static final String MUSIC_PLAYING="musicPlaying";
    private static final String MUSIC_SUSPENDED="musicSuspended";

    private Picasso picasso;

    //buffering variables
    private boolean mBufferingBroadcastReceiverRegistered;
    private ProgressDialog pdBuff;

    //seekbar variables
    private boolean mSeekBroadcastReceiverRegistered;
    private int mMusicPosition;
    private static final String MUSIC_POSITION="musicPosition";
    private int mMusicMax;
    private static final String MUSIC_MAX="musicMax";


    public static final String SEEKBAR_POS_BROADCAST="seekbar_pos_broadcast";
    public static final String MUSIC_PLAY_BROADCAST="music_play_broadcast";
    public static final String MUSIC_PLAY_ACTION="musicPlayAction";



    private Intent musicServiceIntent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        picasso = Picasso.with(getActivity());
        playModeDialog=getResources().getBoolean(R.bool.playModeDialog);
        muiscResumeIntent.putExtra(MUSIC_PLAY_ACTION,MusicPlayAction.Resume.toString());
        musicPauseIntent.putExtra(MUSIC_PLAY_ACTION,MusicPlayAction.Pause.toString());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SELECTED_TRACK_ID, selectedTrackId);
        outState.putParcelableArrayList(TRACK_LIST, trackList);
        outState.putBoolean(MUSIC_PLAYING, musicPlaying);
        outState.putBoolean(MUSIC_SUSPENDED,musicSuspended);

        outState.putInt(MUSIC_MAX, mMusicMax);
        outState.putInt(MUSIC_POSITION,mMusicPosition);
    }

    private void handlePd(Intent intent){

        String bufferState = intent.getStringExtra(MusicPlayService.BUFFERING);
        if(BufferingState.ON_GOING.toString().equals(bufferState)){
            pdBuff = ProgressDialog.show(getActivity(),null,null,true);
        }else if(BufferingState.DONE.toString().equals(bufferState)){
            if(pdBuff!=null){
                pdBuff.dismiss();
            }
        }
    }

    private BroadcastReceiver bufferBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            handlePd(intent);
        }
    };

    private BroadcastReceiver seekBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            handleMusicProgress(intent);
        }
    };

    private void handleMusicProgress(Intent intent) {
        boolean musicEnded = intent.getBooleanExtra(MusicPlayService.SOND_ENDED,false);

        if(musicEnded){
            musicCurrentTime.setText(musicEndTime.getText());
            musicControlBar.setProgress(mMusicMax);
            playBtn.setImageResource(android.R.drawable.ic_media_play);
            musicPlaying=false;
        }else{
            mMusicMax = intent.getIntExtra(MusicPlayService.MEDIA_MAX,0);
            mMusicPosition = intent.getIntExtra(MusicPlayService.MEDIA_POSITION,0);

            drawMusicPositionAndTime();
        }
    }

    private void drawMusicPositionAndTime(){
        musicControlBar.setMax(mMusicMax);
        musicControlBar.setProgress(mMusicPosition);
        musicEndTime.setText(convertSecondToMMSSString(mMusicMax));
        musicCurrentTime.setText(convertSecondToMMSSString(mMusicPosition));
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mBufferingBroadcastReceiverRegistered){
            getActivity().unregisterReceiver(bufferBroadcastReceiver);
            mBufferingBroadcastReceiverRegistered=false;
        }
        if(mSeekBroadcastReceiverRegistered){
            getActivity().unregisterReceiver(seekBroadcastReceiver);
            mSeekBroadcastReceiverRegistered=false;
        }
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_track_play, container, false);

        attachValues(view);
        if(savedInstanceState!=null){
            selectedTrackId = savedInstanceState.getString(SELECTED_TRACK_ID);
            trackList = savedInstanceState.getParcelableArrayList(TRACK_LIST);
            musicPlaying = savedInstanceState.getBoolean(MUSIC_PLAYING);
            musicSuspended = savedInstanceState.getBoolean(MUSIC_SUSPENDED);
            mMusicPosition = savedInstanceState.getInt(MUSIC_POSITION);
            mMusicMax = savedInstanceState.getInt(MUSIC_MAX);
        }else{
            selectedTrackId = mListener.getSelectedTrackId();
            trackList = mListener.getTrackList();
        }


        final SSTrack ssTrack = getSelectedTrack(selectedTrackId,trackList);

        setTrackInfoOnScreen(ssTrack);

        setMusicControlBarOnChangeListener();

        setPlayButtonOnclickListener(ssTrack);

        drawPlayBtn();

        drawMusicPositionAndTime();

        previousTrackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SSTrack previousTrack = getSelectedTrack(previousTrackId, trackList);
                if (previousTrack != null) {
                    playTrack(previousTrack);
                } else {
                    Toast.makeText(getActivity(), R.string.already_begin_of_play_list, Toast.LENGTH_SHORT).show();
                }
            }
        });

        nextTrackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SSTrack nextTrack = getSelectedTrack(nextTrackId, trackList);
                if (nextTrack != null) {
                    playTrack(nextTrack);
                } else {
                    Toast.makeText(getActivity(), R.string.already_end_of_play_list, Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        return dialog;
    }

    @Override
    public void onResume() {


        if(!mBufferingBroadcastReceiverRegistered){
            getActivity().registerReceiver(bufferBroadcastReceiver,
                    new IntentFilter(MusicPlayService.BROADCAST_BUFFER));
            mBufferingBroadcastReceiverRegistered=true;
        }

        if(!mSeekBroadcastReceiverRegistered){
            getActivity().registerReceiver(seekBroadcastReceiver,
                    new IntentFilter(MusicPlayService.BROADCAST_SEEKBAR));
            mSeekBroadcastReceiverRegistered=true;
        }

        super.onResume();

        if(playModeDialog){
            Window window = getDialog().getWindow();
            int dialogWidth = getResources().getDimensionPixelSize(R.dimen.dialogWidth);
            int dialogHeight = getResources().getDimensionPixelSize(R.dimen.dialogHeight);
            window.setLayout(dialogWidth, dialogHeight);
            window.setGravity(Gravity.CENTER);
        }


    }

    private void playTrack(final SSTrack ssTrack){
        setTrackInfoOnScreen(ssTrack);
        selectedTrackId = ssTrack.getId();
        playAudio(ssTrack);
        drawPlayBtn();
    }

    private void setTrackInfoOnScreen(SSTrack ssTrack) {
        artistNameTextView.setText(ssTrack.getArtistName());
        albumNameTextView.setText(ssTrack.getAlbumName());
        picasso.load(ssTrack.getArtworkUrl()).into(artworkImageView);
        trackNameTextView.setText(ssTrack.getName());
    }

    private void setMusicControlBarOnChangeListener() {
        musicControlBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    Intent seekbarIntent = new Intent(SEEKBAR_POS_BROADCAST);
                    seekbarIntent.putExtra(SEEKBAR_POS, progress);
                    getActivity().sendBroadcast(seekbarIntent);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }


    private void setPlayButtonOnclickListener(final SSTrack ssTrack) {
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!musicPlaying) {
                    playAudio(ssTrack);
                } else {
                    pauseAudio();
                }
                drawPlayBtn();
            }
        });
    }

    private void drawPlayBtn(){
        playBtn.setImageResource(musicPlaying?android.R.drawable.ic_media_pause:android.R.drawable.ic_media_play);
    }

    private Intent musicPauseIntent = new Intent(MUSIC_PLAY_BROADCAST);
    private Intent muiscResumeIntent = new Intent(MUSIC_PLAY_BROADCAST);
    private void playAudio(SSTrack ssTrack){
        if(musicSuspended){
            getActivity().sendBroadcast(muiscResumeIntent);
            musicSuspended=false;

        }else{
            musicServiceIntent = new Intent(getActivity(),MusicPlayService.class);
            musicServiceIntent.putExtra(MusicPlayService.AUDIO_URL, ssTrack.getPreviewUrl());
            getActivity().startService(musicServiceIntent);
        }
        musicPlaying = true;

    }

    private void pauseAudio(){

        getActivity().sendBroadcast(musicPauseIntent);
        musicPlaying = false;
        musicSuspended = true;
    }


    private void attachValues(View view) {
        artistNameTextView = (TextView) view.findViewById(R.id.artistNameTextView);
        albumNameTextView = (TextView) view.findViewById(R.id.albumNameTextView);
        artworkImageView = (ImageView) view.findViewById(R.id.albumImageView);
        trackNameTextView = (TextView) view.findViewById(R.id.trackNameTextView);

        //music control
        musicControlBar = (SeekBar)view.findViewById(R.id.musicControlBar);
        musicCurrentTime =(TextView)view.findViewById(R.id.musicCurrentTime);
        musicEndTime = (TextView)view.findViewById(R.id.musicEndTime);
        previousTrackBtn = (ImageButton)view.findViewById(R.id.musicPreviousBtn);
        playBtn = (ImageButton)view.findViewById(R.id.musicPlayBtn);
        nextTrackBtn = (ImageButton)view.findViewById(R.id.musicNextBtn);
    }


    private SSTrack getSelectedTrack(String selectedTrackId, ArrayList<SSTrack> trackList){
        if(selectedTrackId == null) return null;
        for(int i=0; i<trackList.size(); i++){
            SSTrack currentTrack = trackList.get(i);
            if(selectedTrackId.equals(currentTrack.getId())){ // when find track, reset previous track Id and next track id
                previousTrackId = i>0?trackList.get(i-1).getId():null;
                nextTrackId = (i<trackList.size()-1)?trackList.get(i+1).getId():null;
                return currentTrack;
            }
        }
        return null;
    }



    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {

        String getSelectedTrackId();

        ArrayList<SSTrack> getTrackList();
    }

    private String convertSecondToMMSSString(long millSeconds)
    {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        SimpleDateFormat df = new SimpleDateFormat("m:ss");
        df.setTimeZone(tz);
        String time = df.format(new Date(millSeconds));

        return time;

    }

}
