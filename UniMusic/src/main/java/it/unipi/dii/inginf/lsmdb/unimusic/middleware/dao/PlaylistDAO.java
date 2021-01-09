package it.unipi.dii.inginf.lsmdb.unimusic.middleware.dao;


import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.Playlist;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.Song;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.User;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.exception.ActionNotCompletedException;

import java.util.List;

/**
 * Provides CRUD operations and Analytics for Playlists
 */
public interface PlaylistDAO {

    /** Creates a Playlist in the the databases
     * @param playlist playlist to be created
     * @throws ActionNotCompletedException when the construction fails in one of the databases
     */
    void createPlaylist(Playlist playlist) throws ActionNotCompletedException;

    /** Gets information about a playlist
     * @param playlistID the id of the playlist to retrieve
     * @return Playlist object containing all the information about the playlist
     * @throws ActionNotCompletedException when the operation fails
     */
    Playlist getPlaylist(String playlistID) throws ActionNotCompletedException;

    /** Gets the information about the favourite playlist of a user
     * @param user user of which we want to retrieve the favourite playlist
     * @return Playlist object containing the information of the favourite playlist of the user
     * @throws ActionNotCompletedException when the operation fails
     */
    Playlist getFavourite(User user) throws ActionNotCompletedException;

    /** Adds a song to a playlist
     * @param playlist playlist that we want to update
     * @param song song to add
     * @throws ActionNotCompletedException when the operation fails
     */
    void addSong(Playlist playlist, Song song) throws ActionNotCompletedException;

    /** Deletes a song from a playlist
     * @param playlist playlist tha we want to update
     * @param song song to delete
     * @throws ActionNotCompletedException when the operation fails
     */
    void deleteSong(Playlist playlist, Song song) throws ActionNotCompletedException;

    /** Adds a song to the favourite playlist of a user
     * @param user user of which we want to update the favourite playlist
     * @param song song to add
     * @throws ActionNotCompletedException when the operation fails
     */
    void addSongToFavourite(User user, Song song) throws ActionNotCompletedException;

    /** Deletes a song from the favourite playlist of a user
     * @param user user of which we want to update the favourite playlist
     * @param song song to delete
     * @throws ActionNotCompletedException when the operation fails
     */
    void deleteSongFromFavourite(User user, Song song) throws ActionNotCompletedException;

    /** Deletes a playlist
     * @param playlist playlist to delete
     * @throws ActionNotCompletedException when the operation fails
     */
    void deletePlaylist(Playlist playlist) throws ActionNotCompletedException;


    /** Update the name of a Playlist
     * @param playlistId id of the playlist to rename
     * @param newName new name of the playlist
     * @throws ActionNotCompletedException
     */
    void updatePlaylistName(String playlistId, String newName) throws ActionNotCompletedException;

    /** Checks if the song is the favourite of the user
     * @param user
     * @param song
     * @return true if the song is int the favourite playlist of the user
     */
    boolean isSongFavourite(User user, Song song);

    /** Gets all the songs contained in a playlist
     * @param playlist playlist of which we want to retrieve the songs
     * @return the list of songs of the playlist
     * @throws ActionNotCompletedException when the operation fails
     */
    List<Song> getAllSongs(Playlist playlist) throws ActionNotCompletedException;

    /** It's an Analytic function
     *  Gets the suggested playlists of a user
     * @param user user of which we want to retrieve suggested playlists
     * @return the list of suggested playlists
     * @throws ActionNotCompletedException when the operation fails
     */
    List<Playlist> getSuggestedPlaylists(User user) throws ActionNotCompletedException;

    /** It's an Analytic function
     *  Gets the suggested playlists of a user
     * @param user user of which we want to retrieve suggested playlists
     * @param limit max number of suggested playlists to return
     * @return the list of suggested playlists
     * @throws ActionNotCompletedException when the operation fails
     */
    List<Playlist> getSuggestedPlaylists(User user, int limit) throws ActionNotCompletedException;

    /** Deletes a playlist from the MongoDB database
     * @param playlist playlist we want to delete
     * @throws ActionNotCompletedException when the operation fails
     */
    void deletePlaylistDocument(Playlist playlist) throws ActionNotCompletedException;

    /** Gets the number of playlists saved on the databases
     * @return the number of playlists saved on the databases
     */
    int getTotalPlaylists();

}
