package com.udacity.qinfeng.sportifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.udacity.qinfeng.sportifystreamer.model.SSTrack;
import com.udacity.qinfeng.sportifystreamer.toptracks.TopTracksActivity;
import com.udacity.qinfeng.sportifystreamer.toptracks.TopTracksFragment;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements SearchArtistFragment.OnFragmentInteractionListener, TopTracksFragment.OnFragmentInteractionListener{


    private View topTracksContainer;
    private String artistId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        topTracksContainer = findViewById(R.id.topTracksFrame);

    }


    @Override
    public void onArtistSelected(String artistId, String artistName) {
        if(topTracksContainer!=null){
            this.artistId = artistId;
            getSupportActionBar().setSubtitle(artistName);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.topTracksFrame, new TopTracksFragment()).commit();

        }else{

            Intent intent = new Intent(this, TopTracksActivity.class);
            intent.putExtra(TopTracksFragment.KEY_PARAM_ARTIST_ID, artistId);
            intent.putExtra(TopTracksActivity.KEY_PARAM_ARTIST_NAME, artistName);
            startActivity(intent);
        }
    }

    @Override
    public String getArtistId() {
        return this.artistId;
    }

    @Override
    public void onTrackSelected(String trackId, ArrayList<SSTrack> ssTracks) {

    }
}
