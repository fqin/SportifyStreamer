package com.udacity.qinfeng.sportifystreamer.toptracks;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.udacity.qinfeng.sportifystreamer.R;
import com.udacity.qinfeng.sportifystreamer.model.SSTrack;
import com.udacity.qinfeng.sportifystreamer.util.CountryManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TopTracksFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class TopTracksFragment extends Fragment {

    public static final String KEY_PARAM_ARTIST_ID = "artist_id";

    public static final String SAVE_TOP_TRACKS_LIST_KEY = "topTracks";


    private SpotifyService sportifyService = new SpotifyApi().getService();

    private OnFragmentInteractionListener mListener;
    private ListView topTracksListView;
    private TextView emptyMsg; //textView for empty message
    private View searchingIcon;

    private MyAdapter listAdapter;

    private ArrayList<SSTrack> mTopTracks;
    private String artistId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_top_tracks, container, false);
        topTracksListView = (ListView)view.findViewById(R.id.topTrackslistView);
        searchingIcon = view.findViewById(R.id.loadingIcon);
        emptyMsg = (TextView)view.findViewById(R.id.emptyMsg);
        hideEmptyMsg();
        hideSearching();
        mTopTracks = new ArrayList<>();

        listAdapter = new MyAdapter(getActivity(), R.layout.track_item);
        topTracksListView.setAdapter(listAdapter);
        topTracksListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mListener.onTrackSelected(listAdapter.getItem(position).getId(), mTopTracks);
            }
        });

        artistId = mListener.getArtistId();

        if(savedInstanceState!=null){
            mTopTracks = savedInstanceState.getParcelableArrayList(SAVE_TOP_TRACKS_LIST_KEY);
            setListWithNewValue();

        }else{
            new SearchTopTrackTask().execute();
        }



        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(SAVE_TOP_TRACKS_LIST_KEY, mTopTracks);
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

        String getArtistId();

        void onTrackSelected(String trackId, ArrayList<SSTrack> ssTracks);
    }

    private class MyAdapter extends ArrayAdapter<SSTrack> {
        private int ressource;
        private Picasso picasso;

        public MyAdapter(Context context, int resource) {
            super(context, resource);
            this.ressource = resource;
            this.picasso = Picasso.with(getContext());
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if(convertView == null){

                //inflation
                LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(Service.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(this.ressource, parent, false);

                //initialize view holder
                viewHolder = new ViewHolder();
                viewHolder.imageView = (ImageView)convertView.findViewById(R.id.trackAlbumImageView);
                viewHolder.trackName = (TextView)convertView.findViewById(R.id.trackNameTextView);
                viewHolder.albumName = (TextView)convertView.findViewById(R.id.albumNameTextView);
                convertView.setTag(viewHolder);
            }else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            //update values
            SSTrack track = getItem(position);
            if(track.getImageUrl()!=null){
                picasso.load(track.getImageUrl()).into(viewHolder.imageView);
            }else{
                viewHolder.imageView.setImageResource(R.drawable.ic_spotifyicon);
            }
            viewHolder.trackName.setText(track.getName());
            viewHolder.albumName.setText(track.getAlbumName());
            return convertView;
        }
    }

    private static class ViewHolder {
        ImageView imageView;
        TextView trackName;
        TextView albumName;
    }

    private class SearchTopTrackTask extends AsyncTask<Void, Void, List<SSTrack>> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showSearching();
        }

        @Override
        protected List<SSTrack> doInBackground(Void... params) {

            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put(CountryManager.COUNTRY_PARAM,
                    CountryManager.getCountryCode(getActivity()));

            Tracks results = sportifyService.getArtistTopTrack(artistId, paramMap);

            List<Track> tracks = results.tracks;
            List<SSTrack> ssTracks = new ArrayList<>();
            for (Track track : tracks){

                ssTracks.add(getSSTrack(track));
            }

            return ssTracks;
        }

        @Override
        protected void onPostExecute(List<SSTrack> tracks) {
            super.onPostExecute(tracks);
            hideSearching();
            mTopTracks.clear();
            mTopTracks.addAll(tracks);
            setListWithNewValue();
        }
    }

    private SSTrack getSSTrack(Track track) {

        String imageUrl=null;
        String artworkUrl = null;
        if(track.album != null && track.album.images!=null && track.album.images.size()>1){
            imageUrl = track.album.images.get(track.album.images.size()-1).url;
            artworkUrl = track.album.images.get(track.album.images.size()-2).url;
        }
        StringBuilder artistStrBuilder = new StringBuilder();
        for(int i=0; i<track.artists.size(); i++){
            artistStrBuilder.append(track.artists.get(i).name);
            if(i<track.artists.size()-1){
                artistStrBuilder.append(", ");
            }
        }

        return new SSTrack(track.name, track.album==null?"":track.album.name, imageUrl,
                track.id,artistStrBuilder.toString(), artworkUrl, track.duration_ms);
    }

    private void setListWithNewValue(){
        listAdapter.clear();
        for (SSTrack track:mTopTracks){
            listAdapter.add(track);
        }
        if(mTopTracks.size()==0){
            showEmptyMsg();
        }
    }

    private void hideSearching(){
        searchingIcon.setVisibility(View.GONE);
    }
    private void showSearching(){
        searchingIcon.setVisibility(View.VISIBLE);
    }
    private void hideEmptyMsg(){
        emptyMsg.setVisibility(View.GONE);
    }
    private void showEmptyMsg(){
        emptyMsg.setVisibility(View.VISIBLE);
    }

}
