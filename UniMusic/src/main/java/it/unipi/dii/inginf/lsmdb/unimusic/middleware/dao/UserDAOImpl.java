package it.unipi.dii.inginf.lsmdb.unimusic.middleware.dao;

import com.mongodb.MongoException;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.*;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.exception.ActionNotCompletedException;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.log.UMLogger;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.persistence.mongoconnection.*;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.persistence.mongoconnection.Collections;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.persistence.mongoconnection.MongoDriver;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.persistence.neo4jconnection.Neo4jDriver;
import org.neo4j.driver.Session;
import org.neo4j.driver.exceptions.Neo4jException;
import org.bson.Document;
import org.apache.log4j.*;
import static org.neo4j.driver.Values.parameters;

public class UserDAOImpl implements UserDAO{

    public static void main(String[] args) {
        Logger logger = UMLogger.getUserLogger();

        User user = new User("valegiann", "root", "Valerio", "Giannini", 22);
        UserDAO userDAO = new UserDAOImpl();

        try {
            userDAO.createUser(user);
            logger.info("Inserito " + user.getUsername());
        } catch (ActionNotCompletedException e) {
            if (e.getCode() == 11000) {
                logger.error("You are trying to insert a document with *duplicate*  _id: " + user.getUsername());
            }
            else {
                logger.error("Some error while inserting document with  _id: ");
            System.out.println("> Inserito " + user.getUsername());
        } catch (ActionNotCompletedException e) {
            if (e.getCode() == 11000) {
                System.out.println("You are trying to insert a document with *duplicate*  _id: " + user.getUsername());
            }
            else {
                System.out.println("Some error while inserting document with  _id: ");
            }
        }
    }

    @Override
    public void createUser(User user)  throws ActionNotCompletedException{
        try {
            createUserDocument(user);

            createUserNode(user);
        } catch (MongoException mongoEx) {
            //loggo
            throw new ActionNotCompletedException(mongoEx, mongoEx.getCode());
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

    private void createUserDocument(User user) throws MongoException {
        Document userDoc = new Document("_id", user.getUsername())
                .append("password", user.getPassword())
                .append("firstName", user.getFirstName())
                .append("lastName", user.getLastName())
                .append("age", user.getAge())
                .append("privilegeLevel", user.getPrivilegeLevel().toString());

        MongoCollection userColl = MongoDriver.getInstance().getCollection(Collections.USERS);
        userColl.insertOne(userDoc);
    }

    private void createUserNode(User user) throws Neo4jException {
        try (Session session = Neo4jDriver.getInstance().getDriver().session()) {
            session.run("MERGE (a:User {username: $username})", parameters("username", user.getUsername()));
        }
    }

    private void deleteUserDocument(User user) {

    }
}
