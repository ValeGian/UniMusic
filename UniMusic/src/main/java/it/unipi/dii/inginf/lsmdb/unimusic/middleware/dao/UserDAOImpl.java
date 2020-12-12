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
import javafx.util.Pair;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.neo4j.driver.*;
import org.neo4j.driver.exceptions.Neo4jException;

import java.util.ArrayList;

import java.util.Arrays;
import java.util.List;

import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.*;
import static com.mongodb.client.model.Updates.*;
import static org.neo4j.driver.Values.parameters;
import static com.mongodb.client.model.Sorts.*;

public class UserDAOImpl implements UserDAO{
    private static final Logger logger = UMLogger.getUserLogger();

    public static void main(String[] args) throws ActionNotCompletedException {

        UserDAO userDAO = new UserDAOImpl();
        SongDAO songDAO = new SongDAOImpl();

        User user1 = new User("JasHof1981");
        User user2 = new User("AlePai1987");
        User user3 = new User("RogRog1992");
        User user4 = new User("LarHan1998");
        User user5 = new User("FraRey1989");
        User user6 = new User("TimWei1994");
        User user7 = new User("EliTur1990");
        User user8 = new User("MonFri1978");
        User user9 = new User("MorTan1999");

        List<User> users = new ArrayList<>();
        users.add(user1);
        users.add(user2);
        users.add(user3);
        users.add(user4);
        users.add(user5);
        users.add(user6);
        users.add(user7);
        users.add(user8);
        users.add(user9);

        User userFinto = new User("Boh");

        Song song1 = new Song(); song1.setID("5fd3548a9ab2a47028229e76");
        Song song2 = new Song(); song2.setID("5fd354849ab2a47028229e74");
        Song song3 = new Song(); song3.setID("5fd3546d9ab2a47028229e70");
        Song song4 = new Song(); song4.setID("5fd354879ab2a47028229e75");
        Song song5 = new Song(); song5.setID("5fd354759ab2a47028229e71");
        Song song6 = new Song(); song6.setID("5fd354a49ab2a47028229e7c");
        Song song7 = new Song(); song7.setID("5fd354989ab2a47028229e79");
        Song song8 = new Song(); song8.setID("5fd3549b9ab2a47028229e7a");
        Song song9 = new Song(); song9.setID("5fd354a79ab2a47028229e7d");

        Song songFinta = new Song(); songFinta.setID("Boh");

        /*
        Song songFinta = new Song(); songFinta.setID("Boh");

        userDAO.likeSong(user1, song2);
        System.out.println(userDAO.userLikesSong(user1, song2));
        userDAO.deleteLike(user1, song2);
        System.out.println(userDAO.userLikesSong(user1, song2));

        /*
        for(User uno: users) {
            List<User> suggUsers = userDAO.getSuggestedUsers(uno);
            System.out.println("USER________________");
            for (User user : suggUsers) {
                System.out.println(user.getUsername());
            }
        }
         */

        /*
        userDAO.followUser(user1, user2);
        userDAO.followUser(user1, user3);
        userDAO.followUser(user1, user4);
        userDAO.followUser(user1, user5);
        userDAO.followUser(user2, user6);
        userDAO.followUser(user2, user7);
        userDAO.followUser(user2, user8);
        userDAO.followUser(user2, user9);
        userDAO.followUser(user2, user5);
        userDAO.followUser(user3, user1);
        userDAO.followUser(user3, user2);
        userDAO.followUser(user3, user4);
        userDAO.followUser(user4, user5);
         */
        /*
        userDAO.likeSong(user1, song1);
        userDAO.likeSong(user1, song2);
        userDAO.likeSong(user1, song3);
        userDAO.likeSong(user1, song4);
        userDAO.likeSong(user1, song5);
        userDAO.likeSong(user1, song6);
        userDAO.likeSong(user1, song7);
        userDAO.likeSong(user1, song8);
        userDAO.likeSong(user2, song9);
        userDAO.likeSong(user2, song1);
        userDAO.likeSong(user2, song3);
        userDAO.likeSong(user3, song9);
        userDAO.likeSong(user4, song2);
        userDAO.likeSong(user5, song3);
        userDAO.likeSong(user5, song4);
        userDAO.likeSong(user6, song5);
        userDAO.likeSong(user7, song6);
        userDAO.likeSong(user8, song7);
        userDAO.likeSong(user9, song8);
        userDAO.likeSong(user9, song9);
         */
        /*
        User user1 = new User("valegiann", "root", "Valerio", "Giannini", 22);
        User user2 = new User("aleserra", "root", "Alessio", "Serra", 22);
        User user3 = new User("loreBianchi", "root", "Lorenzo", "Bianchi", 22);
        UserDAO userDAO = new UserDAOImpl();

        try {
            userDAO.createUser(user1);
        } catch (ActionNotCompletedException e) {
            e.printStackTrace();
        }
         */
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
            playlist.setFavourite(true);
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
    public List<User> getSuggestedUsers(User user) throws ActionNotCompletedException {
        return getSuggestedUsers(user, 40);
    }

    @Override
    public List<User> getSuggestedUsers(User user, int limit) throws ActionNotCompletedException {
        List<User> list = new ArrayList<>();
        try( Session session = Neo4jDriver.getInstance().getDriver().session()) {
            list = session.readTransaction((TransactionWork<List<User>>) tx -> {
                Result result = tx.run(
                        "MATCH (me:User {username: $me})-[:FOLLOWS_USER]->(followed:User)"
                        + "-[:FOLLOWS_USER]->(suggested:User) WHERE NOT (me)-[:FOLLOWS_USER]->(suggested) "
                        + "AND me <> suggested RETURN suggested, count(*) AS Strength "
                        + "ORDER BY Strength DESC LIMIT $limit",
                        parameters("me", user.getUsername(), "limit", limit)
                );
                ArrayList<User> users = new ArrayList<>();
                while ((result.hasNext())){
                    Record r = result.next();
                    users.add(new User(r.get("suggested").get("username").asString()));
                }
                return users;
            });
        } catch (Neo4jException n4jEx) {
            throw new ActionNotCompletedException(n4jEx);
        }
        return list;
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
        if(!userLikesSong(user, song)) {
            try (Session session = Neo4jDriver.getInstance().getDriver().session()) {
                session.run(
                        "MATCH (u:User { username: $username }) "
                                + "MATCH (s:Song { songId: $songId }) "
                                + "MERGE (u)-[:LIKES {day: date()}]->(s)",
                        parameters("username", user.getUsername(), "songId", song.getID())
                );
                logger.info("User <" + user.getUsername() + "> likes song <" + song.getID() + ">");

                // Handle the redundancy $likeCount
                SongDAOImpl songDAO = new SongDAOImpl();
                songDAO.incrementLikeCount(song);
            } catch (Neo4jException n4jEx) {
                logger.error(n4jEx.getMessage());
                throw new ActionNotCompletedException(n4jEx);
            }
        }
    }

    @Override
    public boolean userLikesSong(User user, Song song) {
        try( Session session = Neo4jDriver.getInstance().getDriver().session()) {
            Boolean likes = session.readTransaction((TransactionWork<Boolean>) tx -> {
                Result result = tx.run(
                        "MATCH (:User { username: $username })-[l:LIKES]->(:Song { songId: $songId }) "
                        + "RETURN l",
                        parameters("username", user.getUsername(), "songId", song.getID())
                );
                if ((result.hasNext())) {
                    return true;
                }else
                    return false;
            });
            return likes;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void deleteLike(User user, Song song) throws ActionNotCompletedException {
        if(userLikesSong(user, song)) {
            try (Session session = Neo4jDriver.getInstance().getDriver().session()) {
                session.run(
                        "MATCH (:User { username: $username })-[l:LIKES]->(:Song { songId: $songId }) "
                                + "DELETE l",
                        parameters("username", user.getUsername(), "songId", song.getID())
                );
                logger.info("Deleted user <" + user.getUsername() + "> likes song <" + song.getID() + ">");

                // Handle the redundancy $likeCount
                SongDAOImpl songDAO = new SongDAOImpl();
                songDAO.decrementLikeCount(song);
            } catch (Neo4jException n4jEx) {
                logger.error(n4jEx.getMessage());
                throw new ActionNotCompletedException(n4jEx);
            }
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

    @Override
    public List<Playlist> getAllPlaylist(User user) throws ActionNotCompletedException{
        MongoCollection<Document> usersCollection = MongoDriver.getInstance().getCollection(Collections.USERS);
        List<Playlist> playlists = new ArrayList<Playlist>();

        Bson match = match(eq("_id", user.getUsername()));
        Bson unwind = unwind("$createdPlaylists");

        try (MongoCursor<Document> cursor = usersCollection.aggregate(Arrays.asList(match, unwind)).iterator()) {
            while(cursor.hasNext()) {
                Document result = cursor.next();
                playlists.add(new Playlist(result.get("createdPlaylists", Document.class), user.getUsername()));
            }
        }catch (MongoException mongoEx) {
            logger.error(mongoEx.getMessage());
            throw new ActionNotCompletedException(mongoEx);
        }
        return playlists;
    }

    @Override
    public List<String> getFavouriteGenres(User user, int numGenres) throws ActionNotCompletedException{
        MongoCollection<Document> usersCollection = MongoDriver.getInstance().getCollection(Collections.USERS);
        List<String> result = new ArrayList<String>();
        Bson match = match(eq("_id", user.getUsername()));
        Bson unwind1 = unwind("$createdPlaylists");
        Bson unwind2 = unwind("$createdPlaylists.songs");
        Bson group = Document.parse("{$group: {" +
                                                "_id: \"$createdPlaylists.songs.genre\"," +
                                                "totalSongs: { $sum: 1}" +
                                             "}}");
        Bson sort = sort(descending("totalSongs"));
        Bson limit = limit(numGenres);
        Bson project = project(include("_id"));
        try (MongoCursor<Document> cursor = usersCollection.aggregate(Arrays.asList(match, unwind1, unwind2, group, sort, limit, project)).iterator()) {
            while(cursor.hasNext()) {
                Document genre = cursor.next();
                result.add(genre.getString("_id"));
            }
        }
        return result;
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
