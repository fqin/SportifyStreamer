package com.udacity.qinfeng.sportifystreamer;

import android.os.AsyncTask;

import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;

/**
 * Created by fengqin on 15/6/19.
 */
public class SearchArtistsTask extends AsyncTask<String, Void, List<Artist>> {
    @Override
    protected List<Artist> doInBackground(String... strings) {

        String artistName = strings[0];

        SpotifyApi api = new SpotifyApi();
        SpotifyService spotify = api.getService();
        ArtistsPager results = spotify.searchArtists(artistName);
        return results.artists.items;
    }
}
