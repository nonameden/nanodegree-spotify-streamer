package nz.co.nonameden.spotifystreamer.media;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nz.co.nonameden.spotifystreamer.infrastructure.models.ArtistViewModel;
import nz.co.nonameden.spotifystreamer.infrastructure.models.TrackViewModel;

/**
 * Created by nonameden on 13/06/15.
 */
public class QueueHelper {

    public static final String CUSTOM_METADATA_TRACK_SOURCE = "__SOURCE__";
    public static final String EXTRA_ARTIST = "extra-artist";
    public static final String EXTRA_TRACKS = "extra-tracks";

    public static ArrayList<TrackViewModel> getTracks(Bundle bundle) {
        return bundle.getParcelableArrayList(EXTRA_TRACKS);
    }

    public static ConverterState convertToQueue(Bundle bundle) {
        if(bundle.containsKey(EXTRA_ARTIST) && bundle.containsKey(EXTRA_TRACKS)) {
            ArtistViewModel artist = bundle.getParcelable(EXTRA_ARTIST);
            ArrayList<TrackViewModel> tracks = bundle.getParcelableArrayList(EXTRA_TRACKS);
            if(artist!=null && tracks!=null) {
                return convertToQueue(artist, tracks);
            }
        }
        return null;
    }

    public static ConverterState convertToQueue(@NonNull ArtistViewModel artist,
                                                                    @NonNull ArrayList<TrackViewModel> tracks) {
        Map<String, MediaMetadataCompat> metadataMap = new HashMap<>(tracks.size());
        List<MediaSessionCompat.QueueItem> itemList = new ArrayList<>(tracks.size());
        int count = 0;
        for(TrackViewModel track : tracks) {
            MediaMetadataCompat mediaMetadata = new MediaMetadataCompat.Builder()
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist.getName())
                    .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI, track.getSmallImageUrl())
                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, track.getBigImageUrl())
                    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, track.getId())
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, track.getName())
                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, track.getAlbumName())
                    .putString(CUSTOM_METADATA_TRACK_SOURCE, track.getPreviewUrl())
                    .build();
            metadataMap.put(track.getId(), mediaMetadata);
            itemList.add(new MediaSessionCompat.QueueItem(mediaMetadata.getDescription(), count++));
        }
        return new ConverterState(metadataMap, itemList);
    }

    public static int getMusicIndexOnQueue(List<MediaSessionCompat.QueueItem> queue, String mediaId) {
        int index = 0;
        for (MediaSessionCompat.QueueItem item : queue) {
            if (mediaId.equals(item.getDescription().getMediaId())) {
                return index;
            }
            index++;
        }
        return -1;
    }

    public static int getMusicIndexOnQueue(Iterable<MediaSessionCompat.QueueItem> queue,
                                           long queueId) {
        int index = 0;
        for (MediaSessionCompat.QueueItem item : queue) {
            if (queueId == item.getQueueId()) {
                return index;
            }
            index++;
        }
        return -1;
    }

    public static boolean isIndexPlayable(int index, List<MediaSessionCompat.QueueItem> queue) {
        return (queue != null && index >= 0 && index < queue.size());
    }

    public static Bundle createBundle(ArtistViewModel artist, ArrayList<TrackViewModel> tracks) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(EXTRA_ARTIST, artist);
        bundle.putParcelableArrayList(EXTRA_TRACKS, tracks);
        return bundle;
    }

    public static class ConverterState {
        private final Map<String, MediaMetadataCompat> mTracks;
        private final List<MediaSessionCompat.QueueItem> mQueueItems;

        public ConverterState(Map<String, MediaMetadataCompat> tracks, List<MediaSessionCompat.QueueItem> items) {
            mTracks = tracks;
            mQueueItems = items;
        }

        public Map<String, MediaMetadataCompat> getTracks() {
            return mTracks;
        }

        public List<MediaSessionCompat.QueueItem> getQueueItems() {
            return mQueueItems;
        }
    }
}
