package nz.co.nonameden.spotifystreamer.ui;

import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import butterknife.InjectView;
import butterknife.OnItemClick;
import nz.co.nonameden.spotifystreamer.R;
import nz.co.nonameden.spotifystreamer.infrastructure.adapters.ArtistListAdapter;
import nz.co.nonameden.spotifystreamer.infrastructure.loaders.AbsNetworkLoader;
import nz.co.nonameden.spotifystreamer.infrastructure.loaders.ArtistSearchLoader;
import nz.co.nonameden.spotifystreamer.infrastructure.models.ArtistViewModel;
import nz.co.nonameden.spotifystreamer.infrastructure.utils.RetrofitHelper;
import nz.co.nonameden.spotifystreamer.infrastructure.utils.UiUtils;
import nz.co.nonameden.spotifystreamer.ui.base.BaseFragment;
import retrofit.RetrofitError;

/**
 * Created by nonameden on 3/06/15.
 */
public class ArtistSearchFragment extends BaseFragment<ArtistSearchFragment.Callback>
        implements LoaderManager.LoaderCallbacks<List<ArtistViewModel>>, AbsNetworkLoader.ErrorCallback {

    private static final String ARG_SEARCH_RESULTS = "arg-search-results";
    private static final String ARG_SEARCH_QUERY = "arg-search-query";

    private static final int LOADER_ARTIST_SEARCH = 100;
    private static final long CHARACTER_WAIT_MS = 200; // ms

    @InjectView(R.id.search_query) EditText mSearchView;
    @InjectView(R.id.list) ListView mListView;
    @InjectView(R.id.shadow) View mShadowView;
    @InjectView(R.id.content) View mContentView;
    @InjectView(R.id.progress) View mProgressView;

    private final Handler mHandler = new Handler();
    private ArtistListAdapter mAdapter;
    private String mLastSearchQuery;
    private boolean mIsTablet;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdapter = new ArtistListAdapter();
        if(savedInstanceState!=null) {
            mLastSearchQuery = savedInstanceState.getString(ARG_SEARCH_QUERY);
            ArrayList<ArtistViewModel> artists = savedInstanceState.getParcelableArrayList(ARG_SEARCH_RESULTS);
            mAdapter.setItems(artists);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_artist_search, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mSearchView.addTextChangedListener(mSearchQueryListener);
        mListView.setAdapter(mAdapter);
        mListView.setOnScrollListener(mScrollListener);
        setTabletMode(mIsTablet);

        UiUtils.crossfadeViews(mContentView, mProgressView, false);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ARG_SEARCH_QUERY, mSearchView.getText().toString());
        outState.putParcelableArrayList(ARG_SEARCH_RESULTS,
                (ArrayList<ArtistViewModel>) mAdapter.getItems().clone());
    }

    private final TextWatcher mSearchQueryListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            mHandler.removeCallbacks(mSearchRunnable);
            mHandler.postDelayed(mSearchRunnable, CHARACTER_WAIT_MS);
        }
    };

    private AbsListView.OnScrollListener mScrollListener = new AbsListView.OnScrollListener() {

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {}

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            boolean showShadow = true;
            if(firstVisibleItem == 0) {
                View firstChild = mListView.getChildAt(0);
                if (firstChild!=null) {
                    showShadow = (firstChild.getTop() != 0);
                }
            }
            showShadow(showShadow && mAdapter.getCount()!=0);
        }
    };

    private void showShadow(boolean showShadow) {
        mShadowView.setVisibility(showShadow ? View.VISIBLE : View.GONE);
    }

    private Runnable mSearchRunnable = new Runnable() {
        @Override
        public void run() {
            onSearchQueryChanged();
        }
    };

    private void onSearchQueryChanged() {
        String searchQuery = mSearchView.getText().toString();
        if(TextUtils.isEmpty(searchQuery)) {
            mAdapter.setItems(null);
        } else {
            if(mLastSearchQuery == null || !searchQuery.equals(mLastSearchQuery)) {
                mLastSearchQuery = null;

                Bundle arguments = new Bundle();
                arguments.putString(ARG_SEARCH_QUERY, searchQuery);
                getLoaderManager().restartLoader(LOADER_ARTIST_SEARCH, arguments, this);
            }
        }
    }

    @Override
    protected Callback initStubCallback() {
        return new Callback() {
            @Override
            public void onArtistClicked(ArtistViewModel artist) {}
        };
    }

    @OnItemClick(R.id.list)
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ArtistViewModel artist = mAdapter.getItem(position);
        getCallback().onArtistClicked(artist);
    }

    @Override
    public Loader<List<ArtistViewModel>> onCreateLoader(int id, Bundle args) {
        UiUtils.crossfadeViews(mProgressView, mContentView, true);
        String searchQuery = args.getString(ARG_SEARCH_QUERY);
        return new ArtistSearchLoader(getActivity(), searchQuery, this);
    }

    @Override
    public void onLoadFinished(Loader<List<ArtistViewModel>> loader, List<ArtistViewModel> data) {
        mAdapter.setItems(data);
        UiUtils.crossfadeViews(mContentView, mProgressView, true);
        if(data != null && data.size() == 0) {
            Toast.makeText(getActivity(), R.string.refine_search, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onLoaderReset(Loader<List<ArtistViewModel>> loader) {
        mAdapter.setItems(null);
    }

    @Override
    public void onNetworkError(RetrofitError error) {
        String errorText = RetrofitHelper.getErrorText(getActivity(), error);
        Toast.makeText(getActivity(), errorText, Toast.LENGTH_SHORT).show();
    }

    public void setTabletMode(boolean isTablet) {
        mIsTablet = isTablet;
        if(mListView!=null) {
            mListView.setChoiceMode(isTablet ? ListView.CHOICE_MODE_SINGLE : ListView.CHOICE_MODE_NONE);
        }
    }

    public interface Callback {
        void onArtistClicked(ArtistViewModel artist);
    }
}
