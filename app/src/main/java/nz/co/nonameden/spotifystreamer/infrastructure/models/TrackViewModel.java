package nz.co.nonameden.spotifystreamer.infrastructure.models;

import android.os.Parcel;
import android.os.Parcelable;

import kaaes.spotify.webapi.android.models.AlbumSimple;
import kaaes.spotify.webapi.android.models.Track;
import nz.co.nonameden.spotifystreamer.infrastructure.shared.Constants;
import nz.co.nonameden.spotifystreamer.infrastructure.utils.SpotifyImageHelper;

/**
 * Created by nonameden on 6/06/15.
 */
public class TrackViewModel implements Parcelable {

    private final String mId;
    private final String mName;
    private final String mAlbumName;
    private final String mBigImageUrl;
    private final String mSmallImageUrl;
    private final String mPreviewUrl;
    private final String mSpotifyUrl;

    public TrackViewModel(Track track) {
        AlbumSimple album = track.album;

        mId = track.id;
        mName = track.name;
        mAlbumName = album.name;
        mBigImageUrl = SpotifyImageHelper.getBestImageUrl(album.images, Constants.COVER_IMAGE_SIZE);
        mSmallImageUrl = SpotifyImageHelper.getBestImageUrl(album.images, Constants.LIST_IMAGE_SIZE);
        mPreviewUrl = track.preview_url;
        mSpotifyUrl = track.external_urls.get("spotify");
    }

    private TrackViewModel(Parcel in) {
        mId = in.readString();
        mName = in.readString();
        mAlbumName = in.readString();
        mBigImageUrl = in.readString();
        mSmallImageUrl = in.readString();
        mPreviewUrl = in.readString();
        mSpotifyUrl = in.readString();
    }

    public static final Creator<TrackViewModel> CREATOR = new Creator<TrackViewModel>() {
        @Override
        public TrackViewModel createFromParcel(Parcel in) {
            return new TrackViewModel(in);
        }

        @Override
        public TrackViewModel[] newArray(int size) {
            return new TrackViewModel[size];
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
        dest.writeString(mAlbumName);
        dest.writeString(mBigImageUrl);
        dest.writeString(mSmallImageUrl);
        dest.writeString(mPreviewUrl);
        dest.writeString(mSpotifyUrl);
    }

    public String getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public String getAlbumName() {
        return mAlbumName;
    }

    public String getBigImageUrl() {
        return mBigImageUrl;
    }

    public String getSmallImageUrl() {
        return mSmallImageUrl;
    }

    public String getPreviewUrl() {
        return mPreviewUrl;
    }

    public String getSpotifyUrl() {
        return mSpotifyUrl;
    }
}
