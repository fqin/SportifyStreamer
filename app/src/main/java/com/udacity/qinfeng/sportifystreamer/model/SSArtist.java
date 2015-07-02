package com.udacity.qinfeng.sportifystreamer.model;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by fengqin on 15/7/2.
 */
public class SSArtist implements Parcelable {

    private static final String KEY_NAME="name";
    private static final String KEY_ID="id";
    private static final String KEY_IMAGE_URL="imageUrl";

    private String name;
    private String id;
    private String imageUrl;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public SSArtist(String name, String id, String imageUrl) {
        this.name = name;
        this.id = id;
        this.imageUrl = imageUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        Bundle bundle = new Bundle();
        bundle.putString(KEY_NAME, this.name);
        bundle.putString(KEY_ID, this.id);
        bundle.putString(KEY_IMAGE_URL,this.imageUrl);
        parcel.writeBundle(bundle);
    }

    public static final Parcelable.Creator<SSArtist> CREATOR = new Creator<SSArtist>() {
        @Override
        public SSArtist createFromParcel(Parcel parcel) {
            Bundle bundle = parcel.readBundle();

            return new SSArtist(bundle.getString(KEY_NAME),
                    bundle.getString(KEY_ID),
                    bundle.getString(KEY_IMAGE_URL));
        }

        @Override
        public SSArtist[] newArray(int i) {
            return new SSArtist[i];
        }
    };
}
