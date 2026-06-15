package com.javanapps.musicplayer.core.media.controller

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.javanapps.musicplayer.core.common.dispatcher.Dispatcher
import com.javanapps.musicplayer.core.common.dispatcher.MusicPlayerDispatchers.Default
import com.javanapps.musicplayer.core.common.dispatcher.di.ApplicationScope
import com.javanapps.musicplayer.core.domain.repository.SongsRepository
import com.javanapps.musicplayer.core.domain.repository.UserDataRepository
import com.javanapps.musicplayer.core.media.service.MusicService
import com.javanapps.musicplayer.core.model.PlayerState
import com.javanapps.musicplayer.core.model.RepeatMode
import com.javanapps.musicplayer.core.model.Song
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds

@Singleton
class Media3PlayerController
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        @ApplicationScope private val scope: CoroutineScope,
        @Dispatcher(Default) private val dispatcher: CoroutineDispatcher,
        private val userDataRepository: UserDataRepository,
        private val songsRepository: SongsRepository,
    ) : PlayerController {
        private var mediaController: MediaController? = null
        private val _playerState = MutableStateFlow(PlayerState())
        override val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

        init {
            initializeController()
        }

        private fun initializeController() {
            val sessionToken = SessionToken(context, ComponentName(context, MusicService::class.java))
            val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()

            // MediaController must be created and accessed on the main thread.
            scope.launch(Dispatchers.Main) {
                mediaController = controllerFuture.await()
                mediaController?.addListener(
                    object : Player.Listener {
                        override fun onEvents(
                            player: Player,
                            events: Player.Events,
                        ) {
                            updateState()
                            if (events.containsAny(
                                    Player.EVENT_PLAY_WHEN_READY_CHANGED,
                                    Player.EVENT_MEDIA_ITEM_TRANSITION,
                                    Player.EVENT_POSITION_DISCONTINUITY,
                                )
                            ) {
                                savePlaybackPosition()
                            }
                        }
                    },
                )

                restoreLastSession()

                launch {
                    var lastSavedPosition = 0L
                    while (true) {
                        if (mediaController?.isPlaying == true) {
                            val currentPos = mediaController?.currentPosition ?: 0
                            _playerState.update { it.copy(currentPosition = currentPos) }

                            // Save position every 10 seconds if playing
                            if (kotlin.math.abs(currentPos - lastSavedPosition) > 10000) {
                                savePlaybackPosition()
                                lastSavedPosition = currentPos
                            }
                        }
                        delay(1.seconds)
                    }
                }
            }
        }

        private fun savePlaybackPosition() {
            val controller = mediaController ?: return
            val songId = controller.currentMediaItem?.mediaId?.toLongOrNull() ?: return
            val position = controller.currentPosition
            val index = controller.currentMediaItemIndex
            val songIds =
                (0 until controller.mediaItemCount).mapNotNull {
                    controller.getMediaItemAt(it).mediaId.toLongOrNull()
                }

            scope.launch(dispatcher) {
                userDataRepository.setLastPlayedSong(songId, position)
                userDataRepository.setLastQueue(songIds, index)
            }
        }

        private suspend fun restoreLastSession() {
            val controller = mediaController ?: return
            // Read DataStore on IO dispatcher, then apply to controller on Main.
            val userData = withContext(dispatcher) { userDataRepository.userData.first() }

            controller.shuffleModeEnabled = userData.shuffleMode
            controller.repeatMode =
                when (userData.repeatMode) {
                    RepeatMode.ALL -> Player.REPEAT_MODE_ALL
                    RepeatMode.ONE -> Player.REPEAT_MODE_ONE
                    RepeatMode.NONE -> Player.REPEAT_MODE_OFF
                }

            if (userData.lastQueueSongIds.isNotEmpty()) {
                val allSongs = withContext(dispatcher) { songsRepository.getSongs().first() }
                val queueSongs =
                    userData.lastQueueSongIds.mapNotNull { id -> allSongs.find { it.id == id } }
                if (queueSongs.isNotEmpty()) {
                    val safeIndex = userData.lastQueueIndex.coerceIn(0, queueSongs.lastIndex)
                    controller.setMediaItems(
                        queueSongs.map { it.asMediaItem() },
                        safeIndex,
                        userData.lastPlaybackPosition,
                    )
                    controller.prepare()
                }
            }
        }

        private fun updateState() {
            val controller = mediaController ?: return
            val queue =
                (0 until controller.mediaItemCount).mapNotNull { i ->
                    controller.getMediaItemAt(i).asExternalModel()
                }
            _playerState.update { state ->
                state.copy(
                    currentSong = controller.currentMediaItem?.asExternalModel(),
                    isPlaying = controller.isPlaying,
                    currentPosition = controller.currentPosition,
                    duration =
                        controller.duration.takeIf {
                            it != androidx.media3.common.C.TIME_UNSET
                        } ?: 0,
                    shuffleMode = controller.shuffleModeEnabled,
                    repeatMode =
                        when (controller.repeatMode) {
                            Player.REPEAT_MODE_ALL -> RepeatMode.ALL
                            Player.REPEAT_MODE_ONE -> RepeatMode.ONE
                            else -> RepeatMode.NONE
                        },
                    queue = queue,
                )
            }
        }

        override fun play(
            songs: List<Song>,
            startIndex: Int,
        ) {
            mediaController?.apply {
                setMediaItems(songs.map { it.asMediaItem() }, startIndex, 0)
                prepare()
                play()
            }
            scope.launch(dispatcher) { userDataRepository.setLastQueue(songs.map { it.id }, startIndex) }
        }

        override fun pause() {
            mediaController?.pause()
        }

        override fun resume() {
            mediaController?.play()
        }

        override fun skipToNext() {
            mediaController?.seekToNext()
        }

        override fun skipToPrevious() {
            mediaController?.seekToPrevious()
        }

        override fun seekTo(position: Long) {
            mediaController?.seekTo(position)
        }

        override fun setShuffleMode(enabled: Boolean) {
            mediaController?.shuffleModeEnabled = enabled
            scope.launch(dispatcher) { userDataRepository.setShuffleMode(enabled) }
        }

        override fun setRepeatMode(repeatMode: RepeatMode) {
            mediaController?.repeatMode =
                when (repeatMode) {
                    RepeatMode.ALL -> Player.REPEAT_MODE_ALL
                    RepeatMode.ONE -> Player.REPEAT_MODE_ONE
                    RepeatMode.NONE -> Player.REPEAT_MODE_OFF
                }
            scope.launch(dispatcher) { userDataRepository.setRepeatMode(repeatMode) }
        }

        override fun addToQueueNext(song: Song) {
            val controller = mediaController ?: return
            val insertIndex = (controller.currentMediaItemIndex + 1).coerceAtMost(controller.mediaItemCount)
            controller.addMediaItem(insertIndex, song.asMediaItem())
        }

        override fun addToQueueLast(song: Song) {
            mediaController?.addMediaItem(song.asMediaItem())
        }
    }

private fun Song.asMediaItem(): MediaItem =
    MediaItem
        .Builder()
        .setMediaId(mediaId)
        .setUri(mediaUri)
        .setMediaMetadata(
            MediaMetadata
                .Builder()
                .setTitle(title)
                .setArtist(artist)
                .setAlbumTitle(album)
                .setArtworkUri(android.net.Uri.parse(artworkUri))
                .build(),
        ).setRequestMetadata(
            MediaItem.RequestMetadata
                .Builder()
                .setMediaUri(android.net.Uri.parse(mediaUri))
                .build(),
        ).build()

private fun MediaItem.asExternalModel(): Song =
    Song(
        id = mediaId.toLongOrNull() ?: 0L,
        mediaId = mediaId,
        title = mediaMetadata.title?.toString() ?: "",
        artist = mediaMetadata.artist?.toString() ?: "",
        artistId = 0L,
        album = mediaMetadata.albumTitle?.toString() ?: "",
        albumId = 0L,
        duration = 0,
        artworkUri = mediaMetadata.artworkUri?.toString(),
        mediaUri = localConfiguration?.uri?.toString() ?: requestMetadata.mediaUri?.toString() ?: "",
        dateAdded = 0,
    )
