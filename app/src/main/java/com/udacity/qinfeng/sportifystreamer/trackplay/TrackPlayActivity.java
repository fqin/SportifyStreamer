package com.udacity.qinfeng.sportifystreamer.trackplay;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.udacity.qinfeng.sportifystreamer.R;
import com.udacity.qinfeng.sportifystreamer.model.SSTrack;

import java.util.ArrayList;

public class TrackPlayActivity extends AppCompatActivity implements TrackPlayFragment.OnFragmentInteractionListener {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_play);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {

                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .add(R.id.playTrackFragment, new TrackPlayFragment())
                        .commit();
            }

    }

    @Override
    public String getSelectedTrackId() {
        return getIntent().getStringExtra(TrackPlayFragment.PARAM_SELECTED_TRACK_ID);
    }

    @Override
    public ArrayList<SSTrack> getTrackList() {
        return getIntent().getParcelableArrayListExtra(TrackPlayFragment.TRACK_LIST);
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
}
