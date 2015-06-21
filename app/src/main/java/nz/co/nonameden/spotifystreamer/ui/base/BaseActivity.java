package nz.co.nonameden.spotifystreamer.ui.base;

import android.content.ComponentName;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import butterknife.ButterKnife;
import nz.co.nonameden.spotifystreamer.media.MusicPlayerService;
import nz.co.nonameden.spotifystreamer.media.compat.MediaBrowserCompat;
import nz.co.nonameden.spotifystreamer.media.compat.MediaProvider;

/**
 * Created by nonameden on 6/06/15.
 */
public abstract class BaseActivity extends AppCompatActivity implements MediaProvider {

    private MediaBrowserCompat mMediaBrowser;
    private MediaControllerCompat mMediaController;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMediaBrowser = new MediaBrowserCompat(this,
                new ComponentName(this, MusicPlayerService.class), mConnectionCallback, null);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        ButterKnife.inject(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

//        mControlsFragment = (PlaybackControlsFragment) getFragmentManager()
//                .findFragmentById(R.id.fragment_playback_controls);
//        if (mControlsFragment == null) {
//            throw new IllegalStateException("Missing fragment with id 'controls'. Cannot continue.");
//        }
        hidePlaybackControls();

        mMediaBrowser.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mMediaController != null) {
            mMediaController.unregisterCallback(mMediaControllerCallback);
        }
        mMediaBrowser.disconnect();
    }

    @Override
    public MediaBrowserCompat getMediaBrowserCompat() {
        return mMediaBrowser;
    }

    @Override
    public MediaControllerCompat getMediaControllerCompat() {
        return mMediaController;
    }

    protected void onMediaControllerConnected() {
        // empty implementation, can be overridden by clients.
    }

    protected void showPlaybackControls() {
//        if (NetworkHelper.isOnline(this)) {
//            mControlsFragment.showPlaybackControls();
//        }
    }

    protected void hidePlaybackControls() {
//        mControlsFragment.hidePlaybackControls();
    }

    /**
     * Check if the MediaSession is active and in a "playback-able" state
     * (not NONE and not STOPPED).
     *
     * @return true if the MediaSession's state requires playback controls to be visible.
     */
    protected boolean shouldShowControls() {
        return !(mMediaController == null ||
                mMediaController.getMetadata() == null ||
                mMediaController.getPlaybackState() == null);
    }

    private void connectToSession(MediaSessionCompat.Token token) {
        try {
            mMediaController = new MediaControllerCompat(this, token);
            mMediaController.registerCallback(mMediaControllerCallback);

            if (shouldShowControls()) {
                showPlaybackControls();
            } else {
                hidePlaybackControls();
            }

//            if (mControlsFragment != null) {
//                mControlsFragment.onConnected();
//            }

            onMediaControllerConnected();

        } catch (RemoteException e) {
            // TODO: Ignore for now
        }
    }

    // Callback that ensures that we are showing the controls
    private final MediaControllerCompat.Callback mMediaControllerCallback =
            new MediaControllerCompat.Callback() {
                @Override
                public void onPlaybackStateChanged(PlaybackStateCompat state) {
                    if (shouldShowControls()) {
                        showPlaybackControls();
                    } else {
                        hidePlaybackControls();
                    }
                }

                @Override
                public void onMetadataChanged(MediaMetadataCompat metadata) {
                    if (shouldShowControls()) {
                        showPlaybackControls();
                    } else {
                        hidePlaybackControls();
                    }
                }
            };

    private MediaBrowserCompat.ConnectionCallback mConnectionCallback =
            new MediaBrowserCompat.ConnectionCallback() {
                @Override
                public void onConnected() {
                    MediaSessionCompat.Token token = mMediaBrowser.getSessionToken();
                    connectToSession(token);
                }
            };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
