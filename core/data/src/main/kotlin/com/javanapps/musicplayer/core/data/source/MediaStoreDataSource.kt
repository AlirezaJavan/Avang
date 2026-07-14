package com.javanapps.musicplayer.core.data.source

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.ContentObserver
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import com.javanapps.musicplayer.core.common.dispatcher.Dispatcher
import com.javanapps.musicplayer.core.common.dispatcher.MusicPlayerDispatchers.IO
import com.javanapps.musicplayer.core.model.Album
import com.javanapps.musicplayer.core.model.Artist
import com.javanapps.musicplayer.core.model.Song
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject

private val ALBUM_ART_URI = Uri.parse("content://media/external/audio/albumart")

class MediaStoreDataSource
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val contentResolver: ContentResolver,
        @Dispatcher(IO) private val ioDispatcher: CoroutineDispatcher,
    ) {
        fun getSongs(): Flow<List<Song>> =
            callbackFlow {
                val observer =
                    object : ContentObserver(null) {
                        override fun onChange(selfChange: Boolean) {
                            launch {
                                send(querySongs())
                            }
                        }
                    }
                contentResolver.registerContentObserver(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    true,
                    observer,
                )
                trySend(querySongs())
                awaitClose { contentResolver.unregisterContentObserver(observer) }
            }.flowOn(ioDispatcher).conflate()

        fun triggerMediaScan() {
            // Scoped to music-relevant directories only - scanning the whole external
            // storage root (Environment.getExternalStorageDirectory()) is recursive over
            // the entire device and was the main cause of Library screen lag.
            val dirs =
                listOfNotNull(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PODCASTS),
                ).filter { it.exists() }
                    .map { it.absolutePath }
                    .toTypedArray()

            if (dirs.isNotEmpty()) {
                MediaScannerConnection.scanFile(context, dirs, null, null)
            }
        }

        private fun querySongs(): List<Song> {
            val songs = mutableListOf<Song>()
            val projection =
                arrayOf(
                    MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.ARTIST_ID,
                    MediaStore.Audio.Media.ALBUM,
                    MediaStore.Audio.Media.ALBUM_ID,
                    MediaStore.Audio.Media.DURATION,
                    MediaStore.Audio.Media.DATA,
                    MediaStore.Audio.Media.DATE_ADDED,
                )

            val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
            val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

            contentResolver
                .query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    selection,
                    null,
                    sortOrder,
                )?.use { cursor ->
                    val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                    val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                    val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                    val artistIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID)
                    val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                    val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                    val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                    val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                    val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)

                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(idColumn)
                        val albumId = cursor.getLong(albumIdColumn)
                        val artistId = cursor.getLong(artistIdColumn)
                        val artworkUri =
                            ContentUris
                                .withAppendedId(
                                    ALBUM_ART_URI,
                                    albumId,
                                ).toString()

                        songs.add(
                            Song(
                                id = id,
                                mediaId = id.toString(),
                                title = cursor.getString(titleColumn),
                                artist = cursor.getString(artistColumn),
                                artistId = artistId,
                                album = cursor.getString(albumColumn),
                                albumId = albumId,
                                duration = cursor.getLong(durationColumn),
                                artworkUri = artworkUri,
                                mediaUri =
                                    ContentUris
                                        .withAppendedId(
                                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                            id,
                                        ).toString(),
                                dateAdded = cursor.getLong(dateAddedColumn),
                            ),
                        )
                    }
                }
            return songs
        }

        fun getAlbums(): Flow<List<Album>> =
            callbackFlow {
                val observer =
                    object : ContentObserver(null) {
                        override fun onChange(selfChange: Boolean) {
                            launch {
                                send(queryAlbums())
                            }
                        }
                    }
                contentResolver.registerContentObserver(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    true,
                    observer,
                )
                trySend(queryAlbums())
                awaitClose { contentResolver.unregisterContentObserver(observer) }
            }.flowOn(ioDispatcher).conflate()

        private fun queryAlbums(): List<Album> {
            val albums = mutableListOf<Album>()
            val projection =
                arrayOf(
                    MediaStore.Audio.Albums._ID,
                    MediaStore.Audio.Albums.ALBUM,
                    MediaStore.Audio.Albums.ARTIST,
                    MediaStore.Audio.Albums.NUMBER_OF_SONGS,
                )

            contentResolver
                .query(
                    MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                    projection,
                    null,
                    null,
                    "${MediaStore.Audio.Albums.ALBUM} ASC",
                )?.use { cursor ->
                    val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums._ID)
                    val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM)
                    val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ARTIST)
                    val countColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.NUMBER_OF_SONGS)

                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(idColumn)
                        albums.add(
                            Album(
                                id = id,
                                title = cursor.getString(albumColumn),
                                artist = cursor.getString(artistColumn),
                                artworkUri =
                                    ContentUris
                                        .withAppendedId(
                                            ALBUM_ART_URI,
                                            id,
                                        ).toString(),
                                songCount = cursor.getInt(countColumn),
                            ),
                        )
                    }
                }
            return albums
        }

        fun getArtists(): Flow<List<Artist>> =
            callbackFlow {
                val observer =
                    object : ContentObserver(null) {
                        override fun onChange(selfChange: Boolean) {
                            launch {
                                send(queryArtists())
                            }
                        }
                    }
                contentResolver.registerContentObserver(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    true,
                    observer,
                )
                trySend(queryArtists())
                awaitClose { contentResolver.unregisterContentObserver(observer) }
            }.flowOn(ioDispatcher).conflate()

        private fun queryArtists(): List<Artist> {
            val artists = mutableListOf<Artist>()
            val projection =
                arrayOf(
                    MediaStore.Audio.Artists._ID,
                    MediaStore.Audio.Artists.ARTIST,
                    MediaStore.Audio.Artists.NUMBER_OF_TRACKS,
                    MediaStore.Audio.Artists.NUMBER_OF_ALBUMS,
                )

            contentResolver
                .query(
                    MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
                    projection,
                    null,
                    null,
                    "${MediaStore.Audio.Artists.ARTIST} ASC",
                )?.use { cursor ->
                    val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists._ID)
                    val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.ARTIST)
                    val tracksColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.NUMBER_OF_TRACKS)
                    val albumsColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.NUMBER_OF_ALBUMS)

                    while (cursor.moveToNext()) {
                        artists.add(
                            Artist(
                                id = cursor.getLong(idColumn),
                                name = cursor.getString(artistColumn),
                                songCount = cursor.getInt(tracksColumn),
                                albumCount = cursor.getInt(albumsColumn),
                            ),
                        )
                    }
                }
            return artists
        }
    }
