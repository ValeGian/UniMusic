package it.unipi.dii.inginf.lsmdb.unimusic.middleware.dao;

import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.Song;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.exception.ActionNotCompletedException;

import java.util.List;

/**
 * Provides CRUD operations for Songs
 */
public interface SongDAO {

    void createSong(Song song) throws ActionNotCompletedException;

    Song getSongById(String songID);

    List<Song> filterSong(String partialInput, int limit, String attributeField) throws ActionNotCompletedException;

    List<Song> getSongsByPartialAlbum(String partialAlbum, int limit) throws ActionNotCompletedException;

    List<Song> getSongsByPartialAlbum(String partialAlbum) throws ActionNotCompletedException;

    List<Song> getSongsByPartialTitle(String partialTitle, int limit) throws ActionNotCompletedException;

    List<Song> getSongsByPartialTitle(String partialTitle) throws ActionNotCompletedException;

    List<Song> getSongsByPartialArtist(String partialArtist, int limit) throws ActionNotCompletedException;

    List<Song> getSongsByPartialArtist(String partialArtist) throws ActionNotCompletedException;

    List<Song> getHotSongs() throws  ActionNotCompletedException;
}
