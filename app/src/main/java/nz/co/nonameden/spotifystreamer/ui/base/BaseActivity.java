package nz.co.nonameden.spotifystreamer.ui.base;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;

import butterknife.ButterKnife;
import nz.co.nonameden.spotifystreamer.R;
import nz.co.nonameden.spotifystreamer.infrastructure.models.ArtistViewModel;
import nz.co.nonameden.spotifystreamer.infrastructure.models.TrackViewModel;
import nz.co.nonameden.spotifystreamer.media.MusicPlayerService;
import nz.co.nonameden.spotifystreamer.media.QueueHelper;
import nz.co.nonameden.spotifystreamer.media.compat.MediaBrowserCompat;
import nz.co.nonameden.spotifystreamer.media.compat.MediaProvider;
import nz.co.nonameden.spotifystreamer.ui.PlayerActivity;
import nz.co.nonameden.spotifystreamer.ui.PlayerFragment;

/**
 * Created by nonameden on 6/06/15.
 */
public abstract class BaseActivity extends AppCompatActivity implements MediaProvider {

    protected static final String TAG_PLAYER = "player";

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

        mMediaBrowser.connect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(shouldShowNowPlaying() && isNowPlayingAvailable()) {
            getMenuInflater().inflate(R.menu.menu_now_playing, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    protected boolean isNowPlayingAvailable() {
        return true;
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

    protected boolean shouldShowNowPlaying() {
        return !(mMediaController == null ||
                mMediaController.getMetadata() == null ||
                mMediaController.getPlaybackState() == null);
    }

    private void connectToSession(MediaSessionCompat.Token token) {
        try {
            mMediaController = new MediaControllerCompat(this, token);
            mMediaController.registerCallback(mMediaControllerCallback);

            invalidateOptionsMenu();

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
                    invalidateOptionsMenu();
                }

                @Override
                public void onMetadataChanged(MediaMetadataCompat metadata) {
                    invalidateOptionsMenu();
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
            case R.id.action_now_playing:
                onNowPlayingClicked();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void onPlayClicked(ArtistViewModel artist, ArrayList<TrackViewModel> tracks, int position) {
        Bundle bundle = QueueHelper.createBundle(artist, tracks);
        TrackViewModel track = tracks.get(position);
        getMediaControllerCompat().getTransportControls().playFromMediaId(track.getId(), bundle);

        onNowPlayingClicked();
    }

    protected void onNowPlayingClicked() {
        boolean isTablet = getResources().getBoolean(R.bool.is_tablet);
        if(isTablet) {
            // To avoid double tap
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            Fragment playerFragment = fragmentManager.findFragmentByTag(TAG_PLAYER);
            if(playerFragment!=null) {
                transaction.remove(playerFragment);
            }
            new PlayerFragment().show(transaction, TAG_PLAYER);
        } else {
            Intent intent = new Intent(this, PlayerActivity.class);
            startActivity(intent);
        }
    }
}
