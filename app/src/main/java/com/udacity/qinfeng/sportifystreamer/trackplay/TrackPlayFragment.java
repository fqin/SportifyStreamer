package com.udacity.qinfeng.sportifystreamer.trackplay;

import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.udacity.qinfeng.sportifystreamer.R;
import com.udacity.qinfeng.sportifystreamer.model.SSTrack;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;


public class TrackPlayFragment extends Fragment {

    public static final String PARAM_SELECTED_TRACK_ID="trackId";
    public static final String PARAM_TRACK_LIST="trackList";

    private OnFragmentInteractionListener mListener;

    private String selectedTrackId;
    private ArrayList<SSTrack> trackList;


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

    private boolean playing = false;
    private boolean playPrepared=false;
    private boolean musicPlayed=false;

    private Picasso picasso;
    private MediaPlayer mediaPlayer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        picasso = Picasso.with(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_track_play, container, false);

        attachValues(view);

        selectedTrackId = mListener.getSelectedTrackId();
        trackList = mListener.getTrackList();
        final SSTrack ssTrack = getSelectedTrack(selectedTrackId,trackList);

        artistNameTextView.setText(ssTrack.getArtistName());
        albumNameTextView.setText(ssTrack.getAlbumName());
        picasso.load(ssTrack.getArtworkUrl()).into(artworkImageView);
        trackNameTextView.setText(ssTrack.getName());

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

        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!playing) {
                    if (mediaPlayer == null) { //initialisation
                        mediaPlayer = new MediaPlayer();
                        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                            @Override
                            public void onPrepared(MediaPlayer mp) {
                                playPrepared = true;
                                playBtn.setClickable(true);
                                mediaPlayer.start();
                                playing = true;
                                int duration = mediaPlayer.getDuration();
                                musicControlBar.setProgress(0);
                                musicControlBar.setMax(duration);

                                musicEndTime.setText(convertSecondToMMSSString(duration));
                                changePlayBtnStatus();
                                //from http://stackoverflow.com/questions/11687011/run-loop-every-second-java
                                (new Thread(new Runnable() {

                                    @Override
                                    public void run() {
                                        while (!Thread.interrupted() && !musicPlayed)
                                            try {
                                                getActivity().runOnUiThread(new Runnable() // start actions in UI thread
                                                {
                                                    @Override
                                                    public void run() {
                                                            if(mediaPlayer!=null) {
                                                                int currentPositionInMs = mediaPlayer.getCurrentPosition();
                                                                musicControlBar.setProgress(currentPositionInMs);
                                                                musicCurrentTime.setText(convertSecondToMMSSString(currentPositionInMs));
                                                            }
                                                        }

                                                });
                                                Thread.sleep(100);
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
                                musicPlayed = true;
                                playPrepared = false;
                                playing = false;
                                musicCurrentTime.setText(convertSecondToMMSSString(mediaPlayer.getDuration()));//be sure of finished
                                changePlayBtnStatus();
                            }
                        });

                    }

                    if (!playPrepared) {
                        try {
                            if (!musicPlayed) {
                                mediaPlayer.setDataSource(ssTrack.getPreviewUrl());
                            } else {
                                musicPlayed = false;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();//TODO show something for user
                        }

                        mediaPlayer.prepareAsync();
                    } else {
                        mediaPlayer.start();
                        playing = true;
                        changePlayBtnStatus();
                    }


                } else {
                    mediaPlayer.pause();
                    playing = false;
                    changePlayBtnStatus();
                }
            }
        });

        playBtn.performClick();
        playBtn.setClickable(false);
        return view;
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
        for (SSTrack track : trackList){
            if(selectedTrackId.equals(track.getId())){
                return track;
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
        musicPlayed = true;
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
        SimpleDateFormat df = new SimpleDateFormat("mm:ss");
        df.setTimeZone(tz);
        String time = df.format(new Date(millSeconds));

        return time;

    }

    private void changePlayBtnStatus(){
       playBtn.setImageResource(playing?android.R.drawable.ic_media_pause:android.R.drawable.ic_media_play);
    }

}
