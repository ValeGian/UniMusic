package it.unipi.dii.inginf.lsmdb.unimusic.middleware.dao;

import com.google.common.annotations.VisibleForTesting;
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
            playlist.setUrlImage("file:src/main/resources/it/unipi/dii/inginf/lsmdb/unimusic/frontend/gui/img/default_playlist.jpg");
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

    @VisibleForTesting
    List<User> getUserByPartialUsername(String partialUsername, int limitResult) throws ActionNotCompletedException, IllegalArgumentException {
        if(limitResult <= 0) throw new IllegalArgumentException();

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
    public List<User> getSuggestedUsers(User user, int limit) throws ActionNotCompletedException, IllegalArgumentException {
        if(limit <= 0 || user == null) throw new IllegalArgumentException();

        List<User> list;
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
            logger.error(n4jEx.getMessage());
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
    public void followUser(User userFollowing, User userFollowed) throws ActionNotCompletedException, IllegalArgumentException {
        if(userFollowing == null || userFollowed == null) throw new IllegalArgumentException();

        try (Session session = Neo4jDriver.getInstance().getDriver().session()) {
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (following:User { username: $following }) "
                                + "MATCH (followed:User { username: $followed }) "
                                + "WHERE following <> followed "
                                + "MERGE (following)-[:FOLLOWS_USER]->(followed)",
                        parameters("following", userFollowing.getUsername(), "followed", userFollowed.getUsername())
                );
                return null;
            });
            logger.info("User <" + userFollowing.getUsername() + "> follows user <" + userFollowed.getUsername() + ">");
        } catch (Neo4jException n4jEx) {
            logger.error(n4jEx.getMessage());
            throw new ActionNotCompletedException(n4jEx);
        }
    }

    @Override
    public void unfollowUser(User userFollowing, User userFollowed) throws ActionNotCompletedException, IllegalArgumentException {
        if(userFollowing == null || userFollowed == null) throw new IllegalArgumentException();

        try (Session session = Neo4jDriver.getInstance().getDriver().session()) {
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (:User { username: $username1 })-[f:FOLLOWS_USER]->(:User { username: $username2 }) "
                                + "DELETE f",
                        parameters("username1", userFollowing.getUsername(), "username2", userFollowed.getUsername())
                );
                return null;
            });
            logger.info("Deleted user <" +userFollowing.getUsername()+ "> follows user <" +userFollowed.getUsername()+ ">");
        } catch (Neo4jException n4jEx) {
            logger.error(n4jEx.getMessage());
            throw new ActionNotCompletedException(n4jEx);
        }
    }

    @Override
    public void followPlaylist(User user, Playlist playlist) throws ActionNotCompletedException, IllegalArgumentException {
        if(user == null || playlist == null) throw new IllegalArgumentException();

        try (Session session = Neo4jDriver.getInstance().getDriver().session()) {
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (following:User { username: $following }) "
                                + "MATCH (followed:Playlist { playlistId: $followed }) "
                                + "MERGE (following)-[:FOLLOWS_PLAYLIST]->(followed)",
                        parameters("following", user.getUsername(), "followed", playlist.getID())
                );
                return null;
            });
            logger.info("User <" +user.getUsername()+ "> follows playlist <" +playlist.getID()+ ">");
        } catch (Neo4jException n4jEx) {
            logger.error(n4jEx.getMessage());
            throw new ActionNotCompletedException(n4jEx);
        }
    }

    @Override
    public boolean isFollowingPlaylist(User user, Playlist playlist) throws IllegalArgumentException {
        if(user == null || playlist == null) throw new IllegalArgumentException();

        try( Session session = Neo4jDriver.getInstance().getDriver().session()) {
            return session.readTransaction((TransactionWork<Boolean>) tx -> {
                Result result = tx.run(
                        "MATCH (:User { username: $username })-[f:FOLLOWS_PLAYLIST]->(:Playlist { playlistId: $playlistId }) "
                                + "RETURN count(*) AS Times",
                        parameters("username", user.getUsername(), "playlistId", playlist.getID())
                );
                if ((result.hasNext())) {
                    int times = result.next().get("Times").asInt();
                    return times > 0;
                }
                return false;
            });
        } catch (Exception e) {
            logger.warn(e.getMessage());
            return false;
        }
    }

    @Override
    public boolean isFollowedBy(User followed, User following) throws IllegalArgumentException {
        if(followed == null || following == null) throw new IllegalArgumentException();

        try( Session session = Neo4jDriver.getInstance().getDriver().session()) {
            return session.readTransaction((TransactionWork<Boolean>) tx -> {
                Result result = tx.run(
                        "MATCH (:User { username: $following })-[f:FOLLOWS_USER]->(:User { username: $followed }) "
                                + "RETURN count(*) AS Times",
                        parameters("following", following.getUsername(), "followed", followed.getUsername())
                );
                if ((result.hasNext())) {
                    int times = result.next().get("Times").asInt();
                    return times > 0;
                }
                return false;
            });
        } catch (Exception e) {
            logger.warn(e.getMessage());
            return false;
        }
    }

    @Override
    public void unfollowPlaylist(User user, Playlist playlist) throws ActionNotCompletedException, IllegalArgumentException {
        if(user == null || playlist == null) throw new IllegalArgumentException();

        try (Session session = Neo4jDriver.getInstance().getDriver().session()) {
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (:User { username: $username })-[f:FOLLOWS_PLAYLIST]->(:Playlist { playlistId: $playlistId }) "
                                + "DELETE f",
                        parameters("username", user.getUsername(), "playlistId", playlist.getID())
                );
                return null;
            });
            logger.info("Deleted user <" +user.getUsername()+ "> follows playlist <" +playlist.getID()+ ">");
        } catch (Neo4jException n4jEx) {
            logger.error(n4jEx.getMessage());
            throw new ActionNotCompletedException(n4jEx);
        }
    }

    @Override
    public void likeSong(User user, Song song) throws ActionNotCompletedException, IllegalArgumentException {
        if(user == null || song == null) throw new IllegalArgumentException();

        try (Session session = Neo4jDriver.getInstance().getDriver().session()) {
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (u:User { username: $username }) "
                                + "MATCH (s:Song { songId: $songId }) "
                                + "MERGE (u)-[:LIKES {day: date()}]->(s)",
                        parameters("username", user.getUsername(), "songId", song.getID())
                );
                return null;
            });
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
    public boolean userLikesSong(User user, Song song) throws IllegalArgumentException {
        if(user == null || song == null) throw new IllegalArgumentException();

        try( Session session = Neo4jDriver.getInstance().getDriver().session()) {
            return session.readTransaction((TransactionWork<Boolean>) tx -> {
                Result result = tx.run(
                        "MATCH (:User { username: $username })-[l:LIKES]->(:Song { songId: $songId }) "
                        + "RETURN count(*) AS Times",
                        parameters("username", user.getUsername(), "songId", song.getID())
                );
                if ((result.hasNext())) {
                    int times = result.next().get("Times").asInt();
                    return times > 0;
                }
                return false;
            });
        } catch (Exception e) {
            logger.warn(e.getMessage());
            return false;
        }
    }

    @Override
    public void deleteLike(User user, Song song) throws ActionNotCompletedException, IllegalArgumentException {
        if(user == null || song == null) throw new IllegalArgumentException();

        try (Session session = Neo4jDriver.getInstance().getDriver().session()) {
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (:User { username: $username })-[l:LIKES]->(:Song { songId: $songId }) "
                                + "DELETE l",
                        parameters("username", user.getUsername(), "songId", song.getID())
                );
                return null;
            });
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
    public void updateUserPrivilegeLevel(User user, PrivilegeLevel newPrivLevel) throws ActionNotCompletedException, IllegalArgumentException {
        if(user == null || newPrivLevel == null)
            throw new IllegalArgumentException();

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
    public List<Playlist> getAllPlaylist(User user) throws ActionNotCompletedException, IllegalArgumentException {
        if(user == null) throw new IllegalArgumentException();

        MongoCollection<Document> usersCollection = MongoDriver.getInstance().getCollection(Collections.USERS);
        List<Playlist> playlists = new ArrayList<>();

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
    public List<Playlist> getFollowedPlaylist(User user) throws ActionNotCompletedException, IllegalArgumentException {
        if(user == null) throw new IllegalArgumentException();

        try( Session session = Neo4jDriver.getInstance().getDriver().session()) {
            return session.readTransaction((TransactionWork<List<Playlist>>) tx -> {
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
        } catch (Neo4jException n4jEx) {
            logger.error(n4jEx.getMessage());
            throw new ActionNotCompletedException(n4jEx);
        }
    }

    @Override
    public List<User> getFollowedUsers(User user) throws ActionNotCompletedException, IllegalArgumentException {
        if(user == null) throw new IllegalArgumentException();

        try( Session session = Neo4jDriver.getInstance().getDriver().session()) {
            return session.readTransaction((TransactionWork<List<User>>) tx -> {
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
        } catch (Neo4jException n4jEx) {
            logger.error(n4jEx.getMessage());
            throw new ActionNotCompletedException(n4jEx);
        }
    }

    @Override
    public List<User> getFollowers(User user) throws ActionNotCompletedException, IllegalArgumentException {
        if(user == null) throw new IllegalArgumentException();

        try( Session session = Neo4jDriver.getInstance().getDriver().session()) {
            return session.readTransaction((TransactionWork<List<User>>) tx -> {
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
        } catch (Neo4jException n4jEx) {
            logger.error(n4jEx.getMessage());
            throw new ActionNotCompletedException(n4jEx);
        }
    }

    @Override
    public List<Pair<String, Integer>> getFavouriteGenres(int numGenres) throws ActionNotCompletedException, IllegalArgumentException{
        if(numGenres <= 0) throw new IllegalArgumentException();

        MongoCollection<Document> usersCollection = MongoDriver.getInstance().getCollection(Collections.USERS);
        List<Pair<String, Integer>> result = new ArrayList<>();
        Bson unwind1 = unwind("$createdPlaylists");
        Bson unwind2 = unwind("$createdPlaylists.songs");
        Bson group = Document.parse("{$group: {" +
                                                "_id: \"$createdPlaylists.songs.genre\"," +
                                                "totalSongs: { $sum: 1}" +
                                             "}}");
        Bson sort = sort(descending("totalSongs"));
        Bson limit = limit(numGenres);
        try (MongoCursor<Document> cursor = usersCollection.aggregate(Arrays.asList(unwind1, unwind2, group, sort, limit)).iterator()) {
            while(cursor.hasNext()) {
                Document genre = cursor.next();
                result.add(new Pair<>(genre.getString("_id"), genre.getInteger("totalSongs")));
            }
        } catch (MongoException mEx) {
            logger.error(mEx.getMessage());
            throw new ActionNotCompletedException(mEx);
        }
        return result;
    }

    @Override
    public List<Pair<Integer, Pair<String, String>>> getFavouriteArtistPerAgeRange() throws ActionNotCompletedException {
        MongoCollection<Document> userCollection = MongoDriver.getInstance().getCollection(Collections.USERS);
        List<Pair<Integer, Pair<String, String>>> topArtists = new ArrayList<>();

        Document computeDecade = Document.parse("{$multiply: [{ $floor:{ $divide: [ \"$age\", 10 ] }}, 10]}");

        Bson match = match(exists("age"));
        Bson projectDecade = project(fields(
                excludeId(),
                include("age"),
                include("createdPlaylists"),
                computed("decade", computeDecade)
        ));
        Bson unwind1 = unwind("$createdPlaylists");
        Bson unwind2 = unwind("$createdPlaylists.songs");
        Bson groupArtists = Document.parse(
            "{ $group:" +
            "    {" +
            "        _id: { decade: \"$decade\", artist: \"$createdPlaylists.songs.artist\" }," +
            "        presences: { $sum: 1}" +
            "    }" +
            "}"
        );
        Bson sortPresences = sort(descending("presences"));
        Bson groupDecades = Document.parse(
            "{ $group:" +
            "    {" +
            "        _id: \"$_id.decade\"," +
            "        favouriteArtist: { $first: \"$_id.artist\" }," +
            "        artistPresences: { $first: \"$presences\" }," +
            "        overallPresences: { $sum: \"$presences\" }" +
            "    }" +
            "}"
        );
        Bson finalProject = project(fields(
                excludeId(),
                include("favouriteArtist"),
                computed("decade", "$_id"),
                include("artistPresences"),
                include("overallPresences")
        ));
        Bson sortDecades = sort(ascending("decade"));

        try (MongoCursor<Document> cursor = userCollection.aggregate(Arrays.asList(match, projectDecade, unwind1, unwind2, groupArtists, sortPresences, groupDecades, finalProject, sortDecades)).iterator()) {
            while (cursor.hasNext()) {
                Document record = cursor.next();
                int decade = record.getDouble("decade").intValue();
                String artist = record.getString("favouriteArtist");
                String presences = String.valueOf(record.getInteger("artistPresences"));
                String overallPresences = String.valueOf(record.getInteger("overallPresences"));
                topArtists.add(new Pair<>(decade, new Pair<>(artist, presences + ":" + overallPresences)));
            }
        } catch (MongoException mongoEx) {
            logger.error(mongoEx.getMessage());
            throw new ActionNotCompletedException(mongoEx);
        }
        return topArtists;
    }

    @Override
    public void deleteUser(User user) throws ActionNotCompletedException, IllegalArgumentException {
        if(user == null) throw new IllegalArgumentException();

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

    @Override
    public int getTotalUsers() {
        try (Session session = Neo4jDriver.getInstance().getDriver().session())
        {
            return session.readTransaction((TransactionWork<Integer>) tx -> {

                Result result = tx.run("MATCH (:User) RETURN COUNT(*) AS NUM");
                if(result.hasNext())
                    return result.next().get("NUM").asInt();
                else
                    return -1;
            });

        }catch (Neo4jException n4jEx){
            logger.error(n4jEx.getMessage());
            return -1;
        }
    }

    //--------------------------PACKAGE-----------------------------------------------------------

    void addPlaylistToUserDocument(User user, Playlist playlist) throws MongoException, IllegalArgumentException{

        if(user == null || playlist == null) throw new IllegalArgumentException();

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
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MERGE (a:User {username: $username})",
                        parameters("username", user.getUsername())
                );
                return null;
            });
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

    @Override
    public void deleteUserDocument(User user) throws MongoException {
        MongoCollection<Document> userColl = MongoDriver.getInstance().getCollection(Collections.USERS);
        userColl.deleteOne(eq("_id", user.getUsername()));
    }

    public void deleteUserNode(User user) throws Neo4jException {
        try (Session session = Neo4jDriver.getInstance().getDriver().session())
        {
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run(
                        "MATCH (a:User {username: $username})"
                                + "DETACH DELETE a",
                        parameters("username", user.getUsername()));
                return null;
            });
        }
    }

}
