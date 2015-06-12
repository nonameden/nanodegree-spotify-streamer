package nz.co.nonameden.spotifystreamer.ui;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.IOException;
import java.util.ArrayList;

import nz.co.nonameden.spotifystreamer.R;
import nz.co.nonameden.spotifystreamer.infrastructure.models.ArtistViewModel;
import nz.co.nonameden.spotifystreamer.infrastructure.models.TrackViewModel;
import nz.co.nonameden.spotifystreamer.ui.base.BaseFragment;

/**
 * Created by nonameden on 13/06/15.
 */
public class PlayerFragment extends BaseFragment<Void> implements MediaPlayer.OnPreparedListener {

    private static final String TAG = "PlayerFragment";

    public static final String ARG_ARTIST = "arg-artist";
    public static final String ARG_TRACKS = "arg-tracks";
    public static final String ARG_CURRENT_TRACK = "arg-current_track";

    private ArtistViewModel mArtist;
    private ArrayList<TrackViewModel> mTracks;
    private int mCurrentPosition;
    private MediaPlayer mMediaPlayer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = getArguments();
        if(arguments != null) {
            mArtist = arguments.getParcelable(ARG_ARTIST);
            mTracks = arguments.getParcelableArrayList(ARG_TRACKS);
            mCurrentPosition = arguments.getInt(ARG_CURRENT_TRACK);
        }

        assert mArtist!=null && mTracks!=null : "Missed required arguments";

        if(savedInstanceState != null) {
            mCurrentPosition = savedInstanceState.getInt(ARG_CURRENT_TRACK, mCurrentPosition);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        initMediaPlayer();
        return inflater.inflate(R.layout.fragment_player, container, false);
    }

    private void initMediaPlayer() {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setWakeMode(getActivity(), PowerManager.PARTIAL_WAKE_LOCK);
        mMediaPlayer.setOnPreparedListener(this);
    }

    private void setMediaPlayerSource(String source) {
        try {
            mMediaPlayer.setDataSource(getActivity(), Uri.parse(source));
            mMediaPlayer.prepareAsync();
        } catch (IOException e) {
            Log.e(TAG, "Can not set data source for media player", e);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(ARG_CURRENT_TRACK, mCurrentPosition);
    }

    @Override
    protected Void initStubCallback() {
        return null;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
    }
}
