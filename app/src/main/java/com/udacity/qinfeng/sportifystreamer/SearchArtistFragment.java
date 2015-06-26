package com.udacity.qinfeng.sportifystreamer;


import android.app.Service;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Image;


/**
 * A simple {@link Fragment} subclass.
 */
public class SearchArtistFragment extends Fragment {

    private EditText editText;
    private ListView listView;
    private MyAdapter listAdapter;
    private CountDownTimer countDownTimer;


    public SearchArtistFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search_artist, container, false);

        editText = (EditText) view.findViewById(R.id.searchArtistET);
        listView = (ListView) view.findViewById(R.id.artistListLV);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(final Editable editable) {

                    if(countDownTimer!=null){
                        countDownTimer.cancel();
                    }
                    countDownTimer = new CountDownTimer(1000,50) {
                        @Override
                        public void onTick(long l) {

                        }

                        @Override
                        public void onFinish() {
                            String nameEntered = editable.toString();

                            if(nameEntered.length()>0){

                                SearchArtistsTask searchArtistsTask = new SearchArtistsTask();
                                searchArtistsTask.execute(nameEntered);
                            }else{
                                listAdapter.clear();
                            }
                        }
                    };
                    countDownTimer.start();
                }






                //comment attacher une liste avec donnée renvoyé par un requete rest?




        });

        listAdapter = new MyAdapter(getActivity(),R.layout.artist_item);
        listView.setAdapter(listAdapter);


        return view;
    }

    class MyAdapter extends ArrayAdapter<Artist>{
        private int ressource;

        public MyAdapter(Context context, int resource) {
            super(context, resource);
            this.ressource = resource;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(Service.LAYOUT_INFLATER_SERVICE);

            View view = inflater.inflate(this.ressource, parent, false);

            Artist artist = getItem(position);


            List<Image> images = artist.images;
            if(images!=null && images.size()>0){
                ImageView imageView = (ImageView)view.findViewById(R.id.artistImageIV);
                Picasso.with(getContext()).load(images.get(images.size()-1).url).into(imageView);
            }


            TextView textView = (TextView)view.findViewById(R.id.artistNameTV);
            textView.setText(artist.name);


            return view;
        }
    }

    public class SearchArtistsTask extends AsyncTask<String, Void, List<Artist>> {
        @Override
        protected List<Artist> doInBackground(String... strings) {

            String artistName = strings[0];

            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();
            ArtistsPager results = spotify.searchArtists(artistName);
            return results.artists.items;
        }

        @Override
        protected void onPostExecute(List<Artist> artists) {
            super.onPostExecute(artists);
            listAdapter.clear();
            for(Artist artist:artists){
                listAdapter.add(artist);
            }
         //   listView.invalidate();
        }
    }



}
