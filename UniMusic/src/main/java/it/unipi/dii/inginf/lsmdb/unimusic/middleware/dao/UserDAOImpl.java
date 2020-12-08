package it.unipi.dii.inginf.lsmdb.unimusic.middleware.dao;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.*;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.exception.ActionNotCompletedException;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.log.UMLogger;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.persistence.mongoconnection.*;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.persistence.neo4jconnection.Labels;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.persistence.neo4jconnection.Neo4jDriver;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.persistence.neo4jconnection.UserProperties;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.neo4j.driver.Session;
import org.neo4j.driver.exceptions.Neo4jException;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.*;
import static org.neo4j.driver.Values.parameters;

public class UserDAOImpl implements UserDAO{
    private static final Logger logger = UMLogger.getUserLogger();

    public static void main(String[] args) {

        User user = new User("valegiann", "root", "Valerio", "Giannini", 22);
        UserDAO userDAO = new UserDAOImpl();

        try {
            userDAO.createUser(user);
        } catch (ActionNotCompletedException e) {
            if (e.getCode() == 11000) {
                logger.error("You are trying to insert a document with *duplicate*  _id: " + user.getUsername());
            } else {
                logger.error("Some error while inserting document with  _id: ");
            }
        }

        try {
            userDAO.updateUserPrivilegeLevel(user, PrivilegeLevel.ADMIN);
        } catch (ActionNotCompletedException ancEx) {
            ancEx.printStackTrace();
        }

        Neo4jDriver.getInstance().closeDriver();
    }

    @Override
    public void createUser(User user) throws ActionNotCompletedException{
        try {
            createUserDocument(user);
            createUserNode(user);
            logger.info("Created user <" +user.getUsername()+ ">");

            PlaylistDAO playlistDAO = new PlaylistDAOImpl();
            playlistDAO.createFavourite(user);
            logger.info("Created favourite playlist of user <" +user.getUsername()+ ">");

        } catch (ActionNotCompletedException ancEx) {
            logger.error(ancEx.getMessage());
            throw new ActionNotCompletedException(ancEx);
        } catch (MongoException mEx) {
            logger.error(mEx.getMessage());
            throw new ActionNotCompletedException(mEx, mEx.getCode());
        } catch (Neo4jException n4jEx) {
            logger.error(n4jEx.getMessage());
            try {
                deleteUserDocument(user);
                throw new ActionNotCompletedException(n4jEx);
            } catch (MongoException mEx) {
                logger.error(mEx.getMessage());
                throw new ActionNotCompletedException(mEx);
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
    public void updateUserPrivilegeLevel(User user, PrivilegeLevel newPrivLevel)  throws ActionNotCompletedException{
        user.setPrivilegeLevel(newPrivLevel);
        try {
            updateUserDocument(user);
            logger.info("Updated privilege level of user <" +user.getUsername()+ "> : new level <" +user.getPrivilegeLevel()+ ">");
        } catch (MongoException mEx) {
            logger.warn(mEx.getMessage());
            throw new ActionNotCompletedException(mEx);
        }
    }

    //---------------------------------------------------------------------------------------------

    private void createUserDocument(User user) throws MongoException {
        Document userDoc = new Document(UserFields.USERNAME.toString(), user.getUsername())
                .append(UserFields.PASSWORD.toString(), user.getPassword())
                .append(UserFields.FIRST_NAME.toString(), user.getFirstName())
                .append(UserFields.LAST_NAME.toString(), user.getLastName())
                .append(UserFields.AGE.toString(), user.getAge())
                .append(UserFields.PRIVILEGE_LEVEL.toString(), user.getPrivilegeLevel().toString());

        MongoCollection<Document> userColl = MongoDriver.getInstance().getCollection(Collections.USERS);
        userColl.insertOne(userDoc);
    }

    private void createUserNode(User user) throws Neo4jException {
        try (Session session = Neo4jDriver.getInstance().getDriver().session()) {
            session.run(
                    "MERGE (a:" +Labels.USER+ " {" +UserProperties.USERNAME+ ": $username})",
                    parameters(UserProperties.USERNAME, user.getUsername()));
        }
    }

    private void updateUserDocument(User user) throws MongoException {
        MongoCollection<Document> userColl = MongoDriver.getInstance().getCollection(Collections.USERS);
        userColl.updateOne(
                eq(UserFields.USERNAME.toString(), user.getUsername()),
                combine(
                        set(UserFields.FIRST_NAME.toString(), user.getFirstName()),
                        set(UserFields.LAST_NAME.toString(), user.getLastName()),
                        set(UserFields.AGE.toString(), user.getAge()),
                        set(UserFields.PRIVILEGE_LEVEL.toString(), user.getPrivilegeLevel().toString())
                ));
    }

    private void deleteUserDocument(User user) throws MongoException {
        MongoCollection<Document> userColl = MongoDriver.getInstance().getCollection(Collections.USERS);
        userColl.deleteOne(eq(UserFields.USERNAME.toString(), user.getUsername()));
    }
}
