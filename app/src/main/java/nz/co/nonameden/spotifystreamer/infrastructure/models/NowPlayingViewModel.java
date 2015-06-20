package nz.co.nonameden.spotifystreamer.infrastructure.models;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import nz.co.nonameden.spotifystreamer.BR;
import nz.co.nonameden.spotifystreamer.infrastructure.utils.MediaUtils;

/**
 * Created by nonameden on 20/06/15.
 */
public class NowPlayingViewModel extends BaseObservable {

    private String mArtistName;
    private String mTrackName;
    private int mCurrentTime;
    private int mTotalTime;
    private String mAlbumArtUrl;
    private PlaybackStateCompat mPlaybackState;

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

    public void updateFromMetadata(MediaMetadataCompat metadata) {
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
}
