package nz.co.nonameden.spotifystreamer.ui;

import android.databinding.DataBindingUtil;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import butterknife.InjectView;
import butterknife.OnClick;
import nz.co.nonameden.spotifystreamer.R;
import nz.co.nonameden.spotifystreamer.databinding.FragmentPlayerBinding;
import nz.co.nonameden.spotifystreamer.infrastructure.models.NowPlayingViewModel;
import nz.co.nonameden.spotifystreamer.media.compat.MediaBrowserCompat;
import nz.co.nonameden.spotifystreamer.media.compat.MediaProvider;
import nz.co.nonameden.spotifystreamer.ui.base.BaseFragment;

/**
 * Created by nonameden on 13/06/15.
 */
public class PlayerFragment extends BaseFragment<MediaProvider> {

    private static final String ARG_VIEW_MODEL = "arg-view-model";

    private static final long PROGRESS_UPDATE_INTERNAL = 1000;
    private static final long PROGRESS_UPDATE_INITIAL_INTERVAL = 100;

    private final ScheduledExecutorService mExecutorService = Executors.newSingleThreadScheduledExecutor();
    private final Handler mHandler = new Handler();

    @InjectView(R.id.seek_bar)
    SeekBar mSeekBar;

    private FragmentPlayerBinding mDataBinder;
    private NowPlayingViewModel mViewModel;
    private ScheduledFuture<?> mScheduleFuture;
    private PlaybackStateCompat mLastPlaybackState;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState == null) {
            mViewModel = new NowPlayingViewModel();
        } else {
            mViewModel = savedInstanceState.getParcelable(ARG_VIEW_MODEL);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mDataBinder = DataBindingUtil.inflate(inflater, R.layout.fragment_player, container, false);
        mDataBinder.setTrack(mViewModel);
        return mDataBinder.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mSeekBar.setOnSeekBarChangeListener(mSeekBarListener);
    }

    @Override
    protected MediaProvider initStubCallback() {
        return new MediaProvider() {
            @Override
            public MediaBrowserCompat getMediaBrowserCompat() {
                return null;
            }

            @Override
            public MediaControllerCompat getMediaControllerCompat() {
                return null;
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();

        onConnected();
    }

    public void onConnected() {
        MediaControllerCompat controller = getCallback().getMediaControllerCompat();
        if (controller != null) {
            onMetadataChanged(controller.getMetadata());
            onPlaybackStateChanged(controller.getPlaybackState());
            controller.registerCallback(mCallback);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        MediaControllerCompat controller = getCallback().getMediaControllerCompat();
        if (controller != null) {
            controller.unregisterCallback(mCallback);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(ARG_VIEW_MODEL, mViewModel);
    }

    public void onMetadataChanged(MediaMetadataCompat metadata) {
        if(metadata != null) {
            mViewModel.updateFromMetadata(metadata);
        }
    }

    private void onPlaybackStateChanged(PlaybackStateCompat playbackState) {
        mLastPlaybackState = playbackState;
        mViewModel.setPlaybackState(playbackState);

        if(playbackState.getState() == PlaybackStateCompat.STATE_PLAYING) {
            scheduleSeekBarUpdate();
        } else {
            stopSeekBarUpdate();
        }
    }

    private void scheduleSeekBarUpdate() {
        stopSeekBarUpdate();
        if (!mExecutorService.isShutdown()) {
            mScheduleFuture = mExecutorService.scheduleAtFixedRate(
                    new Runnable() {
                        @Override
                        public void run() {
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    updateProgress();
                                }
                            });
                        }
                    }, PROGRESS_UPDATE_INITIAL_INTERVAL,
                    PROGRESS_UPDATE_INTERNAL, TimeUnit.MILLISECONDS);
        }
    }

    private void stopSeekBarUpdate() {
        if (mScheduleFuture != null) {
            mScheduleFuture.cancel(false);
        }
    }

    private void updateProgress() {
        if (mLastPlaybackState == null) {
            return;
        }
        long currentPosition = mLastPlaybackState.getPosition();
        if (mLastPlaybackState.getState() != PlaybackState.STATE_PAUSED) {
            // Calculate the elapsed time between the last position update and now and unless
            // paused, we can assume (delta * speed) + current position is approximately the
            // latest position. This ensure that we do not repeatedly call the getPlaybackState()
            // on MediaController.
            long timeDelta = SystemClock.elapsedRealtime() -
                    mLastPlaybackState.getLastPositionUpdateTime();
            currentPosition += (int) timeDelta * mLastPlaybackState.getPlaybackSpeed();
        }
        mViewModel.setCurrentTime((int) currentPosition);
    }

    private SeekBar.OnSeekBarChangeListener mSeekBarListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if(fromUser) {
                mViewModel.setCurrentTime(progress);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            stopSeekBarUpdate();
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            MediaControllerCompat controller = getCallback().getMediaControllerCompat();
            controller.getTransportControls().seekTo(seekBar.getProgress());
            scheduleSeekBarUpdate();
        }
    };

    private MediaControllerCompat.Callback mCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            PlayerFragment.this.onMetadataChanged(metadata);
        }

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            PlayerFragment.this.onPlaybackStateChanged(state);
        }
    };

    @OnClick(R.id.prev)
    public void onSkipPreviousClicked() {
        MediaControllerCompat controller = getCallback().getMediaControllerCompat();
        if(controller!=null) {
            MediaControllerCompat.TransportControls controls = controller.getTransportControls();
            controls.skipToPrevious();
        }
    }

    @OnClick(R.id.play_pause)
    public void onPlayPauseClicked() {
        MediaControllerCompat controller = getCallback().getMediaControllerCompat();
        if(controller == null) return;

        PlaybackStateCompat state = controller.getPlaybackState();
        if (state != null) {
            MediaControllerCompat.TransportControls controls =
                    controller.getTransportControls();
            switch (state.getState()) {
                case PlaybackState.STATE_PLAYING: // fall through
                case PlaybackState.STATE_BUFFERING:
                    controls.pause();
                    stopSeekBarUpdate();
                    break;
                case PlaybackState.STATE_PAUSED:
                case PlaybackState.STATE_STOPPED:
                    controls.play();
                    scheduleSeekBarUpdate();
                    break;
            }
        }
    }

    @OnClick(R.id.next)
    public void onSkipNextClicked() {
        MediaControllerCompat controller = getCallback().getMediaControllerCompat();
        if(controller!=null) {
            MediaControllerCompat.TransportControls controls = controller.getTransportControls();
            controls.skipToNext();
        }
    }
}
