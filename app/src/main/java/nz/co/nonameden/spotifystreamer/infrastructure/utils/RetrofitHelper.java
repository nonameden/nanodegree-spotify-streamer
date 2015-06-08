package nz.co.nonameden.spotifystreamer.infrastructure.utils;

import android.content.Context;
import android.support.annotation.NonNull;

import nz.co.nonameden.spotifystreamer.R;
import retrofit.RetrofitError;

/**
 * Created by nonameden on 6/06/15.
 */
public final class RetrofitHelper {

    public static String getErrorText(@NonNull Context context, @NonNull RetrofitError error) {
        switch (error.getKind()) {
            case NETWORK:
                return context.getString(R.string.no_network_error);
            default:
                return context.getString(R.string.unknown_error);
        }
    }
}
