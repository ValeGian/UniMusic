package it.unipi.dii.inginf.lsmdb.unimusic.middleware.dao;

import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.*;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.exception.ActionNotCompletedException;

import java.util.List;

/**
 * Provides CRUD operations for Users
 */
public interface UserDAO {

    public void createUser(User user) throws ActionNotCompletedException;

    public User getUserByUsername(String username) throws ActionNotCompletedException;

    public List<User> getSuggestedUsers(User user) throws ActionNotCompletedException;

    public List<User> getSuggestedUsers(User user, int limit) throws ActionNotCompletedException;

    public boolean checkUserPassword(String username, String password);

    public void followUser(User userFollowing, User userFollowed) throws ActionNotCompletedException;

    public void unfollowUser(User userFollowing, User userFollowed) throws ActionNotCompletedException;

    public void followPlaylist(User user, Playlist playlist) throws ActionNotCompletedException;

    public void unfollowPlaylist(User user, Playlist playlist) throws ActionNotCompletedException;

    public void likeSong(User user, Song song) throws ActionNotCompletedException;

    public boolean userLikesSong(User user, Song song);

    public void deleteLike(User user, Song song) throws ActionNotCompletedException;

    public void updateUserPrivilegeLevel(User user, PrivilegeLevel newPrivLevel) throws ActionNotCompletedException;

}
