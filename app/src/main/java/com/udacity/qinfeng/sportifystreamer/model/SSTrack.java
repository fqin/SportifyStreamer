package com.udacity.qinfeng.sportifystreamer.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by fengqin on 15/7/2.
 */
public class SSTrack implements Parcelable {

    private static final String KEY_NAME="name";// track name
    private static final String KEY_ALBUM_NAME="album_name";
    private static final String KEY_IMAGE_URL="image_url"; //thumb url key

    private String name;
    private String albumName;
    private String imageUrl;

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

    public SSTrack(String name, String albumName, String imageUrl) {
        this.name = name;
        this.albumName = albumName;
        this.imageUrl = imageUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }
}
