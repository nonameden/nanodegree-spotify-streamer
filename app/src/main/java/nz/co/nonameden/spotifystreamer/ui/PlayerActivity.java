package nz.co.nonameden.spotifystreamer.ui;

import android.os.Bundle;
import android.support.v7.app.ActionBar;

import nz.co.nonameden.spotifystreamer.R;
import nz.co.nonameden.spotifystreamer.ui.base.BaseActivity;

/**
 * Created by nonameden on 12/06/15.
 */
public class PlayerActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }
}
