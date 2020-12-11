package it.unipi.dii.inginf.lsmdb.unimusic.middleware.dao;


import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.Playlist;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.Song;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.User;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.exception.ActionNotCompletedException;

/**
 * Provides CRUD operations for Playlists
 */
public interface PlaylistDAO {

    public void createPlaylist(Playlist playlist) throws ActionNotCompletedException;

    public Playlist getPlaylist(String playlistID) throws ActionNotCompletedException;

    public Playlist getFavourite(User user) throws ActionNotCompletedException;

    public void addSong(Playlist playlist, Song song) throws ActionNotCompletedException;

    public void deleteSong(Playlist playlist, Song song) throws ActionNotCompletedException;

    public void deletePlaylist(Playlist playlist) throws ActionNotCompletedException;
}
