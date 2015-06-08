package nz.co.nonameden.spotifystreamer.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import nz.co.nonameden.spotifystreamer.R;
import nz.co.nonameden.spotifystreamer.infrastructure.models.ArtistViewModel;
import nz.co.nonameden.spotifystreamer.ui.base.BaseActivity;

public class SpotifySearchActivity extends BaseActivity
        implements SpotifySearchFragment.SpotifySearchCallback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spotify_search);
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

    private void onSettingsClicked() {
        Intent intent = new Intent(this, SpotifySettingsActivity.class);
        startActivity(intent);
    }

    @Override
    public void onArtistClicked(ArtistViewModel artist) {
        SpotifyTopTracksFragment tracksFragment = getTopTracksFragment();
        if(tracksFragment == null) {
            Intent intent = new Intent(this, SpotifyTopTracksActivity.class);
            intent.putExtra(SpotifyTopTracksActivity.EXTRA_ARTIST, artist);
            startActivity(intent);
        } else {
            tracksFragment.setArtistId(artist.getId());
        }
    }

    private SpotifyTopTracksFragment getTopTracksFragment() {
        return (SpotifyTopTracksFragment) getFragmentManager().findFragmentById(R.id.spotify_top_tracks);
    }
}
