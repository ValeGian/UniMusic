package it.unipi.dii.inginf.lsmdb.unimusic.frontend;

import it.unipi.dii.inginf.lsmdb.unimusic.middleware.dao.*;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.*;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.exception.ActionNotCompletedException;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.persistence.mongoconnection.MongoDriver;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.persistence.neo4jconnection.Neo4jDriver;

public class MiddlewareConnector {
    private static final MiddlewareConnector instance = new MiddlewareConnector();

    private final UserDAO userDAO = new UserDAOImpl();
    private final PlaylistDAO playlistDAO = new PlaylistDAOImpl();
    private final SongDAO songDAO = new SongDAOImpl();

    private User loggedUser;

    private MiddlewareConnector() {

    }

    public static MiddlewareConnector getInstance() { return instance; }

    public void closeApplication() {
        MongoDriver.getInstance().closeConnection();
        Neo4jDriver.getInstance().closeDriver();
    }

    public boolean registerUser(String username,
                             String password,
                             String firstName,
                             String lastName,
                             int age) {
        if(userDAO.checkUserExists(username))
            return false;

        User user = new User(username, password, firstName, lastName, age);
        try {
            userDAO.createUser(user);
        } catch (ActionNotCompletedException e) {
            return false;
        }

        return true;
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
}
