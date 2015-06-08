package nz.co.nonameden.spotifystreamer.ui;

import android.os.Bundle;
import android.support.v7.app.ActionBar;

import nz.co.nonameden.spotifystreamer.R;
import nz.co.nonameden.spotifystreamer.infrastructure.models.ArtistViewModel;
import nz.co.nonameden.spotifystreamer.infrastructure.models.TrackViewModel;
import nz.co.nonameden.spotifystreamer.ui.base.BaseActivity;


/**
 * Created by nonameden on 6/06/15.
 */
public class SpotifyTopTracksActivity extends BaseActivity
        implements SpotifyTopTracksFragment.Callback {

    public static final String EXTRA_ARTIST = "extra-artist";
    private ArtistViewModel mArtist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Just because in feature stage we gonna support tablet with 2-pane
        // we gonna pass artist directly

        setContentView(R.layout.activity_spotify_top_tracks);

        mArtist = getIntent().getParcelableExtra(EXTRA_ARTIST);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null) {
            actionBar.setSubtitle(mArtist.getName());
        }

        SpotifyTopTracksFragment fragment = (SpotifyTopTracksFragment) getFragmentManager()
                .findFragmentById(R.id.spotify_top_tracks);
        fragment.setArtistId(mArtist.getId());
    }

    @Override
    public void onTrackClicked(TrackViewModel track) {

    }
}
