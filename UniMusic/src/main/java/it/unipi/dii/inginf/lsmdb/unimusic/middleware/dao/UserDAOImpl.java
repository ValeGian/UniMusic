package it.unipi.dii.inginf.lsmdb.unimusic.middleware.dao;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.*;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.exception.ActionNotCompletedException;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.log.UMLogger;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.persistence.mongoconnection.MongoDriver;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.persistence.mongoconnection.Collections;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.persistence.neo4jconnection.Neo4jDriver;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.neo4j.driver.*;
import org.neo4j.driver.exceptions.Neo4jException;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.*;
import static org.neo4j.driver.Values.parameters;

public class UserDAOImpl implements UserDAO{
    private static final Logger logger = UMLogger.getUserLogger();

    public static void main(String[] args) {

        User user1 = new User("valegiann", "root", "Valerio", "Giannini", 22);
        User user2 = new User("aleserra", "root", "Alessio", "Serra", 22);
        User user3 = new User("loreBianchi", "root", "Lorenzo", "Bianchi", 22);
        UserDAO userDAO = new UserDAOImpl();

        try {
            userDAO.createUser(user1);
        } catch (ActionNotCompletedException e) {
            e.printStackTrace();
        }
        /*try {
            userDAO.createUser(user1);
            userDAO.createUser(user2);
            userDAO.createUser(user3);

        } catch (ActionNotCompletedException e) {
            if (e.getCode() == 11000) {
                logger.error("You are trying to insert a document with *duplicate*  _id: " + user1.getUsername());
                e.printStackTrace();
            } else {
                logger.error("Some error while inserting document with  _id: ");
                e.printStackTrace();
            }
        }*/

        /*try( Session session = Neo4jDriver.getInstance().getDriver().session()) {
            List<String> lista = session.readTransaction((TransactionWork<List<String>>) tx -> {
               Result result = tx.run("MATCH (a:User) RETURN a.username as username");
                ArrayList<String> users = new ArrayList<>();
                while ((result.hasNext())){
                    Record r = result.next();
                    users.add(r.get("username").asString());
                }
                return users;
            });
            logger.info(lista);
        }*/

        /*try (MongoCursor<Document> cursor = MongoDriver.getInstance().getCollection(Collections.USERS).find().iterator()) {
            while (cursor.hasNext()) {
                logger.info("/" + cursor.next().get("boh") + "/");
            }
        }
         */

        Neo4jDriver.getInstance().closeDriver();
    }

    @Override
    public void createUser(User user) throws ActionNotCompletedException{
        try {
            createUserDocument(user);
            createUserNode(user);

            Playlist playlist = new Playlist(user.getUsername(), "Favourites");
            PlaylistDAO playlistDAO = new PlaylistDAOImpl();
            playlistDAO.createPlaylist(playlist);

            logger.info("Created user <" +user.getUsername()+ ">");

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
        User user = null;

        try (MongoCursor<Document> cursor =
                     MongoDriver.getInstance().getCollection(Collections.USERS).find(eq("_id", username)).iterator()) {
            if (cursor.hasNext()) {
                user = new User(cursor.next());
            }
        } catch (MongoException mEx) {
            logger.warn(mEx.getMessage());
            throw new ActionNotCompletedException(mEx);
        }
        return user;
    }

    @Override
    public boolean checkUserExists(String username) {
        try (MongoCursor<Document> cursor = MongoDriver.getInstance().getCollection(Collections.USERS)
                .find(eq("_id", username)).iterator()) {
            if (cursor.hasNext()) {
                return true;
            }
        } catch (MongoException mEx) {
            return false;
        }
        return false;
    }

    @Override
    public boolean checkUserPassword(String username, String password) {
        try (MongoCursor<Document> cursor = MongoDriver.getInstance().getCollection(Collections.USERS)
                .find(eq("_id", username)).iterator()) {
            if (cursor.hasNext())
               if(password.equals(cursor.next().get("password").toString()))
                   return true;
        } catch (MongoException mEx) {
            return false;
        }
        return false;
    }

    @Override
    public void followUser(User userFollowing, User userFollowed) throws ActionNotCompletedException {
        try (Session session = Neo4jDriver.getInstance().getDriver().session()) {
            session.run("MATCH (following:User { username: $following }) "
                            + "MATCH (followed:User { username: $followed }) "
                            + "MERGE (following)-[:FOLLOWS_USER]->(followed)",
                    parameters("following", userFollowing.getUsername(), "followed", userFollowed.getUsername())
            );
            logger.info("User <" + userFollowing.getUsername() + "> follows user <" + userFollowed.getUsername() + ">");
        } catch (Neo4jException n4jEx) {
            logger.error(n4jEx.getMessage());
            throw new ActionNotCompletedException(n4jEx);
        }
    }

    @Override
    public void unfollowUser(User userFollowing, User userFollowed) throws ActionNotCompletedException {
        try (Session session = Neo4jDriver.getInstance().getDriver().session()) {
            session.run(
                    "MATCH (:User { username: $username1 })-[f:FOLLOWS_USER]->(:User { username: $username2 }) "
                            + "DELETE f",
                    parameters("username1", userFollowing.getUsername(), "username2", userFollowed.getUsername())
            );
            logger.info("Deleted user <" +userFollowing.getUsername()+ "> follows user <" +userFollowed.getUsername()+ ">");
        } catch (Neo4jException n4jEx) {
            logger.error(n4jEx.getMessage());
            throw new ActionNotCompletedException(n4jEx);
        }
    }

    @Override
    public void followPlaylist(User user, Playlist playlist) throws ActionNotCompletedException {
        try (Session session = Neo4jDriver.getInstance().getDriver().session()) {
            session.run(
                    "MATCH (following:User { username: $following }) "
                            + "MATCH (followed:Playlist { playlistId: $followed }) "
                            + "MERGE (following)-[:FOLLOWS_PLAYLIST]->(followed)",
                    parameters("following", user.getUsername(), "followed", playlist.getID())
            );
            logger.info("User <" +user.getUsername()+ "> follows playlist <" +playlist.getID()+ ">");
        } catch (Neo4jException n4jEx) {
            logger.error(n4jEx.getMessage());
            throw new ActionNotCompletedException(n4jEx);
        }
    }

    @Override
    public void unfollowPlaylist(User user, Playlist playlist) throws ActionNotCompletedException {
        try (Session session = Neo4jDriver.getInstance().getDriver().session()) {
            session.run(
                    "MATCH (:User { username: $username })-[f:FOLLOWS_PLAYLIST]->(:Playlist { playlistId: $playlistId }) "
                            + "DELETE f",
                    parameters("username", user.getUsername(), "playlistId", playlist.getID())
            );
            logger.info("Deleted user <" +user.getUsername()+ "> follows playlist <" +playlist.getID()+ ">");
        } catch (Neo4jException n4jEx) {
            logger.error(n4jEx.getMessage());
            throw new ActionNotCompletedException(n4jEx);
        }
    }

    @Override
    public void likeSong(User user, Song song) throws ActionNotCompletedException {
        try (Session session = Neo4jDriver.getInstance().getDriver().session()) {
            session.run(
                    "MATCH (u:User { username: $username }) "
                    + "MATCH (s:Song { songId: $songId }) "
                    + "MERGE (u)-[:LIKES {day: date()}]->(s)",
                    parameters("username", user.getUsername(), "songId", song.getID())
            );
            logger.info("User <" +user.getUsername()+ "> likes song <" +song.getTitle()+ ">");
        } catch (Neo4jException n4jEx) {
            logger.error(n4jEx.getMessage());
            throw new ActionNotCompletedException(n4jEx);
        }
    }

    @Override
    public void deleteLike(User user, Song song) throws ActionNotCompletedException {
        try (Session session = Neo4jDriver.getInstance().getDriver().session()) {
            session.run(
                    "MATCH (:User { username: $username })-[l:LIKES]->(:Song { songId: $songId }) "
                            + "DELETE l",
                    parameters("username", user.getUsername(), "songId", song.getID())
            );
            logger.info("Deleted user <" +user.getUsername()+ "> likes song <" +song.getTitle()+ ">");
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

    //--------------------------PACKAGE-----------------------------------------------------------

    void addPlaylistToUserDocument(User user, Playlist playlist)  throws MongoException{
        Document playlistDoc = playlist.toBsonDocument();

        MongoCollection<Document> userColl = MongoDriver.getInstance().getCollection(Collections.USERS);
        userColl.updateOne(
                eq("_id", user.getUsername()),
                addToSet("createdPlaylists", playlistDoc)
        );
    }

    //--------------------------PRIVATE------------------------------------------------------------

    private void createUserDocument(User user) throws MongoException {
        Document userDoc = user.toBsonDocument();

        MongoCollection<Document> userColl = MongoDriver.getInstance().getCollection(Collections.USERS);
        userColl.insertOne(userDoc);
    }

    private void createUserNode(User user) throws Neo4jException {
        try (Session session = Neo4jDriver.getInstance().getDriver().session()) {
            session.run(
                    "MERGE (a:User {username: $username})",
                    parameters("username", user.getUsername()));
        }
    }

    private void updateUserDocument(User user) throws MongoException {
        MongoCollection<Document> userColl = MongoDriver.getInstance().getCollection(Collections.USERS);
        userColl.updateOne(
                eq("_id", user.getUsername()),
                combine(
                        set("firstName", user.getFirstName()),
                        set("lastName", user.getLastName()),
                        set("age", user.getAge()),
                        set("privilegeLevel", user.getPrivilegeLevel().toString())
                ));
    }

    private void deleteUserDocument(User user) throws MongoException {
        MongoCollection<Document> userColl = MongoDriver.getInstance().getCollection(Collections.USERS);
        userColl.deleteOne(eq("_id", user.getUsername()));
    }
}
