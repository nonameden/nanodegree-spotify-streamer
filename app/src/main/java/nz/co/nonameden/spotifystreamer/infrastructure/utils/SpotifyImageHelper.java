package nz.co.nonameden.spotifystreamer.infrastructure.utils;

import java.util.List;

import kaaes.spotify.webapi.android.models.Image;

/**
 * Created by nonameden on 6/06/15.
 */
public final class SpotifyImageHelper {

    public static String getBestImageUrl(List<Image> images, int preferredSize) {
        Image resultImage = null;
        if(images != null) {
            // For spotify all images are going from bigger to smaller
            // We gonna compare only widths for now

            for (Image image : images) {
                if(resultImage == null) {
                    resultImage = image;
                    continue;
                }

                if (image.width < resultImage.width && image.width >= preferredSize) {
                    resultImage = image;
                } else {
                    break;
                }
            }
        }
        return resultImage == null ? null : resultImage.url;
    }
}
