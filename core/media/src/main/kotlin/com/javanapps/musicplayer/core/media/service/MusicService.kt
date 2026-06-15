package com.javanapps.musicplayer.core.media.service

import android.app.PendingIntent
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.javanapps.musicplayer.core.domain.equalizer.EqualizerManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MusicService : MediaSessionService() {
    @Inject
    lateinit var player: ExoPlayer

    @Inject
    lateinit var equalizerManager: EqualizerManager

    private var mediaSession: MediaSession? = null

    @androidx.annotation.OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()

        val audioAttributes =
            AudioAttributes
                .Builder()
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .setUsage(C.USAGE_MEDIA)
                .build()

        player.setAudioAttributes(audioAttributes, true)
        equalizerManager.initialize(player.audioSessionId)

        val sessionActivityIntent =
            packageManager
                .getLaunchIntentForPackage(packageName)
                ?.let { intent ->
                    PendingIntent.getActivity(
                        this,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                    )
                }

        mediaSession =
            MediaSession
                .Builder(this, player)
                .apply { sessionActivityIntent?.let { setSessionActivity(it) } }
                .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onDestroy() {
        equalizerManager.release()
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}
