package it.unipi.dii.inginf.lsmdb.unimusic.middleware.dao;

import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.*;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.exception.ActionNotCompletedException;
import javafx.util.Pair;

import java.util.List;

/**
 * Provides CRUD and Analytic operations for Users
 */
public interface UserDAO {

    /** Creates a User in the databases
     * @param user user to be created
     * @throws ActionNotCompletedException when the construction fails in one of the databases
     */
    public void createUser(User user) throws ActionNotCompletedException;

    /** Gets informations about a user
     * @param username username of the user to be retrieve
     * @return User object containing all the informations of the user
     * @throws ActionNotCompletedException when the operation fails
     */
    public User getUserByUsername(String username) throws ActionNotCompletedException;

    /** Gets all users which name contains the partial username
     * @param partialUsername part of the usernames we want to retrieve
     * @return Users which username contains partialUsername
     * @throws ActionNotCompletedException when the operation fails
     */
    public List<User> getUserByPartialUsername(String partialUsername) throws ActionNotCompletedException;

    /** Gets suggestion for users
     * @param user user for which we want to get suggestions
     * @return user's suggested for the specific user passed as parameter
     * @throws ActionNotCompletedException when the operation fails
     */
    public List<User> getSuggestedUsers(User user) throws ActionNotCompletedException;

    /** Gets suggestion for users
     * @param user user for which we want to get suggestions
     * @param limit limits the number of users returned as result
     * @return user's suggested for the specific user passed as parameter
     * @throws ActionNotCompletedException when the operation fails
     */
    public List<User> getSuggestedUsers(User user, int limit) throws ActionNotCompletedException;

    /** Checks if the password is correct for the given user
     * @param username user we want to check on
     * @param password password we want to check
     * @return true if the password is correct, false otherwise
     */
    public boolean checkUserPassword(String username, String password);

    /** Create a FOLLOWS_USER relation between two given users
     * @param userFollowing user which starts following
     * @param userFollowed user which starts being followed
     * @throws ActionNotCompletedException when the operation fails
     */
    public void followUser(User userFollowing, User userFollowed) throws ActionNotCompletedException;

    /** Delete a FOLLOWS_USER relation between two given users
     * @param userFollowing user which used to follow
     * @param userFollowed user which used to be followed
     * @throws ActionNotCompletedException when the operation fails
     */
    public void unfollowUser(User userFollowing, User userFollowed) throws ActionNotCompletedException;

    /** Create a FOLLOWS_PLAYLIST relation between a user and a playlist
     * @param user user which starts to follow the playlist
     * @param playlist playlist which will be followed
     * @throws ActionNotCompletedException when the operation fails
     */
    public void followPlaylist(User user, Playlist playlist) throws ActionNotCompletedException;

    /** Delete a FOLLOWS_PLAYLIST relation between a user and a playlist
     * @param user user which used to follow the playlist
     * @param playlist playlist which used to be followed
     * @throws ActionNotCompletedException when the operation fails
     */
    public void unfollowPlaylist(User user, Playlist playlist) throws ActionNotCompletedException;

    /** Checks if a playlist is followed by a user
     * @param user user hypotetically following
     * @param playlist playlist hypotetically followed
     * @return true if user FOLLOWS_PLAYLIST playlist
     */
    public boolean isFollowingPlaylist(User user, Playlist playlist);

    /** Checks if a user is followed by another user
     * @param followed user being hypotetically followed
     * @param following user hypotetically following the other
     * @return true if following FOLLOWS_USER followed
     */
    public boolean isFollowedBy(User followed, User following);

    /** Create a LIKE relation between a user and a song
     * @param user user whici will start to like the song
     * @param song song which will start being liked
     * @throws ActionNotCompletedException when the operation fails
     */
    public void likeSong(User user, Song song) throws ActionNotCompletedException;

    /** Checks if a user likes a song
     * @param user user hypotetically liking the song
     * @param song song hypotetically being liked
     * @return yes if user LIKES song
     */
    public boolean userLikesSong(User user, Song song);

    /** Delete the LIKES relation between a user and a song
     * @param user user which used to like the song
     * @param song song which used to be liked
     * @throws ActionNotCompletedException when the operation fails
     */
    public void deleteLike(User user, Song song) throws ActionNotCompletedException;

    /** Updated the level of privilege of a user
     * @param user user which privilege level will be updated
     * @param newPrivLevel new privilege level for the user
     * @throws ActionNotCompletedException when the operation fails
     */
    public void updateUserPrivilegeLevel(User user, PrivilegeLevel newPrivLevel) throws ActionNotCompletedException;

    /** Gets all the playlists created by a specific user
     * @param user user which playlists we want to return
     * @return playlists created by the user
     * @throws ActionNotCompletedException when the operation fails
     */
    public List<Playlist> getAllPlaylist(User user) throws ActionNotCompletedException;

    /** Gets all the playlists followed by a specific user
     * @param user user which playlists we want to return
     * @return playlists followed by the user
     * @throws ActionNotCompletedException when the operation fails
     */
    public List<Playlist> getFollowedPlaylist(User user) throws ActionNotCompletedException;

  
    /** Gets favourite genres of all the users, ordered by popularity
     * @param numGenres how many genres we want to retrieve
     * @return ordered favourite genres, number of times the genre is inserted in the playlist
     * @throws ActionNotCompletedException when the operation fails
     */

    public List<Pair<String, Integer>> getFavouriteGenres(int numGenres) throws ActionNotCompletedException;

    /** Gets all the users followed by a specific user
     * @param user user which followed users we want to return
     * @return followed users of the user
     * @throws ActionNotCompletedException when the operation fails
     */
    public List<User> getFollowedUsers(User user) throws ActionNotCompletedException;

    /** Gets all the followers of a specific user
     * @param user user which followers we want to return
     * @return followers of the user
     * @throws ActionNotCompletedException when the operation fails
     */
    public List<User> getFollowers(User user) throws ActionNotCompletedException;


    /** Deletes a user and all his created playlists
     * @param user user we want to delete
     * @throws ActionNotCompletedException when the operation fails
     */
    public void deleteUser(User user) throws ActionNotCompletedException;
}
