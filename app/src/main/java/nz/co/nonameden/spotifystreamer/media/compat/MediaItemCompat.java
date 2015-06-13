package nz.co.nonameden.spotifystreamer.media.compat;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.text.TextUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by nonameden on 13/06/15.
 */
public class MediaItemCompat implements Parcelable {

    // I have just modified native sources to work on all platforms

    private final int mFlags;
    private final MediaDescriptionCompat mDescription;

    /**
     * @hide
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef(flag = true, value = {FLAG_BROWSABLE, FLAG_PLAYABLE})
    public @interface Flags {
    }

    /**
     * Flag: Indicates that the item has children of its own.
     */
    public static final int FLAG_BROWSABLE = 1;

    /**
     * Flag: Indicates that the item is playable.
     * <p>
     * The id of this item may be passed to
     * {@link MediaControllerCompat.TransportControls#playFromMediaId(String, Bundle)}
     * to start playing it.
     * </p>
     */
    public static final int FLAG_PLAYABLE = 1 << 1;

    /**
     * Create a new MediaItem for use in browsing media.
     *
     * @param description The description of the media, which must include a
     *                    media id.
     * @param flags       The flags for this item.
     */
    public MediaItemCompat(@NonNull MediaDescriptionCompat description, @Flags int flags) {
        if (TextUtils.isEmpty(description.getMediaId())) {
            throw new IllegalArgumentException("description must have a non-empty media id");
        }
        mFlags = flags;
        mDescription = description;
    }

    /**
     * Private constructor.
     */
    private MediaItemCompat(Parcel in) {
        mFlags = in.readInt();
        mDescription = MediaDescriptionCompat.CREATOR.createFromParcel(in);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(mFlags);
        mDescription.writeToParcel(out, flags);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MediaItem{");
        sb.append("mFlags=").append(mFlags);
        sb.append(", mDescription=").append(mDescription);
        sb.append('}');
        return sb.toString();
    }

    public static final Creator<MediaItemCompat> CREATOR =
            new Parcelable.Creator<MediaItemCompat>() {
                @Override
                public MediaItemCompat createFromParcel(Parcel in) {
                    return new MediaItemCompat(in);
                }

                @Override
                public MediaItemCompat[] newArray(int size) {
                    return new MediaItemCompat[size];
                }
            };

    /**
     * Gets the flags of the item.
     */
    public
    @Flags
    int getFlags() {
        return mFlags;
    }

    /**
     * Returns whether this item is browsable.
     *
     * @see #FLAG_BROWSABLE
     */
    public boolean isBrowsable() {
        return (mFlags & FLAG_BROWSABLE) != 0;
    }

    /**
     * Returns whether this item is playable.
     *
     * @see #FLAG_PLAYABLE
     */
    public boolean isPlayable() {
        return (mFlags & FLAG_PLAYABLE) != 0;
    }

    /**
     * Returns the description of the media.
     */
    public
    @NonNull
    MediaDescriptionCompat getDescription() {
        return mDescription;
    }

    /**
     * Returns the media id for this item.
     */
    public
    @NonNull
    String getMediaId() {
        return mDescription.getMediaId();
    }
}