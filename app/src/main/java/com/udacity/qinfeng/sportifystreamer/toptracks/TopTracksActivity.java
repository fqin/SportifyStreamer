package com.udacity.qinfeng.sportifystreamer.toptracks;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.udacity.qinfeng.sportifystreamer.R;
import com.udacity.qinfeng.sportifystreamer.model.SSTrack;
import com.udacity.qinfeng.sportifystreamer.trackplay.TrackPlayActivity;
import com.udacity.qinfeng.sportifystreamer.trackplay.TrackPlayFragment;

import java.util.ArrayList;

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_top_tracks, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }else if(id == android.R.id.home){
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTrackSelected(String trackId,ArrayList<SSTrack> ssTracks) {

        Intent intent = new Intent(this, TrackPlayActivity.class);
        intent.putExtra(TrackPlayFragment.PARAM_SELECTED_TRACK_ID, trackId);
        intent.putExtra(TrackPlayFragment.TRACK_LIST, ssTracks);
        startActivity(intent);

    }


}
