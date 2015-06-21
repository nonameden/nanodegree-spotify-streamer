package nz.co.nonameden.spotifystreamer.ui;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.view.Menu;

import butterknife.ButterKnife;
import butterknife.InjectView;
import nz.co.nonameden.spotifystreamer.R;
import nz.co.nonameden.spotifystreamer.ui.base.BaseActivity;

/**
 * Created by nonameden on 12/06/15.
 */
public class PlayerActivity extends BaseActivity {

    public static final String EXTRA_CURRENT_MEDIA_DESCRIPTION = "extra-media-description";

    @InjectView(R.id.toolbar)
    Toolbar mToolbar;

    private PlayerFragment mPlayerFragment;
    private ShareActionProvider mShareActionProvider;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        ButterKnife.inject(this);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        setSupportActionBar(mToolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        mPlayerFragment = (PlayerFragment) getFragmentManager().findFragmentById(R.id.player);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_share, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onMediaControllerConnected() {
        super.onMediaControllerConnected();

        mPlayerFragment.onConnected();
    }
}
