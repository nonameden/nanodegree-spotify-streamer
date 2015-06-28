package nz.co.nonameden.spotifystreamer.infrastructure.utils;

/**
 * Created by nonameden on 20/06/15.
 */
public final class MediaUtils {

    public static String formatMillis(int timeMs) {
        int seconds = timeMs / 1000;
        int hours = seconds / 3600;
        seconds %= 3600;
        int minutes = seconds / 60;
        seconds %= 60;
        String time;
        if(hours > 0) {
            time = String.format("%d:%02d:%02d", hours, minutes, seconds);
        } else {
            time = String.format("%d:%02d", minutes, seconds);
        }

        return time;
    }
}
