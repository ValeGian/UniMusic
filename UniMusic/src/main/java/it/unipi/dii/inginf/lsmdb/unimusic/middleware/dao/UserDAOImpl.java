package it.unipi.dii.inginf.lsmdb.unimusic.middleware.dao;

import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.Playlist;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.User;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.exception.ActionNotCompletedException;

public class UserDAOImpl implements UserDAO{

    @Override
    public void createUser(User user)  throws ActionNotCompletedException{

    }

    @Override
    public User getUser(String username)  throws ActionNotCompletedException{
        return null;
    }

    @Override
    public void addPlaylist(User user, Playlist playlist)  throws ActionNotCompletedException{

    }

    @Override
    public void updateUser(User user)  throws ActionNotCompletedException{

    }

    //---------------------------------------------------------------------------------------------

    private void createUserDocument(User user) {

    }

    private void createUserNode(User user) {

    }
}
