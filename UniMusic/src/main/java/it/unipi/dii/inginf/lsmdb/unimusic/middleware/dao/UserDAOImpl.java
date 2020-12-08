package it.unipi.dii.inginf.lsmdb.unimusic.middleware.dao;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.*;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.exception.ActionNotCompletedException;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.log.UMLogger;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.persistence.mongoconnection.*;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.persistence.neo4jconnection.*;
import org.apache.log4j.Logger;
import org.bson.BsonArray;
import org.bson.Document;
import org.neo4j.driver.*;
import org.neo4j.driver.exceptions.Neo4jException;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.*;
import static org.neo4j.driver.Values.parameters;

public class UserDAOImpl implements UserDAO{
    private static final Logger logger = UMLogger.getUserLogger();

    public static void main(String[] args) {

        User user1 = new User("valegiann", "root", "Valerio", "Giannini", 22);
        User user2 = new User("aleserra", "root", "Alessio", "Serra", 22);
        UserDAO userDAO = new UserDAOImpl();

        try {
            userDAO.createUser(user1);
            userDAO.createUser(user2);
            userDAO.followUser(user1, user2);

        } catch (ActionNotCompletedException e) {
            if (e.getCode() == 11000) {
                logger.error("You are trying to insert a document with *duplicate*  _id: " + user1.getUsername());
                e.printStackTrace();
            } else {
                logger.error("Some error while inserting document with  _id: ");
                e.printStackTrace();
            }
        }

        try( Session session = Neo4jDriver.getInstance().getDriver().session()) {
            List<String> lista = session.readTransaction((TransactionWork<List<String>>) tx -> {
               Result result = tx.run("MATCH (a:User) RETURN a.username as username");
                ArrayList<String> users = new ArrayList<>();
                while ((result.hasNext())){
                    Record r = result.next();
                    users.add(r.get("username").asString());
                }
                return users;
            });
            System.out.println(lista);
        }

        Neo4jDriver.getInstance().closeDriver();
    }

    @Override
    public void createUser(User user) throws ActionNotCompletedException{
        try {
            createUserDocument(user);
            createUserNode(user);
            logger.info("Created user <" +user.getUsername()+ ">");

        } catch (ActionNotCompletedException ancEx) {
            logger.error(ancEx.getMessage());
            throw new ActionNotCompletedException(ancEx.getMessage());
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
    public User getUserByUsername(String username)  throws ActionNotCompletedException{
        return null;
    }

    @Override
    public void addCreatedPlaylist(User user, Playlist playlistCreated)  throws ActionNotCompletedException{
        try {
            addPlaylistToUserDocument(user, playlistCreated);
            logger.info("Playlist <" +playlistCreated.getName()+ "> created by user <" + user.getUsername() + ">");
        } catch (MongoException mEx) {
            logger.error(mEx.getMessage());
            throw new ActionNotCompletedException(mEx);
        }
    }

    @Override
    public void followUser(User userFollowing, User userFollowed) throws ActionNotCompletedException {
        try (Session session = Neo4jDriver.getInstance().getDriver().session()) {
            session.run("MATCH (following:" +Labels.USER+ ") WHERE following." +UserProperties.USERNAME+ " = $following "
                            + "MATCH (followed:" +Labels.USER+ ") WHERE followed." +UserProperties.USERNAME+ " = $followed "
                            + "CREATE (following)-[:" +RelationshipTypes.FOLLOW_USER+ "]->(followed)",
                    parameters("following", userFollowing.getUsername(), "followed", userFollowed.getUsername())
            );
            logger.info("User <" + userFollowing.getUsername() + "> follows user <" + userFollowed.getUsername() + ">");
        } catch (Neo4jException n4jEx) {
            logger.error(n4jEx.getMessage());
            throw new ActionNotCompletedException(n4jEx);
        }
    }

    @Override
    public void followPlaylist(User user, Playlist playlist) throws ActionNotCompletedException {
        try (Session session = Neo4jDriver.getInstance().getDriver().session()) {
            session.run("MATCH (following:" +Labels.USER+ ") WHERE following." +UserProperties.USERNAME+ " = $following "
                            + "MATCH (followed:" +Labels.PLAYLIST+ ") WHERE followed." + PlaylistProperties.ID + " = $followed "
                            + "CREATE (following)-[:" +RelationshipTypes.FOLLOW_PLAYLIST+ "]->(followed)",
                    parameters("following", user.getUsername(), "followed", playlist.getID())
            );
            logger.info("User <" + user.getUsername() + "> follows playlist <" + playlist.getID() + ">");
        } catch (Neo4jException n4jEx) {
            logger.error(n4jEx.getMessage());
            throw new ActionNotCompletedException(n4jEx);
        }
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

    private void createUserDocument(User user) throws MongoException, ActionNotCompletedException {
        Document userDoc = user.toBsonDocument();

        MongoCollection<Document> userColl = MongoDriver.getInstance().getCollection(Collections.USERS);
        userColl.insertOne(userDoc);

        Playlist favouritePlaylist = new Playlist(user.getUsername(), "Favourites");
        addPlaylistToUserDocument(user, favouritePlaylist);
    }

    private void createUserNode(User user) throws Neo4jException {
        try (Session session = Neo4jDriver.getInstance().getDriver().session()) {
            session.run(
                    "MERGE (a:" +Labels.USER+ " {" +UserProperties.USERNAME+ ": $username})",
                    parameters("username", user.getUsername()));
        }
    }

    private void addPlaylistToUserDocument(User user, Playlist playlist) throws MongoException {

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
