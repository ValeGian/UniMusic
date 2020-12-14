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

import javax.swing.plaf.synth.SynthScrollBarUI;
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

        User admin = new User("valegiann", "root", "Valerio", "Giannini", 22);
        admin.setPrivilegeLevel(PrivilegeLevel.ADMIN);
        userDAO.createUser(admin);

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

            Playlist playlist = new Playlist(
                    user.getUsername(), "Favourites of "
                    + user.getFirstName() + " " + user.getLastName()
            );
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
    public List<User> getUserByPartialUsername(String partialUsername) throws ActionNotCompletedException {
        return getUserByPartialUsername(partialUsername, 40);
    }

    private List<User> getUserByPartialUsername(String partialUsername, int limitResult) throws ActionNotCompletedException {
        if(limitResult < 0)
            throw new IllegalArgumentException();

        MongoCollection<Document> userCollection = MongoDriver.getInstance().getCollection(Collections.USERS);
        List<User> usersToReturn = new ArrayList<>();

        Bson match = match(regex("_id", "(?i)^" + partialUsername + ".*"));

        try (MongoCursor<Document> cursor = userCollection.aggregate(Arrays.asList(match, limit(limitResult))).iterator()) {
            while(cursor.hasNext()) {
                usersToReturn.add(new User(cursor.next()));
            }
        } catch (MongoException mongoEx) {
            logger.error(mongoEx.getMessage());
            throw new ActionNotCompletedException(mongoEx);
        }
        return usersToReturn;
    }


    @Override
    public List<User> getSuggestedUsers(User user) throws ActionNotCompletedException {
        return getSuggestedUsers(user, 40);
    }

    @Override
    public List<User> getSuggestedUsers(User user, int limit) throws ActionNotCompletedException {
        List<User> list = new ArrayList<>();
        try( Session session = Neo4jDriver.getInstance().getDriver().session()) {
            //First layer Suggestion
            list = session.readTransaction((TransactionWork<List<User>>) tx -> {
                Result result = tx.run(
                        "MATCH (me:User {username: $me})-[:FOLLOWS_USER]->(followed:User)"
                        + "-[:FOLLOWS_USER]->(suggested:User) WHERE NOT (me)-[:FOLLOWS_USER]->(suggested) "
                        + "AND me <> suggested RETURN suggested, count(*) AS Strength "
                        + "ORDER BY Strength DESC LIMIT $limit",
                        parameters("me", user.getUsername(), "limit", limit)
                );
                ArrayList<User> firstLayerUsers = new ArrayList<>();
                while ((result.hasNext())){
                    Record r = result.next();
                    firstLayerUsers.add(new User(r.get("suggested").get("username").asString()));
                }
                return firstLayerUsers;
            });

            final int firstSuggestionSize = list.size();
            if(firstSuggestionSize < limit) {
                //Second layer suggestion
                List<User> secondLayerSuggestion = session.readTransaction((TransactionWork<List<User>>) tx -> {
                    Result result = tx.run(
                            "MATCH (me:User {username: $username})-[:LIKES]->()<-[:LIKES]-(suggested:User) "
                                    + "WHERE NOT (me)-[:FOLLOWS_USER]->(suggested) "
                                    + "AND NOT (me)-[:FOLLOWS_USER]->()-[:FOLLOWS_USER]->(suggested) AND me <> suggested "
                                    + "RETURN suggested, count(*) AS Strength ORDER BY Strength DESC LIMIT $limit",
                            parameters("username", user.getUsername(), "limit", limit - firstSuggestionSize)
                    );
                    ArrayList<User> secondLayerUsers = new ArrayList<>();
                    while ((result.hasNext())){
                        Record r = result.next();
                        secondLayerUsers.add(new User(r.get("suggested").get("username").asString()));
                    }
                    return secondLayerUsers;
                });
                list.addAll(secondLayerSuggestion);
            }
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
                            + "WHERE following <> followed " +
                            ""
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
    public boolean isFollowingPlaylist(User user, Playlist playlist) {
        try( Session session = Neo4jDriver.getInstance().getDriver().session()) {
            Boolean follows = session.readTransaction((TransactionWork<Boolean>) tx -> {
                Result result = tx.run(
                        "MATCH (:User { username: $username })-[f:FOLLOWS_PLAYLIST]->(:Playlist { playlistId: $playlistId }) "
                                + "RETURN count(*) AS Times",
                        parameters("username", user.getUsername(), "playlistId", playlist.getID())
                );
                if ((result.hasNext())) {
                    int times = result.next().get("Times").asInt();
                    if(times > 0)
                        return true;
                }
                return false;
            });
            return follows;
        } catch (Exception e) {
            logger.warn(e.getMessage());
            return false;
        }
    }

    @Override
    public boolean isFollowedBy(User followed, User following) {
        try( Session session = Neo4jDriver.getInstance().getDriver().session()) {
            Boolean follows = session.readTransaction((TransactionWork<Boolean>) tx -> {
                Result result = tx.run(
                        "MATCH (:User { username: $following })-[f:FOLLOWS_USER]->(:User { username: $followed }) "
                                + "RETURN count(*) AS Times",
                        parameters("following", following.getUsername(), "followed", followed.getUsername())
                );
                if ((result.hasNext())) {
                    int times = result.next().get("Times").asInt();
                    if(times > 0)
                        return true;
                }
                return false;
            });
            return follows;
        } catch (Exception e) {
            logger.warn(e.getMessage());
            return false;
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
            logger.info("User <" + user.getUsername() + "> likes song <" + song.getID() + ">");

            // Handle the redundancy $likeCount
            SongDAOImpl songDAO = new SongDAOImpl();
            songDAO.incrementLikeCount(song);
        } catch (Neo4jException n4jEx) {
            logger.error(n4jEx.getMessage());
            throw new ActionNotCompletedException(n4jEx);
        }
    }

    @Override
    public boolean userLikesSong(User user, Song song) {
        try( Session session = Neo4jDriver.getInstance().getDriver().session()) {
            Boolean likes = session.readTransaction((TransactionWork<Boolean>) tx -> {
                Result result = tx.run(
                        "MATCH (:User { username: $username })-[l:LIKES]->(:Song { songId: $songId }) "
                        + "RETURN count(*) AS Times",
                        parameters("username", user.getUsername(), "songId", song.getID())
                );
                if ((result.hasNext())) {
                    int times = result.next().get("Times").asInt();
                    if(times > 0)
                        return true;
                }
                return false;
            });
            return likes;
        } catch (Exception e) {
            logger.warn(e.getMessage());
            return false;
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
            logger.info("Deleted user <" + user.getUsername() + "> likes song <" + song.getID() + ">");

            // Handle the redundancy $likeCount
            SongDAOImpl songDAO = new SongDAOImpl();
            songDAO.decrementLikeCount(song);
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
    public List<Playlist> getFollowedPlaylist(User user) throws ActionNotCompletedException {
        try( Session session = Neo4jDriver.getInstance().getDriver().session()) {
            List<Playlist> playlists = session.readTransaction((TransactionWork<List<Playlist>>) tx -> {
                Result result = tx.run(
                        "MATCH (:User {username: $username})-[:FOLLOWS_PLAYLIST]->(playlist:Playlist) RETURN playlist ",
                        parameters("username", user.getUsername())
                );
                List<Playlist> tmpList = new ArrayList<>();
                while ((result.hasNext())) {
                    Playlist playlist = new Playlist(result.next().get("playlist"));
                    playlist.setAuthor(user.getUsername());
                    tmpList.add(playlist);
                }
                return tmpList;
            });
            return playlists;
        } catch (Neo4jException n4jEx) {
            throw new ActionNotCompletedException(n4jEx);
        }
    }

    @Override
    public List<User> getFollowedUsers(User user) throws ActionNotCompletedException {
        try( Session session = Neo4jDriver.getInstance().getDriver().session()) {
            List<User> followedUsers = session.readTransaction((TransactionWork<List<User>>) tx -> {
                Result result = tx.run(
                        "MATCH (:User {username: $username})-[:FOLLOWS_USER]->(followedUser:User) RETURN followedUser ",
                        parameters("username", user.getUsername())
                );
                List<User> tmpList = new ArrayList<>();
                while ((result.hasNext())) {
                    User followedUser = new User(result.next().get("followedUser"));
                    tmpList.add(followedUser);
                }
                return tmpList;
            });
            return followedUsers;
        } catch (Neo4jException n4jEx) {
            throw new ActionNotCompletedException(n4jEx);
        }
    }

    @Override
    public List<User> getFollowers(User user) throws ActionNotCompletedException {
        try( Session session = Neo4jDriver.getInstance().getDriver().session()) {
            List<User> followingUsers = session.readTransaction((TransactionWork<List<User>>) tx -> {
                Result result = tx.run(
                        "MATCH (followingUser:User)-[:FOLLOWS_USER]->(:User {username: $username}) RETURN followingUser ",
                        parameters("username", user.getUsername())
                );
                List<User> tmpList = new ArrayList<>();
                while ((result.hasNext())) {
                    User followingUser = new User(result.next().get("followingUser"));
                    tmpList.add(followingUser);
                }
                return tmpList;
            });
            return followingUsers;
        } catch (Neo4jException n4jEx) {
            throw new ActionNotCompletedException(n4jEx);
        }
    }

    @Override
    public List<String> getFavouriteGenres(int numGenres) throws ActionNotCompletedException{
        MongoCollection<Document> usersCollection = MongoDriver.getInstance().getCollection(Collections.USERS);
        List<String> result = new ArrayList<String>();
        Bson unwind1 = unwind("$createdPlaylists");
        Bson unwind2 = unwind("$createdPlaylists.songs");
        Bson group = Document.parse("{$group: {" +
                                                "_id: \"$createdPlaylists.songs.genre\"," +
                                                "totalSongs: { $sum: 1}" +
                                             "}}");
        Bson sort = sort(descending("totalSongs"));
        Bson limit = limit(numGenres);
        Bson project = project(include("_id"));
        try (MongoCursor<Document> cursor = usersCollection.aggregate(Arrays.asList(unwind1, unwind2, group, sort, limit, project)).iterator()) {
            while(cursor.hasNext()) {
                Document genre = cursor.next();
                result.add(genre.getString("_id"));
            }
        }
        return result;
    }

    @Override
    public void deleteUser(User user) throws ActionNotCompletedException {
        List<Playlist> userPlaylists = getAllPlaylist(user);

        PlaylistDAO playlistDAO = new PlaylistDAOImpl();
        for(Playlist playlist: userPlaylists)
            playlistDAO.deletePlaylist(playlist);

        try {
            deleteUserDocument(user);
            deleteUserNode(user);
            logger.info("DELETED User " + user.getUsername());
        } catch (MongoException mEx) {
            logger.error(mEx.getMessage());
            throw new ActionNotCompletedException(mEx);
        } catch (Neo4jException n4jEx) {
            logger.error(n4jEx.getMessage());
            throw new ActionNotCompletedException(n4jEx);
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

    private void deleteUserNode(User user) throws Neo4jException {
        try (Session session = Neo4jDriver.getInstance().getDriver().session()) {
            session.run(
                    "MATCH (a:User {username: $username})"
                    + "DETACH DELETE a",
                    parameters("username", user.getUsername()));
        }
    }
}
