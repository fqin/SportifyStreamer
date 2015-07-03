package com.udacity.qinfeng.sportifystreamer;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.udacity.qinfeng.sportifystreamer.model.SSArtist;
import com.udacity.qinfeng.sportifystreamer.toptracks.TopTracksActivity;
import com.udacity.qinfeng.sportifystreamer.toptracks.TopTracksFragment;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;


/**
 * A simple {@link Fragment} subclass.
 */
public class SearchArtistFragment extends Fragment {

    private static final String SEARCH_TEXT_KEY="searchText";

    private static final String SEARCH_RESULT_KEY="searchResults"; // for artists list

    private EditText editText;
    private ListView listView;
    private View searchingIcon;
    private TextView emptyMsg;
    private MyAdapter listAdapter;
    private CountDownTimer countDownTimer;
    private ArrayList<SSArtist> mSSArtists = new ArrayList<>();
    private String searchText;



    public SearchArtistFragment() {
        // Required empty public constructor
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
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search_artist, container, false);

        editText = (EditText) view.findViewById(R.id.searchArtistET);
        listView = (ListView) view.findViewById(R.id.artistListLV);
        searchingIcon = view.findViewById(R.id.loadingIcon);
        emptyMsg = (TextView)view.findViewById(R.id.emptyMsg);
        hideSearching();

        listAdapter = new MyAdapter(getActivity(),R.layout.artist_item);
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), TopTracksActivity.class);
                intent.putExtra(TopTracksFragment.KEY_PARAM_ARTIST_ID, ((SSArtist)parent.getAdapter().getItem(position)).getId());
                intent.putExtra(TopTracksActivity.KEY_PARAM_ARTIST_NAME, ((SSArtist)parent.getAdapter().getItem(position)).getName());
                startActivity(intent);
            }
        });

        if(savedInstanceState!=null){ // restore values

            //search field
            searchText = savedInstanceState.getString(SEARCH_TEXT_KEY);
            editText.setText(searchText);

            //result artist list
            mSSArtists = savedInstanceState.getParcelableArrayList(SEARCH_RESULT_KEY);
            setListWithNewValue();

        }


        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(final Editable editable) {

                if (countDownTimer != null) {
                    countDownTimer.cancel();
                }
                countDownTimer = new CountDownTimer(1000, 50) {
                    @Override
                    public void onTick(long l) {

                    }

                    @Override
                    public void onFinish() {
                        String nameEntered = editable.toString();
                        if(!nameEntered.equals(searchText)){ // if not the restored value
                            searchText=nameEntered;
                            if (nameEntered.length() > 0) {

                                SearchArtistsTask searchArtistsTask = new SearchArtistsTask();
                                searchArtistsTask.execute(nameEntered);
                            } else {
                                listAdapter.clear();
                            }
                        }

                    }
                };
                countDownTimer.start();
            }
        });


        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SEARCH_TEXT_KEY, searchText);
        outState.putParcelableArrayList(SEARCH_RESULT_KEY, mSSArtists);
    }

    private class MyAdapter extends ArrayAdapter<SSArtist>{
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
                viewHolder.imageView = (ImageView)convertView.findViewById(R.id.artistImageIV);
                viewHolder.textView = (TextView)convertView.findViewById(R.id.artistNameTV);
                convertView.setTag(viewHolder);
            }else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            //update values
            SSArtist artist = getItem(position);
            if(artist.getImageUrl()!=null){
                picasso.load(artist.getImageUrl()).into(viewHolder.imageView);
            }else{
                viewHolder.imageView.setImageResource(R.drawable.spotifyicon);
            }
            viewHolder.textView.setText(artist.getName());
            return convertView;
        }
    }

    private static class ViewHolder {
        ImageView imageView;
        TextView textView;
    }

    private SpotifyService sportifyService = new SpotifyApi().getService();

    private class SearchArtistsTask extends AsyncTask<String, Void, List<SSArtist>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showSearching();
            hideEmptyMsg();
        }

        @Override
        protected List<SSArtist> doInBackground(String... strings) {

            String artistName = strings[0];
            ArtistsPager results = sportifyService.searchArtists(artistName);

            List<Artist> artists = results.artists.items;
            List<SSArtist> ssArtists = new ArrayList<>();
            for (Artist artist : artists){
                String imageUrl=null;
                if(artist.images != null && artist.images.size()>0){
                    imageUrl = artist.images.get(artist.images.size()-1).url;
                }
                ssArtists.add(new SSArtist(artist.name, artist.id, imageUrl));
            }

            return ssArtists;
        }

        @Override
        protected void onPostExecute(List<SSArtist> artists) {
            super.onPostExecute(artists);
            hideSearching();
            mSSArtists.clear();
            mSSArtists.addAll(artists);
            setListWithNewValue();
        }
    }

    private void setListWithNewValue(){
        listAdapter.clear();
        for (SSArtist artist:mSSArtists){
            listAdapter.add(artist);
        }
        if(mSSArtists.size()==0){
            showEmptyMsg();
        }
    }





}
