package nz.co.nonameden.spotifystreamer.infrastructure.loaders;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import nz.co.nonameden.spotifystreamer.infrastructure.models.ArtistViewModel;

/**
 * Created by nonameden on 6/06/15.
 */
public class ArtistSearchLoader extends AbsNetworkLoader<List<ArtistViewModel>, ArtistsPager> {

    private final String mSearchQuery;

    public ArtistSearchLoader(Context context, String searchQuery, @NonNull ErrorCallback errorCallback) {
        super(context, errorCallback);

        mSearchQuery = searchQuery;
    }

    @Override
    protected void executeNetworkRequest() {
        mSpotifyService.searchArtists(mSearchQuery, getAdditionalParammeters(), this);
    }

    @NonNull
    @Override
    protected List<ArtistViewModel> convert(ArtistsPager data) {
        List<ArtistViewModel> models = new ArrayList<>();
        if(data != null) {
            for(Artist artist : data.artists.items) {
                models.add(new ArtistViewModel(artist));
            }
        }
        return models;
    }
}
