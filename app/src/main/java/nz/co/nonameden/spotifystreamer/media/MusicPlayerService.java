package nz.co.nonameden.spotifystreamer.media;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;

import nz.co.nonameden.spotifystreamer.BuildConfig;
import nz.co.nonameden.spotifystreamer.R;
import nz.co.nonameden.spotifystreamer.infrastructure.shared.Constants;
import nz.co.nonameden.spotifystreamer.media.compat.MediaBrowserServiceCompat;
import nz.co.nonameden.spotifystreamer.media.compat.MediaItemCompat;
import nz.co.nonameden.spotifystreamer.media.playback.LocalPlayback;
import nz.co.nonameden.spotifystreamer.media.playback.Playback;
import nz.co.nonameden.spotifystreamer.ui.PlayerActivity;

/**
 * Created by nonameden on 13/06/15.
 */
public class MusicPlayerService extends MediaBrowserServiceCompat implements Playback.Callback {

    // The action of the incoming Intent indicating that it contains a command
    // to be executed (see {@link #onStartCommand})
    public static final String ACTION_CMD = BuildConfig.APPLICATION_ID + ".ACTION_CMD";
    // The key in the extras of the incoming Intent indicating the command that
    // should be executed (see {@link #onStartCommand})
    public static final String CMD_NAME = "CMD_NAME";

    public static final String CMD_PAUSE = "CMD_PAUSE";
    public static final String CMD_PLAY = "CMD_PLAY";
    public static final String CMD_NEXT = "CMD_NEXT";
    public static final String CMD_PREV = "CMD_PREV";

    /**
     * @hide
     */
    @StringDef({CMD_PAUSE, CMD_PLAY, CMD_NEXT, CMD_PREV})
    public @interface Commands {}

    private final DelayedStopHandler mDelayedStopHandler = new DelayedStopHandler(this);
    private static final int STOP_DELAY = 30000;

    private MediaSessionCompat mSession;
    private MediaNotificationManager mMediaNotificationManager;
    private List<MediaSessionCompat.QueueItem> mPlayingQueue;
    private int mCurrentIndexOnQueue;
    private Playback mPlayback;
    private Map<String, MediaMetadataCompat> mTracksMap;
    private boolean mServiceStarted;

    @Override
    public void onCreate() {
        super.onCreate();

        // Start a new MediaSession
        ComponentName eventReceiver = new ComponentName(getPackageName(), MediaNotificationManager.class.getName());
        PendingIntent buttonReceiverIntent = PendingIntent.getBroadcast(
                this,
                0,
                new Intent(Constants.ACTION_MEDIA_BUTTONS),
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        mSession = new MediaSessionCompat(this, "SpotifyService", eventReceiver, buttonReceiverIntent);
        setSessionToken(mSession.getSessionToken());
        mSession.setCallback(new MediaSessionCallback());
        mSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        mPlayback = new LocalPlayback(this);
        mPlayback.setState(PlaybackStateCompat.STATE_NONE);
        mPlayback.setCallback(this);
        mPlayback.start();

        Context context = getApplicationContext();
        Intent intent = new Intent(context, PlayerActivity.class);
        PendingIntent pi = PendingIntent.getActivity(context, 99 /*request code*/,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mSession.setSessionActivity(pi);

        mMediaNotificationManager = new MediaNotificationManager(this);
    }

    @Override
    public int onStartCommand(Intent startIntent, int flags, int startId) {
        if (startIntent != null) {
            String action = startIntent.getAction();
            String command = startIntent.getStringExtra(CMD_NAME);
            if (ACTION_CMD.equals(action)) {
                if (CMD_PAUSE.equals(command)) {
                    if (mPlayback != null && mPlayback.isPlaying()) {
                        handlePauseRequest();
                    }
                }
            }
        }
        // Reset the delay handler to enqueue a message to stop the service if
        // nothing is playing.
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        mDelayedStopHandler.sendEmptyMessageDelayed(0, STOP_DELAY);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handleStopRequest(null);
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        mSession.release();
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        return new BrowserRoot(Constants.MEDIA_BROWSER_ROOT, null);
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<MediaItemCompat[]> result) {
        // TODO: move calling API from UI classes to here
    }

    @Override
    public void onCompletion() {
        // The media player finished playing the current song, so we go ahead
        // and start the next.
        if (mPlayingQueue != null && !mPlayingQueue.isEmpty()) {
            // In this sample, we restart the playing queue when it gets to the end:
            mCurrentIndexOnQueue++;
            if (mCurrentIndexOnQueue >= mPlayingQueue.size()) {
                mCurrentIndexOnQueue = 0;
            }
            handlePlayRequest();
        } else {
            // If there is nothing to play, we stop and release the resources:
            handleStopRequest(null);
        }
    }

    @Override
    public void onPlaybackStatusChanged(int state) {
        updateDuration();
        updatePlaybackState(null);
    }

    @Override
    public void onError(String error) {
        updatePlaybackState(error);
    }

    @Override
    public void onMetadataChanged(String mediaId) {
        int index = QueueHelper.getMusicIndexOnQueue(mPlayingQueue, mediaId);
        if (index > -1) {
            mCurrentIndexOnQueue = index;
            updateMetadata();
        }
    }

    public MediaMetadataCompat getTrack(@NonNull String mediaId) {
        if(mTracksMap != null) {
            return mTracksMap.get(mediaId);
        }
        return null;
    }

    private final class MediaSessionCallback extends MediaSessionCompat.Callback {
        @Override
        public void onPlay() {
            if (mPlayingQueue != null && !mPlayingQueue.isEmpty()) {
                handlePlayRequest();
            }
        }

        @Override
        public void onSkipToQueueItem(long queueId) {
            if (mPlayingQueue != null && !mPlayingQueue.isEmpty()) {
                // set the current index on queue from the music Id:
                mCurrentIndexOnQueue = QueueHelper.getMusicIndexOnQueue(mPlayingQueue, queueId);
                // play the music
                handlePlayRequest();
            }
        }

        @Override
        public void onSeekTo(long position) {
            mPlayback.seekTo((int) position);
        }

        @Override
        public void onPause() {
            handlePauseRequest();
        }

        @Override
        public void onStop() {
            handleStopRequest(null);
        }

        @Override
        public void onSkipToNext() {
            mCurrentIndexOnQueue++;
            if (mPlayingQueue != null && mCurrentIndexOnQueue >= mPlayingQueue.size()) {
                // This sample's behavior: skipping to next when in last song returns to the
                // first song.
                mCurrentIndexOnQueue = 0;
            }
            if (QueueHelper.isIndexPlayable(mCurrentIndexOnQueue, mPlayingQueue)) {
                handlePlayRequest();
            } else {
                handleStopRequest("Cannot skip");
            }
        }

        @Override
        public void onSkipToPrevious() {
            mCurrentIndexOnQueue--;
            if (mPlayingQueue != null && mCurrentIndexOnQueue < 0) {
                // This sample's behavior: skipping to previous when in first song restarts the
                // first song.
                mCurrentIndexOnQueue = 0;
            }
            if (QueueHelper.isIndexPlayable(mCurrentIndexOnQueue, mPlayingQueue)) {
                handlePlayRequest();
            } else {
                handleStopRequest("Cannot skip");
            }
        }

        @Override
        public void onPlayFromMediaId(String mediaId, Bundle extras) {
            QueueHelper.ConverterState state = QueueHelper.convertToQueue(extras);
            if(state == null) {
                mTracksMap = null;
                mPlayingQueue = null;
            } else {
                mTracksMap = state.getTracks();
                mPlayingQueue = state.getQueueItems();
            }
            mSession.setQueue(mPlayingQueue);

            if (mPlayingQueue != null && !mPlayingQueue.isEmpty()) {
                mCurrentIndexOnQueue = QueueHelper.getMusicIndexOnQueue(mPlayingQueue, mediaId);

                if (mCurrentIndexOnQueue >= 0) {
                    handlePlayRequest();
                }
            }
        }
    }

    /**
     * Handle a request to play music
     */
    private void handlePlayRequest() {
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        if (!mServiceStarted) {
            // The MusicService needs to keep running even after the calling MediaBrowser
            // is disconnected. Call startService(Intent) and then stopSelf(..) when we no longer
            // need to play media.
            startService(new Intent(getApplicationContext(), MusicPlayerService.class));
            mServiceStarted = true;
        }

        if (!mSession.isActive()) {
            mSession.setActive(true);
        }

        if (QueueHelper.isIndexPlayable(mCurrentIndexOnQueue, mPlayingQueue)) {
            updateMetadata();
            mPlayback.play(mPlayingQueue.get(mCurrentIndexOnQueue));
        }
    }

    /**
     * Handle a request to pause music
     */
    private void handlePauseRequest() {
        mPlayback.pause();
        // reset the delayed stop handler.
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        mDelayedStopHandler.sendEmptyMessageDelayed(0, STOP_DELAY);
    }

    /**
     * Handle a request to stop music
     */
    private void handleStopRequest(String withError) {
        mPlayback.stop(true);
        // reset the delayed stop handler.
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        mDelayedStopHandler.sendEmptyMessageDelayed(0, STOP_DELAY);

        updatePlaybackState(withError);

        // service is no longer necessary. Will be started again if needed.
        stopSelf();
        mServiceStarted = false;
    }

    private void updateMetadata() {
        if (!QueueHelper.isIndexPlayable(mCurrentIndexOnQueue, mPlayingQueue)) {
            updatePlaybackState(getResources().getString(R.string.error_no_metadata));
            return;
        }

        MediaSessionCompat.QueueItem queueItem = mPlayingQueue.get(mCurrentIndexOnQueue);
        final String trackId = queueItem.getDescription().getMediaId();
        MediaMetadataCompat track = getTrack(trackId);
        if (track == null) {
            throw new IllegalArgumentException("Invalid musicId " + trackId);
        }
        mSession.setMetadata(track);

        // Set the proper album artwork on the media session, so it can be shown in the
        // locked screen and in other places.
        if (track.getDescription().getIconBitmap() == null &&
                track.getDescription().getIconUri() != null) {
            Uri albumUri = track.getDescription().getIconUri();
            Glide.with(this).load(albumUri).asBitmap().into(new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                    if (mTracksMap != null) {
                        MediaSessionCompat.QueueItem queueItem = mPlayingQueue.get(mCurrentIndexOnQueue);
                        MediaMetadataCompat track = getTrack(trackId);
                        if (track != null) {
                            track = new MediaMetadataCompat.Builder(track)
                                    .putBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, resource)
                                    .build();
                            mTracksMap.put(trackId, track);
                            if (queueItem.getDescription().getMediaId().equals(trackId)) {
                                mSession.setMetadata(track);
                            }
                        }
                    }
                }
            });
        }
    }

    /**
     * Update the current media player state, optionally showing an error message.
     *
     * @param error if not null, error message to present to the user.
     */
    private void updatePlaybackState(String error) {
        long position = PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN;
        if (mPlayback != null && mPlayback.isConnected()) {
            position = mPlayback.getCurrentStreamPosition();
        }

        PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder()
                .setActions(getAvailableActions());

        int state = mPlayback.getState();

        // If there is an error message, send it to the playback state:
        if (error != null) {
            // Error states are really only supposed to be used for errors that cause playback to
            // stop unexpectedly and persist until the user takes action to fix it.
            stateBuilder.setErrorMessage(error);
            state = PlaybackStateCompat.STATE_ERROR;
        }
        stateBuilder.setState(state, position, 1.0f, SystemClock.elapsedRealtime());

        // Set the activeQueueItemId if the current index is valid.
        if (QueueHelper.isIndexPlayable(mCurrentIndexOnQueue, mPlayingQueue)) {
            MediaSessionCompat.QueueItem item = mPlayingQueue.get(mCurrentIndexOnQueue);
            stateBuilder.setActiveQueueItemId(item.getQueueId());
        }

        mSession.setPlaybackState(stateBuilder.build());

        if (state == PlaybackStateCompat.STATE_PLAYING || state == PlaybackStateCompat.STATE_PAUSED) {
            mMediaNotificationManager.startNotification();
        }
    }

    private void updateDuration() {
        MediaSessionCompat.QueueItem queueItem = mPlayingQueue.get(mCurrentIndexOnQueue);
        final String trackId = queueItem.getDescription().getMediaId();
        MediaMetadataCompat track = getTrack(trackId);
        if (track == null) {
            throw new IllegalArgumentException("Invalid musicId " + trackId);
        }

        track = new MediaMetadataCompat.Builder(track)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, mPlayback.getDuration())
                .build();
        mTracksMap.put(trackId, track);
        mSession.setMetadata(track);
    }

    private long getAvailableActions() {
        long actions = PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID |
                PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH;
        if (mPlayingQueue == null || mPlayingQueue.isEmpty()) {
            return actions;
        }
        if (mPlayback.isPlaying()) {
            actions |= PlaybackStateCompat.ACTION_PAUSE;
        }
        if (mCurrentIndexOnQueue > 0) {
            actions |= PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS;
        }
        if (mCurrentIndexOnQueue < mPlayingQueue.size() - 1) {
            actions |= PlaybackStateCompat.ACTION_SKIP_TO_NEXT;
        }
        return actions;
    }

    /**
     * A simple handler that stops the service if playback is not active (playing)
     */
    private static class DelayedStopHandler extends Handler {
        private final WeakReference<MusicPlayerService> mWeakReference;

        private DelayedStopHandler(MusicPlayerService service) {
            mWeakReference = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            MusicPlayerService service = mWeakReference.get();
            if (service != null && service.mPlayback != null) {
                if (service.mPlayback.isPlaying()) {
                    return;
                }
                service.stopSelf();
                service.mServiceStarted = false;
            }
        }
    }
}
