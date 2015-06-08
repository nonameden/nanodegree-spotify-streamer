package nz.co.nonameden.spotifystreamer.ui;

import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import nz.co.nonameden.spotifystreamer.R;
import nz.co.nonameden.spotifystreamer.infrastructure.adapters.TrackListAdapter;
import nz.co.nonameden.spotifystreamer.infrastructure.loaders.AbsNetworkLoader;
import nz.co.nonameden.spotifystreamer.infrastructure.loaders.TopTracksLoader;
import nz.co.nonameden.spotifystreamer.infrastructure.models.TrackViewModel;
import nz.co.nonameden.spotifystreamer.infrastructure.utils.RetrofitHelper;
import nz.co.nonameden.spotifystreamer.infrastructure.utils.UiUtils;
import nz.co.nonameden.spotifystreamer.ui.base.BaseFragment;
import retrofit.RetrofitError;

/**
 * Created by nonameden on 6/06/15.
 */
public class SpotifyTopTracksFragment extends BaseFragment<SpotifyTopTracksFragment.Callback>
        implements LoaderManager.LoaderCallbacks<List<TrackViewModel>>,AbsNetworkLoader.ErrorCallback {

    private static final String ARG_ARTIST_ID = "arg-artist-id";
    private static final String ARG_TRACKS = "arg-tracks";

    private static final int LOADER_TOP_TRACKS = 200;

    private String mArtistId;
    private TrackListAdapter mAdapter;
    private ListView mListView;
    private View mEmptyView;
    private View mProgressView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdapter = new TrackListAdapter();
        if(savedInstanceState!=null) {
            mArtistId = savedInstanceState.getString(ARG_ARTIST_ID);
            ArrayList<TrackViewModel> tracks = savedInstanceState.getParcelableArrayList(ARG_TRACKS);
            mAdapter.setItems(tracks);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_spotify_top_songs, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mEmptyView = view.findViewById(R.id.empty_view);
        mEmptyView.setVisibility(View.GONE);

        mListView = (ListView) view.findViewById(R.id.list);
        mListView.setAdapter(mAdapter);
        mProgressView = view.findViewById(R.id.progress);

        if(mAdapter.getCount() == 0) {
            UiUtils.crossfadeViews(mProgressView, mListView, false);
        } else {
            UiUtils.crossfadeViews(mListView, mProgressView, false);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ARG_ARTIST_ID, mArtistId);
        outState.putParcelableArrayList(ARG_TRACKS,
                (ArrayList<TrackViewModel>) mAdapter.getItems().clone()
        );
    }

    @Override
    protected Callback initStubCallback() {
        return new Callback() {
            @Override
            public void onTrackClicked(TrackViewModel track) {}
        };
    }

    public void setArtistId(String artistId) {
        if(mArtistId == null || !mArtistId.equals(artistId)) {
            mArtistId = artistId;
            Bundle arguments = new Bundle();
            arguments.putString(ARG_ARTIST_ID, mArtistId);
            getLoaderManager().restartLoader(LOADER_TOP_TRACKS, arguments, this);
        }
    }

    @Override
    public Loader<List<TrackViewModel>> onCreateLoader(int id, Bundle args) {
        String artistId = args.getString(ARG_ARTIST_ID);
        return new TopTracksLoader(getActivity(), artistId, this);
    }

    @Override
    public void onLoadFinished(Loader<List<TrackViewModel>> loader, List<TrackViewModel> data) {
        UiUtils.crossfadeViews(mListView, mProgressView, true);
        mAdapter.setItems(data);
        mEmptyView.setVisibility(data != null && data.size() == 0 ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onLoaderReset(Loader<List<TrackViewModel>> loader) {
        mAdapter.setItems(null);
    }

    @Override
    public void onNetworkError(RetrofitError error) {
        String errorText = RetrofitHelper.getErrorText(getActivity(), error);
        Toast.makeText(getActivity(), errorText, Toast.LENGTH_SHORT).show();
    }

    public interface Callback {
        void onTrackClicked(TrackViewModel track);
    }
}
