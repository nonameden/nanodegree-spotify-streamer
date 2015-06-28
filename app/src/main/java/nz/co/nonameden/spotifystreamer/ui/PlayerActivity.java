package nz.co.nonameden.spotifystreamer.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;
import nz.co.nonameden.spotifystreamer.R;
import nz.co.nonameden.spotifystreamer.ui.base.BaseActivity;

/**
 * Created by nonameden on 12/06/15.
 */
public class PlayerActivity extends BaseActivity {

    public static final String EXTRA_CURRENT_MEDIA_METADATA = "extra-media-metadata";

    @InjectView(R.id.toolbar)
    Toolbar mToolbar;

    private PlayerFragment mPlayerFragment;

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
        Bundle extras = getIntent().getExtras();
        if(extras!=null && extras.containsKey(EXTRA_CURRENT_MEDIA_METADATA)) {
            MediaMetadataCompat metadata = extras.getParcelable(EXTRA_CURRENT_MEDIA_METADATA);
            mPlayerFragment.onMetadataChanged(metadata);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_share, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_share:
                onShareClicked();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected boolean isNowPlayingAvailable() {
        return false;
    }

    private void onShareClicked() {
        MediaControllerCompat controller = getMediaControllerCompat();
        if(controller != null) {
            MediaMetadataCompat metadata = controller.getMetadata();
            if(metadata != null) {
                // Unfortunately Media Compat implementation still contains bug, custom fields in
                // Metadata will be not passed back to UI through Session Compat =(
                // so lets us template for external url

                String artistName = metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST);
                String trackName = metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE);
                String mediaId = metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);
                String shareText = String.format(Locale.ENGLISH, "%1$s:%2$s\nhttps://open.spotify.com/track/%3$s",
                        artistName, trackName, mediaId);

                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/*");
                intent.putExtra(Intent.EXTRA_TEXT, shareText);
                startActivity(Intent.createChooser(intent, getString(R.string.title_share_with)));
            }
        }
    }

    @Override
    protected void onMediaControllerConnected() {
        super.onMediaControllerConnected();

        mPlayerFragment.onConnected();
    }
}
