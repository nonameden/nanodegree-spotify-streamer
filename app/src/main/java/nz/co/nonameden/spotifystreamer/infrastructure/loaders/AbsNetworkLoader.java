package nz.co.nonameden.spotifystreamer.infrastructure.loaders;

import android.content.Context;
import android.content.Loader;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import nz.co.nonameden.spotifystreamer.R;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by nonameden on 6/06/15.
 */
public abstract class AbsNetworkLoader<T, K> extends Loader<T> implements Callback<K> {

    protected final SpotifyService mSpotifyService;
    private final ErrorCallback mErrorCallback;
    private final String mCountryCode;
    private final Handler mHandler = new Handler();
    private T mData;

    public AbsNetworkLoader(Context context, @NonNull ErrorCallback errorCallback) {
        super(context);

        mCountryCode = PreferenceManager.getDefaultSharedPreferences(context).getString(
                context.getString(R.string.key_country),
                context.getString(R.string.default_country)
        );

        mErrorCallback = errorCallback;
        mSpotifyService = new SpotifyApi().getService();
    }

    protected Map<String, Object> getAdditionalParammeters() {
        Map<String, Object> map = new HashMap<>();
        map.put("country", mCountryCode);
        return map;
    }

    @Override
    protected void onStartLoading() {
        if(mData != null)
            deliverResult(mData);

        if(takeContentChanged() || mData == null)
            forceLoad();
    }

    @Override
    protected void onForceLoad() {
        executeNetworkRequest();
    }

    @Override
    protected void onStopLoading() {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            //TODO: maybe add some handling later
        } else {
            cancelLoad();
        }
    }


    @Override
    public void deliverResult(T data) {
        if(isReset()){
            releaseData(data);
            return;
        }

        T oldData = mData;
        mData = data;

        if(isStarted()) {
            super.deliverResult(data);
        }

        if(oldData != null) {
            releaseData(oldData);
        }
    }

    @Override
    protected void onReset() {
        onStopLoading();

        if(mData != null){
            releaseData(mData);
            mData = null;
        }
    }

    protected void releaseData(T data) {}

    /**
     * Here we should make all our network calls and use this Loader as callback for them
     */
    protected abstract void executeNetworkRequest();
    protected abstract @NonNull T convert(K data);

    @Override
    public void success(K data, Response response) {
        if(isAbandoned()) return;
        T convertedData = convert(data);
        mHandler.post(new DeliveryRunnable(convertedData));
    }

    @SuppressWarnings("unchecked")
    @Override
    public void failure(RetrofitError error) {
        if (isAbandoned()) return;

        mHandler.post(new FailureRunnable(error));
    }

    public interface ErrorCallback {
        void onNetworkError(RetrofitError error);
    }

    private class DeliveryRunnable implements Runnable {

        private T mResult;

        public DeliveryRunnable(T result) {
            mResult = result;
        }

        @Override
        public void run() {
            deliverResult(mResult);
        }
    }

    private class FailureRunnable implements Runnable {

        private RetrofitError mError;

        public FailureRunnable(RetrofitError error) {
            mError = error;
        }

        @Override
        public void run() {
            mErrorCallback.onNetworkError(mError);
            deliverResult(null);
        }
    }
}