package nz.co.nonameden.spotifystreamer.ui;

import android.os.Bundle;
import android.support.v7.app.ActionBar;

import nz.co.nonameden.spotifystreamer.R;
import nz.co.nonameden.spotifystreamer.ui.base.BaseActivity;

/**
 * Created by nonameden on 12/06/15.
 */
public class PlayerActivity extends BaseActivity {

    public static final String EXTRA_ARTIST = PlayerFragment.ARG_ARTIST;
    public static final String EXTRA_TRACKS = PlayerFragment.ARG_TRACKS;
    public static final String EXTRA_CURRENT_TRACK = PlayerFragment.ARG_CURRENT_TRACK;
    public static final String EXTRA_CURRENT_MEDIA_DESCRIPTION = "extra-media-description";

    private PlayerFragment mPlayerFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if(savedInstanceState == null) {
            mPlayerFragment = new PlayerFragment();
            mPlayerFragment.setArguments(getIntent().getExtras());
            getFragmentManager().beginTransaction()
                    .add(R.id.content, mPlayerFragment)
                    .commit();
        } else {
            mPlayerFragment = (PlayerFragment) getFragmentManager().findFragmentById(R.id.content);
        }
    }
}
