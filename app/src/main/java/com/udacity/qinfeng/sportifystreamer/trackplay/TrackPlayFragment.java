package com.udacity.qinfeng.sportifystreamer.trackplay;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.udacity.qinfeng.sportifystreamer.R;
import com.udacity.qinfeng.sportifystreamer.model.SSTrack;

import java.util.ArrayList;


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


    private Picasso picasso;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        picasso = Picasso.with(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_track_play, container, false);

        artistNameTextView = (TextView) view.findViewById(R.id.artistNameTextView);
        albumNameTextView = (TextView) view.findViewById(R.id.albumNameTextView);
        artworkImageView = (ImageView) view.findViewById(R.id.albumImageView);
        trackNameTextView = (TextView) view.findViewById(R.id.trackNameTextView);

        selectedTrackId = mListener.getSelectedTrackId();
        trackList = mListener.getTrackList();
        SSTrack ssTrack = getSelectedTrack(selectedTrackId,trackList);

        artistNameTextView.setText(ssTrack.getArtistName());
        albumNameTextView.setText(ssTrack.getAlbumName());
        picasso.load(ssTrack.getArtworkUrl()).into(artworkImageView);
        trackNameTextView.setText(ssTrack.getName());

        return view;
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
    }

    public interface OnFragmentInteractionListener {

        String getSelectedTrackId();

        ArrayList<SSTrack> getTrackList();
    }

}
