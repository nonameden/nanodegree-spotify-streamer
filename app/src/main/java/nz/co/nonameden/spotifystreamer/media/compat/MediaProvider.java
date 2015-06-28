package nz.co.nonameden.spotifystreamer.media.compat;

import android.support.v4.media.session.MediaControllerCompat;

/**
 * Created by nonameden on 13/06/15.
 */
public interface MediaProvider {
    MediaBrowserCompat getMediaBrowserCompat();
    MediaControllerCompat getMediaControllerCompat();
}
