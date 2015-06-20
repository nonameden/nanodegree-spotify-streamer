package nz.co.nonameden.spotifystreamer.infrastructure.utils;

import android.databinding.BindingAdapter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

/**
 * Created by nonameden on 20/06/15.
 */
public class BindingHelper {

    @BindingAdapter({"bind:image"})
    public static void loadImage(ImageView view, String url) {
        Glide.with(view.getContext())
                .load(url)
                .placeholder(new ColorDrawable(Color.GRAY))
                .into(view);
    }
}
