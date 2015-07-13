package com.udacity.qinfeng.sportifystreamer.model;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by fengqin on 15/7/2.
 */
public class SSTrack implements Parcelable {

    private static final String KEY_NAME="name";// track name
    private static final String KEY_ALBUM_NAME="album_name";
    private static final String KEY_IMAGE_URL="image_url"; //thumb url key
    private static final String KEY_ID="track_id";
    private static final String KEY_ARTIST_NAME="artist";
    private static final String KEY_ARTWORK_URL="artwork_url";
    private static final String KEY_DURATION_IN_MS="durationInMs";


    private String name;
    private String albumName;
    private String imageUrl;
    private String id;
    private String artistName;
    private String artworkUrl;
    private long durationInMs; //duration in milli-secondes

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getArtworkUrl() {
        return artworkUrl;
    }

    public void setArtworkUrl(String artworkUrl) {
        this.artworkUrl = artworkUrl;
    }

    public long getDurationInMs() {
        return durationInMs;
    }

    public void setDurationInMs(int durationInMs) {
        this.durationInMs = durationInMs;
    }

    public SSTrack(String name, String albumName, String imageUrl, String id, String artistName,
                   String artworkUrl, long durationInMs) {
        this.name = name;
        this.albumName = albumName;
        this.imageUrl = imageUrl;
        this.id=id;
        this.artistName = artistName;
        this.artworkUrl = artworkUrl;
        this.durationInMs = durationInMs;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        Bundle bundle = new Bundle();
        bundle.putString(KEY_NAME, this.name);
        bundle.putString(KEY_ALBUM_NAME, this.albumName);
        bundle.putString(KEY_IMAGE_URL,this.imageUrl);
        bundle.putString(KEY_ID,this.id);
        bundle.putString(KEY_ARTIST_NAME,this.artistName);
        bundle.putString(KEY_ARTWORK_URL,this.artworkUrl);
        bundle.putLong(KEY_DURATION_IN_MS, this.durationInMs);
        dest.writeBundle(bundle);

    }

    public static final Parcelable.Creator<SSTrack> CREATOR = new Creator<SSTrack>() {
        @Override
        public SSTrack createFromParcel(Parcel parcel) {
            Bundle bundle = parcel.readBundle();

            return new SSTrack(bundle.getString(KEY_NAME),
                    bundle.getString(KEY_ALBUM_NAME),
                    bundle.getString(KEY_IMAGE_URL),
                    bundle.getString(KEY_ID),
                    bundle.getString(KEY_ARTIST_NAME),
                    bundle.getString(KEY_ARTWORK_URL),
                    bundle.getInt(KEY_DURATION_IN_MS));
        }

        @Override
        public SSTrack[] newArray(int i) {
            return new SSTrack[i];
        }
    };
}
