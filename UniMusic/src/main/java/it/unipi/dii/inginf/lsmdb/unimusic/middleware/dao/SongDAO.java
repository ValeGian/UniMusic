package it.unipi.dii.inginf.lsmdb.unimusic.middleware.dao;

import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.Album;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.Song;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.exception.ActionNotCompletedException;
import javafx.util.Pair;
import org.bson.Document;

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

    List<Pair<Integer, Pair<Album, Double>>> findTopRatedAlbumPerDecade() throws ActionNotCompletedException;

    List<Pair<String, Integer>> findArtistsWithMostNumberOfHit(int hitLimit, int maxNumber) throws ActionNotCompletedException;

    List<Song> getHotSongs(int limit) throws  ActionNotCompletedException;

    void incrementLikeCount(Song song) throws ActionNotCompletedException;

    void decrementLikeCount(Song song) throws ActionNotCompletedException;

    int getTotalSongs();

    void deleteSong(Song song) throws ActionNotCompletedException, IllegalArgumentException;

    void deleteSongDocument(Song mongoSong);
}
