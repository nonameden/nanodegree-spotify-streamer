package nz.co.nonameden.spotifystreamer.infrastructure.models;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import nz.co.nonameden.spotifystreamer.BR;
import nz.co.nonameden.spotifystreamer.infrastructure.utils.MediaUtils;

/**
 * Created by nonameden on 20/06/15.
 */
public class NowPlayingViewModel extends BaseObservable implements Parcelable {

    private String mArtistName;
    private String mTrackName;
    private int mCurrentTime;
    private int mTotalTime;
    private String mAlbumArtUrl;
    private PlaybackStateCompat mPlaybackState;

    public NowPlayingViewModel() {
    }

    protected NowPlayingViewModel(Parcel in) {
        mArtistName = in.readString();
        mTrackName = in.readString();
        mCurrentTime = in.readInt();
        mTotalTime = in.readInt();
        mAlbumArtUrl = in.readString();
        mPlaybackState = in.readParcelable(PlaybackStateCompat.class.getClassLoader());
    }

    public static final Creator<NowPlayingViewModel> CREATOR = new Creator<NowPlayingViewModel>() {
        @Override
        public NowPlayingViewModel createFromParcel(Parcel in) {
            return new NowPlayingViewModel(in);
        }

        @Override
        public NowPlayingViewModel[] newArray(int size) {
            return new NowPlayingViewModel[size];
        }
    };

    @Bindable
    public String getArtistName() {
        return mArtistName;
    }

    @Bindable
    public String getTrackName() {
        return mTrackName;
    }

    @Bindable
    public String getCurrentTime() {
        return MediaUtils.formatMillis(mCurrentTime);
    }

    @Bindable
    public int getCurrentTimeMs() {
        return mCurrentTime;
    }

    @Bindable
    public String getTotalTime() {
        return MediaUtils.formatMillis(mTotalTime);
    }

    @Bindable
    public int getTotalTimeMs() {
        return mTotalTime;
    }

    @Bindable
    public String getAlbumArtUrl() {
        return mAlbumArtUrl;
    }

    public PlaybackStateCompat getPlaybackState() {
        return mPlaybackState;
    }

    public void setPlaybackState(PlaybackStateCompat playbackState) {
        mPlaybackState = playbackState;
        notifyPropertyChanged(BR.skipNextVisible);
        notifyPropertyChanged(BR.playing);
        notifyPropertyChanged(BR.skipPreviousVisible);
        notifyPropertyChanged(BR.loadingInProgress);
    }

    public void updateFromMetadata(@NonNull MediaMetadataCompat metadata) {
        mArtistName = metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST);
        mTrackName = metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE);
        mAlbumArtUrl = metadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI);
        mTotalTime = (int) metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
        notifyChange();
    }

    public void setCurrentTime(int currentTime) {
        mCurrentTime = currentTime;
        notifyPropertyChanged(BR.currentTime);
        notifyPropertyChanged(BR.currentTimeMs);
    }

    @Bindable
    public boolean isPlaying() {
        return mPlaybackState != null && mPlaybackState.getState() == PlaybackStateCompat.STATE_PLAYING;
    }

    @Bindable
    public boolean isSkipNextVisible() {
        return mPlaybackState != null &&
                !((mPlaybackState.getActions() & PlaybackStateCompat.ACTION_SKIP_TO_NEXT) == 0);
    }

    @Bindable
    public boolean isSkipPreviousVisible() {
        return mPlaybackState != null &&
                !((mPlaybackState.getActions() & PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS) == 0);
    }

    @Bindable
    public boolean isLoadingInProgress() {
        return mPlaybackState == null || (mPlaybackState.getState() >= PlaybackStateCompat.STATE_BUFFERING);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mArtistName);
        dest.writeString(mTrackName);
        dest.writeInt(mCurrentTime);
        dest.writeInt(mTotalTime);
        dest.writeString(mAlbumArtUrl);
        dest.writeParcelable(mPlaybackState, flags);
    }
}
