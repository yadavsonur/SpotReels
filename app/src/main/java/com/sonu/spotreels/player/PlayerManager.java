package com.sonu.spotreels.player;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media3.common.AudioAttributes;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

@UnstableApi
public class PlayerManager {

    private static final String TAG = "PlayerManager";
    private static PlayerManager instance;

    private ExoPlayer player;
    private final Context appContext;

    @Nullable
    private String currentUrl;

    private PlayerManager(@NonNull Context context) {
        this.appContext = context.getApplicationContext(); // ✅ No Activity leak
        initPlayer();
    }

    private void initPlayer() {
        player = new ExoPlayer.Builder(appContext).build();

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(androidx.media3.common.C.USAGE_MEDIA)
                .setContentType(androidx.media3.common.C.AUDIO_CONTENT_TYPE_MOVIE)
                .build();

        player.setAudioAttributes(audioAttributes, true);
        player.setRepeatMode(Player.REPEAT_MODE_ONE);
        player.setVolume(1f);

        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == Player.STATE_BUFFERING) {
                    Log.d(TAG, "Buffering...");
                } else if (state == Player.STATE_READY) {
                    Log.d(TAG, "Playback ready");
                }
            }

            @Override
            public void onPlayerError(PlaybackException error) {
                Log.e(TAG, "Playback error", error);
            }
        });

        currentUrl = null;
    }

    // ✅ FIXED NonNullApi WARNING HERE
    public static synchronized PlayerManager getInstance(@NonNull Context context) {
        if (instance == null) {
            instance = new PlayerManager(context);
        } else if (instance.player == null) {
            instance.initPlayer();
        }
        return instance;
    }

    public void playUrlOnPlayerView(String url, PlayerView playerView) {

        if (playerView == null || url == null || url.trim().isEmpty()) return;

        if (player == null) initPlayer();

        // ❌ REMOVE THESE (CAUSE BLACK SCREEN)
        // player.clearVideoSurface();
        // player.stop();

        // ✅ Correct: simple player attach
        playerView.setPlayer(player);

        if (!url.equals(currentUrl)) {
            MediaItem mediaItem = MediaItem.fromUri(Uri.parse(url));
            player.setMediaItem(mediaItem);
            player.prepare();
            currentUrl = url;
        }

        player.setPlayWhenReady(true);
    }


    public void pause() {
        if (player != null) {
            player.setPlayWhenReady(false);
        }
    }

    // ✅ KEEP THIS FOR LOGOUT / APP EXIT
    public static synchronized void releasePlayer() {
        if (instance != null && instance.player != null) {
            instance.player.release();
            instance.player = null;
            instance.currentUrl = null;
            instance = null;
        }
    }
}
