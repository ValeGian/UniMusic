package it.unipi.dii.inginf.lsmdb.unimusic.middleware.dao;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.Playlist;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.User;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.exception.ActionNotCompletedException;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.persistence.mongoconnection.Collections;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.persistence.mongoconnection.MongoDriver;
import org.neo4j.driver.exceptions.Neo4jException;

public class UserDAOImpl implements UserDAO{

    @Override
    public void createUser(User user)  throws ActionNotCompletedException{
        try {
            createUserDocument(user);

            createUserNode(user);
        } catch (MongoException mongoEx) {
            //loggo
            throw new ActionNotCompletedException(mongoEx);
        } catch (Neo4jException neoEx) {
            //loggo
            try {
                deleteUserDocument(user);
                throw new ActionNotCompletedException(neoEx);
            } catch (MongoException mongoEx) {
                //loggo
                throw new ActionNotCompletedException(mongoEx);
            }
        }
    }

    @Override
    public User getUser(String username)  throws ActionNotCompletedException{
        return null;
    }

    @Override
    public void addPlaylist(User user, Playlist playlist)  throws ActionNotCompletedException{

    }

    @Override
    public void followPlaylist(User user, Playlist playlist) throws ActionNotCompletedException {

    }

    @Override
    public void updateUser(User user)  throws ActionNotCompletedException{

    }

    //---------------------------------------------------------------------------------------------

    private void createUserDocument(User user) {
        MongoCollection userColl = MongoDriver.getInstance().getCollection(Collections.USERS);
    }

    private void createUserNode(User user) {

    }

    private void deleteUserDocument(User user) {

    }
}
