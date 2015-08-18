package com.udacity.qinfeng.sportifystreamer.trackplay;

import android.app.Activity;
import android.app.Dialog;
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
    private View searchingIcon;
    private boolean playModeDialog=false;

    private boolean playPrepared=false;

    private Picasso picasso;
    private MediaPlayer mediaPlayer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        picasso = Picasso.with(getActivity());
        playModeDialog=getResources().getBoolean(R.bool.playModeDialog);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SELECTED_TRACK_ID,selectedTrackId);
        outState.putParcelableArrayList(TRACK_LIST,trackList);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_track_play, container, false);

        attachValues(view);
        if(savedInstanceState!=null){
            selectedTrackId = savedInstanceState.getString(SELECTED_TRACK_ID);
            trackList = savedInstanceState.getParcelableArrayList(TRACK_LIST);
        }else{
            selectedTrackId = mListener.getSelectedTrackId();
            trackList = mListener.getTrackList();
        }


        final SSTrack ssTrack = getSelectedTrack(selectedTrackId,trackList);

        setTrackInfoOnScreen(ssTrack);

        setMusicControlBarOnChangeListener();

        setPlayButtonOnclickListener(ssTrack);

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
        // The only reason you might override this method when using onCreateView() is
        // to modify any dialog characteristics. For example, the dialog includes a
        // title by default, but your custom layout might not need it. So here you can
        // remove the dialog title, but you must call the superclass to get the Dialog.
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        return dialog;
    }

    @Override
    public void onResume() {
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
        mediaPlayer.reset();
        setTrackInfoOnScreen(ssTrack);
        selectedTrackId = ssTrack.getId();
        prepareAsync(ssTrack);
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
                if (fromUser && playPrepared) {
                    musicCurrentTime.setText(convertSecondToMMSSString(progress));
                    mediaPlayer.seekTo(progress);
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

                if (mediaPlayer == null) { //initialisation
                    initMediaPlayer();
                }

                if (!mediaPlayer.isPlaying()) {
                    if (!playPrepared) {
                        prepareAsync(ssTrack);
                    } else {
                        mediaPlayer.start();
                        changePlayBtnStatus();
                    }
                } else {
                    mediaPlayer.pause();
                    changePlayBtnStatus();
                }
            }
        });

        playBtn.performClick();
        playBtn.setClickable(false);
    }

    private void prepareAsync(SSTrack ssTrack) {
        try {
            mediaPlayer.setDataSource(ssTrack.getPreviewUrl());
        } catch (IOException e) {
            Toast.makeText(getActivity(), getString(R.string.read_track_error), Toast.LENGTH_SHORT).show();
        }
        showSearching();
        mediaPlayer.prepareAsync();
    }

    private void initMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                playPrepared = true;
                hideSearching();
                playBtn.setClickable(true);
                mediaPlayer.start();
                int duration = mediaPlayer.getDuration();
                musicControlBar.setProgress(0);
                musicControlBar.setMax(duration);

                musicEndTime.setText(convertSecondToMMSSString(duration));
                changePlayBtnStatus();
                //from http://stackoverflow.com/questions/11687011/run-loop-every-second-java
                (new Thread(new Runnable() {

                    @Override
                    public void run() {
                        while (!Thread.interrupted())
                            try {
                                if(getActivity()!=null){
                                    getActivity().runOnUiThread(new Runnable() // start actions in UI thread
                                    {
                                        @Override
                                        public void run() {
                                            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                                                int currentPositionInMs = mediaPlayer.getCurrentPosition();
                                                musicControlBar.setProgress(currentPositionInMs);
                                                musicCurrentTime.setText(convertSecondToMMSSString(currentPositionInMs));
                                            }
                                        }

                                    });
                                    Thread.sleep(100);
                                }
                            } catch (InterruptedException e) {
                                // ooops
                            }
                        Log.i("spotifyStreamer", "music played");
                    }
                })).start();

            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                playPrepared = false;
                musicCurrentTime.setText(convertSecondToMMSSString(mediaPlayer.getDuration()));//be sure of finished
                changePlayBtnStatus();
                //play next song if we have
                if(nextTrackId!=null){
                    nextTrackBtn.performClick();
                }
            }
        });
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
        searchingIcon = view.findViewById(R.id.loadingIcon);
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
        if(mediaPlayer!=null){
            mediaPlayer.release();
            mediaPlayer = null;
        }
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

    private void changePlayBtnStatus(){
       playBtn.setImageResource(mediaPlayer.isPlaying()?android.R.drawable.ic_media_pause:android.R.drawable.ic_media_play);
    }

    private void hideSearching(){
        searchingIcon.setVisibility(View.GONE);
    }
    private void showSearching(){
        searchingIcon.setVisibility(View.VISIBLE);
    }

}
