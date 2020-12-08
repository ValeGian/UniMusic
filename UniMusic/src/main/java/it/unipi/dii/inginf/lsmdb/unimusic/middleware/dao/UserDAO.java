package it.unipi.dii.inginf.lsmdb.unimusic.middleware.dao;

import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.Playlist;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.PrivilegeLevel;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.User;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.exception.ActionNotCompletedException;

/**
 * Provides CRUD operations for Users
 */
public interface UserDAO {

    public void createUser(User user) throws ActionNotCompletedException;

    public User getUser(String username) throws ActionNotCompletedException;

    public void addPlaylist(User user, Playlist playlist) throws ActionNotCompletedException;

    public void followPlaylist(User user, Playlist playlist) throws ActionNotCompletedException;

    public void updateUserPrivilegeLevel(User user, PrivilegeLevel newPrivLevel) throws ActionNotCompletedException;

}
