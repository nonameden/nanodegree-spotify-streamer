package nz.co.nonameden.spotifystreamer.ui;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import nz.co.nonameden.spotifystreamer.R;
import nz.co.nonameden.spotifystreamer.ui.base.BaseActivity;


/**
 * Created by nonameden on 6/06/15.
 */
public class SpotifySettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.spotify_settings);
    }

    public static class SettingsFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.spotify_settings);
        }
    }
}
