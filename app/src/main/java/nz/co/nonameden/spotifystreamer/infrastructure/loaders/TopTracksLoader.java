package nz.co.nonameden.spotifystreamer.infrastructure.loaders;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import nz.co.nonameden.spotifystreamer.infrastructure.models.TrackViewModel;

/**
 * Created by nonameden on 6/06/15.
 */
public class TopTracksLoader extends AbsNetworkLoader<List<TrackViewModel>, Tracks> {

    private final String mArtistId;

    public TopTracksLoader(Context context, String artistId, @NonNull ErrorCallback errorCallback) {
        super(context, errorCallback);

        mArtistId = artistId;
    }

    @Override
    protected void executeNetworkRequest() {
        mSpotifyService.getArtistTopTrack(mArtistId, getAdditionalParammeters(), this);
    }

    @NonNull
    @Override
    protected List<TrackViewModel> convert(Tracks data) {
        List<TrackViewModel> models = new ArrayList<>();
        if(data != null) {
            for(Track track : data.tracks) {
                models.add(new TrackViewModel(track));
            }
        }
        return models;
    }
}
