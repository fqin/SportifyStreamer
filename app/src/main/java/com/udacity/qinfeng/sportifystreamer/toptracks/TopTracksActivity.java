package com.udacity.qinfeng.sportifystreamer.toptracks;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.udacity.qinfeng.sportifystreamer.R;

public class TopTracksActivity extends AppCompatActivity implements TopTracksFragment.OnFragmentInteractionListener{

    public static final String KEY_PARAM_ARTIST_NAME = "artist_name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_tracks);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setSubtitle(getIntent().getStringExtra(KEY_PARAM_ARTIST_NAME));
    }

    @Override
    public String getArtistId() {
        return getIntent().getStringExtra(TopTracksFragment.KEY_PARAM_ARTIST_ID);
    }


}
