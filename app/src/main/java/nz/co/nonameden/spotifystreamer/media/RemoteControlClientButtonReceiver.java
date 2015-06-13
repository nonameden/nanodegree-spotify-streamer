package nz.co.nonameden.spotifystreamer.media;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

import nz.co.nonameden.spotifystreamer.infrastructure.shared.Constants;

/**
 * Created by nonameden on 13/06/15.
 */
public class RemoteControlClientButtonReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Constants.ACTION_MEDIA_BUTTONS)) {
            final KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);

            if (event != null && event.getAction() == KeyEvent.ACTION_UP) {
                switch(event.getKeyCode()) {
                    case KeyEvent.KEYCODE_MEDIA_PAUSE:
                        onAction(context, MusicPlayerService.CMD_PAUSE);
                        break;
                    case KeyEvent.KEYCODE_MEDIA_PLAY:
                        onAction(context, MusicPlayerService.CMD_PLAY);
                        break;
                    case KeyEvent.KEYCODE_MEDIA_NEXT:
                        onAction(context, MusicPlayerService.CMD_NEXT);
                        break;
                    case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                        onAction(context, MusicPlayerService.CMD_PREV);
                        break;
                }
            }
        }
    }

    private void onAction(Context context, @MusicPlayerService.Commands String command) {
        Intent i = new Intent(context, MusicPlayerService.class);
        i.setAction(MusicPlayerService.ACTION_CMD);
        i.putExtra(MusicPlayerService.CMD_NAME, command);
        context.startService(i);
    }
}
