package it.unipi.dii.inginf.lsmdb.unimusic.middleware.dao;


import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.Playlist;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.Song;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.User;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.exception.ActionNotCompletedException;

import java.util.List;

/**
 * Provides CRUD operations for Playlists
 */
public interface PlaylistDAO {

    void createPlaylist(Playlist playlist) throws ActionNotCompletedException;

    Playlist getPlaylist(String playlistID) throws ActionNotCompletedException;

    Playlist getFavourite(User user) throws ActionNotCompletedException;

    void addSong(Playlist playlist, Song song) throws ActionNotCompletedException;

    void deleteSong(Playlist playlist, Song song) throws ActionNotCompletedException;

    void addSongToFavourite(User user, Song song) throws ActionNotCompletedException;

    void deleteSongFromFavourite(User user, Song song) throws ActionNotCompletedException;

    void deletePlaylist(Playlist playlist) throws ActionNotCompletedException;

    boolean isSongFavourite(User user, Song song);

    List<Song> getAllSongs(Playlist playlist) throws ActionNotCompletedException;

    List<Playlist> getSuggestedPlaylists(User user) throws ActionNotCompletedException;

    List<Playlist> getSuggestedPlaylists(User user, int limit) throws ActionNotCompletedException;



    int getTotalPlaylists();

    void deletePlaylistDocument(Playlist playlist);
}
