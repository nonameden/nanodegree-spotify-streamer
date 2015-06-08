package nz.co.nonameden.spotifystreamer.infrastructure.utils;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

/**
 * Created by nonameden on 6/06/15.
 */
public final class UiUtils {

    public static void crossfadeViews(View showView, View hideView, boolean animate) {
        Context context = showView.getContext();

        if (animate) {
            hideView.startAnimation(AnimationUtils.loadAnimation(
                    context, android.R.anim.fade_out));

            final Animation fadeInAnimation = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
            showView.startAnimation(fadeInAnimation);

        } else {
            hideView.clearAnimation();
            showView.clearAnimation();
        }
        hideView.setVisibility(View.GONE);
        showView.setVisibility(View.VISIBLE);
    }
}
