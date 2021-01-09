package it.unipi.dii.inginf.lsmdb.unimusic.middleware.dao;

import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.Album;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.Song;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.exception.ActionNotCompletedException;
import javafx.util.Pair;

import java.util.List;

/**
 * Provides CRUD operations for Songs
 */
public interface SongDAO {

    /**
     * Add a song in both databases and handle possible errors:
     * 1) if the song isn't added to MongoDb throws ActionNotCompletedException.
     * 2) if the song isn't added to Neo4j delete also the document in MongoDb to avoid inconsitency and then throws  ActionNotCompletedException.
     * In any case errors are logged.
     * @param song the song you want to add to databases.
     * @throws ActionNotCompletedException when a database write fails.
     */
    void createSong(Song song) throws ActionNotCompletedException;

    /**
     * @param songID the id of the song you wanto to return.
     * @return the song with the specified id.
     */
    Song getSongById(String songID);

    /**
     * @param partialInput the partial input of the user.
     * @param limit the max number of song you want to return.
     * @param attributeField the document's attribute you want to match.
     * @return songs where the specified attribute fields contains the partial input of the user (case insensitive).
     * @throws ActionNotCompletedException when a database error occurs.
     */
    List<Song> filterSong(String partialInput, int limit, String attributeField) throws ActionNotCompletedException;

    List<Song> getSongsByPartialAlbum(String partialAlbum, int limit) throws ActionNotCompletedException;

    List<Song> getSongsByPartialAlbum(String partialAlbum) throws ActionNotCompletedException;

    List<Song> getSongsByPartialTitle(String partialTitle, int limit) throws ActionNotCompletedException;

    List<Song> getSongsByPartialTitle(String partialTitle) throws ActionNotCompletedException;

    List<Song> getSongsByPartialArtist(String partialArtist, int limit) throws ActionNotCompletedException;

    List<Song> getSongsByPartialArtist(String partialArtist) throws ActionNotCompletedException;

    /**
     * It's an Analytic function.
     * @return the album with the highest average of rating for every decade.
     * @throws ActionNotCompletedException when a database error occurs.
     */
    List<Pair<Integer, Pair<Album, Double>>> findTopRatedAlbumPerDecade() throws ActionNotCompletedException;

    /**
     * It's an Analytic function.
     * @param hitLimit the threshold to consider a song as a hit.
     * @param maxNumber the max number of artists you want to return.
     * @return artists which made the highest number of “hit songs”. A song is a “hit” if it received more than hitLimit likes.
     * @throws ActionNotCompletedException when a database error occurs.
     */
    List<Pair<String, Integer>> findArtistsWithMostNumberOfHit(int hitLimit, int maxNumber) throws ActionNotCompletedException;

    /**
     * It's an Analytic function.
     * @return songs that received more likes in the current day.
     * @param limit Maximum number of songs to return.
     * @throws ActionNotCompletedException when a database error occurs.
     */
    List<Song> getHotSongs(int limit) throws  ActionNotCompletedException;

    /**
     * Update the song Document in MongoDb incrementing the likeCount field.
     * @param song the song you want to update.
     * @throws ActionNotCompletedException when a database error occurs.
     */
    void incrementLikeCount(Song song) throws ActionNotCompletedException;

    /**
     * Update the song Document in MongoDb decrementing the likeCount field.
     * @param song the song you want to update.
     * @throws ActionNotCompletedException when a database error occurs.
     */
    void decrementLikeCount(Song song) throws ActionNotCompletedException;

    /**
     * @return the number of songs present in the application.
     */
    int getTotalSongs();

    /**
     * @param song the song you want to delete.
     */
    void deleteSong(Song song) throws ActionNotCompletedException, IllegalArgumentException;

    void deleteSongDocument(Song mongoSong);
}
