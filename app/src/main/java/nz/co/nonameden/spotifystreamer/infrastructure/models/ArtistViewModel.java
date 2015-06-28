package nz.co.nonameden.spotifystreamer.infrastructure.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import kaaes.spotify.webapi.android.models.Artist;
import nz.co.nonameden.spotifystreamer.infrastructure.shared.Constants;
import nz.co.nonameden.spotifystreamer.infrastructure.utils.SpotifyImageHelper;

/**
 * Created by nonameden on 6/06/15.
 */
public class ArtistViewModel implements Parcelable {

    private final String mId;
    private final String mName;
    private final String mImageUrl;

    public ArtistViewModel(Artist artist) {
        mId = artist.id;
        mName = artist.name;
        mImageUrl = SpotifyImageHelper.getBestImageUrl(artist.images, Constants.LIST_IMAGE_SIZE);
    }

    private ArtistViewModel(Parcel in) {
        mId = in.readString();
        mName = in.readString();
        mImageUrl = in.readString();
    }

    public static final Creator<ArtistViewModel> CREATOR = new Creator<ArtistViewModel>() {
        @Override
        public ArtistViewModel createFromParcel(Parcel in) {
            return new ArtistViewModel(in);
        }

        @Override
        public ArtistViewModel[] newArray(int size) {
            return new ArtistViewModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mId);
        dest.writeString(mName);
        dest.writeString(mImageUrl);
    }

    public String getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    @Override
    public boolean equals(@NonNull Object o) {
        if(o instanceof ArtistViewModel) {
            ArtistViewModel other = (ArtistViewModel) o;
            return mId.equals(other.mId);
        }
        return false;
    }
}
