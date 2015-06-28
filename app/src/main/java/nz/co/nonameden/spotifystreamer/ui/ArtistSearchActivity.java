package nz.co.nonameden.spotifystreamer.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;

import nz.co.nonameden.spotifystreamer.R;
import nz.co.nonameden.spotifystreamer.infrastructure.models.ArtistViewModel;
import nz.co.nonameden.spotifystreamer.infrastructure.models.TrackViewModel;
import nz.co.nonameden.spotifystreamer.ui.base.BaseActivity;

public class ArtistSearchActivity extends BaseActivity
        implements ArtistSearchFragment.Callback, TopTracksFragment.Callback {

    private static final String EXTRA_ARTIST = "extra-artist";

    private ArtistViewModel mArtist;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_search);

        if(savedInstanceState != null) {
            mArtist = savedInstanceState.getParcelable(EXTRA_ARTIST);
        }

        TopTracksFragment topTracksFragment = getTopTracksFragment();
        if(topTracksFragment != null) {
            topTracksFragment.setTabletMode(true);
            getSearchFragment().setTabletMode(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                onSettingsClicked();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(EXTRA_ARTIST, mArtist);
    }

    private void onSettingsClicked() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    @Override
    public void onArtistClicked(ArtistViewModel artist) {
        mArtist = artist;
        TopTracksFragment tracksFragment = getTopTracksFragment();
        if(tracksFragment == null) {
            Intent intent = new Intent(this, TopTracksActivity.class);
            intent.putExtra(TopTracksActivity.EXTRA_ARTIST, artist);
            startActivity(intent);
        } else {
            tracksFragment.setArtistId(artist.getId());
        }
    }

    private ArtistSearchFragment getSearchFragment() {
        return (ArtistSearchFragment) getFragmentManager().findFragmentById(R.id.spotify_search);
    }

    private TopTracksFragment getTopTracksFragment() {
        return (TopTracksFragment) getFragmentManager().findFragmentById(R.id.spotify_top_tracks);
    }

    @Override
    public void onTrackClicked(ArrayList<TrackViewModel> tracks, int position) {
        onPlayClicked(mArtist, tracks, position);
    }

    @Override
    protected void onMediaControllerConnected() {
        super.onMediaControllerConnected();

        PlayerFragment playerFragment = (PlayerFragment) getFragmentManager().findFragmentByTag(TAG_PLAYER);
        if(playerFragment!=null) {
            playerFragment.onConnected();
        }
    }
}
