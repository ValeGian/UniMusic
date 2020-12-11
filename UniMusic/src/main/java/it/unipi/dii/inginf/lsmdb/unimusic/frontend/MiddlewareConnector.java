package it.unipi.dii.inginf.lsmdb.unimusic.frontend;

import it.unipi.dii.inginf.lsmdb.unimusic.middleware.dao.*;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.*;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.exception.ActionNotCompletedException;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.persistence.mongoconnection.MongoDriver;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.persistence.neo4jconnection.Neo4jDriver;

import java.util.ArrayList;
import java.util.List;

public class MiddlewareConnector {
    private static final MiddlewareConnector instance = new MiddlewareConnector();

    private final UserDAO userDAO = new UserDAOImpl();
    private final PlaylistDAO playlistDAO = new PlaylistDAOImpl();
    private final SongDAO songDAO = new SongDAOImpl();

    private User loggedUser = new User("");

    private MiddlewareConnector() {

    }

    public static MiddlewareConnector getInstance() { return instance; }

    public void closeApplication() {
        MongoDriver.getInstance().closeConnection();
        Neo4jDriver.getInstance().closeDriver();
    }

    //-----------------USER-------------------------------------------------------------------

    public void registerUser(String username,
                             String password,
                             String firstName,
                             String lastName,
                             int age) throws ActionNotCompletedException {
        User user = new User(username, password, firstName, lastName, age);
        userDAO.createUser(user);
    }

    public boolean loginUser(String username, String password) {
        if(userDAO.checkUserPassword(username, password)) {
            try {
                loggedUser = userDAO.getUserByUsername(username);
            } catch (ActionNotCompletedException e) {
                return false;
            }
            return true;
        }
        return false;
    }


    public void logout() {
        loggedUser = null;
    }

    public List<User> getSuggestedUsers() {
        List<User> suggUsers = new ArrayList<>();
        try {
            suggUsers = userDAO.getSuggestedUsers(loggedUser);
        } catch (ActionNotCompletedException ancEx) {
            return new ArrayList<User>();
        }
        return suggUsers;
    }

    //--------------------------SONG-------------------------------------------------------------------

    public List<Song> getHotSongs() {
        List<Song> hotSongs = new ArrayList<>();
        try {
            hotSongs = songDAO.getHotSongs();
        } catch (ActionNotCompletedException ancEx) {
            return new ArrayList<Song>();
        }
        return hotSongs;
    }

    public boolean userLikesSong(Song song){
       return userDAO.userLikesSong(loggedUser, song);

    }
}
