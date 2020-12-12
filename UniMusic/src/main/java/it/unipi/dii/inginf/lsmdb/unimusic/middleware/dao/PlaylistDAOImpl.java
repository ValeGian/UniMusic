package it.unipi.dii.inginf.lsmdb.unimusic.middleware.dao;


import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Updates;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.Album;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.Playlist;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.Song;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.User;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.exception.ActionNotCompletedException;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.log.UMLogger;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.persistence.mongoconnection.Collections;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.persistence.mongoconnection.MongoDriver;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.persistence.neo4jconnection.Neo4jDriver;
import org.apache.log4j.Logger;
import org.bson.conversions.Bson;
import org.bson.json.JsonWriterSettings;
import org.bson.types.ObjectId;
import org.json.JSONObject;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.TransactionWork;
import org.neo4j.driver.exceptions.Neo4jException;
import org.bson.Document;

import javax.print.Doc;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.*;
import static com.mongodb.client.model.Updates.*;
import static org.neo4j.driver.Values.parameters;

public class PlaylistDAOImpl implements PlaylistDAO{
    private static final Logger logger = UMLogger.getPlaylistLogger();

    public static void main(String[] args){
        PlaylistDAOImpl p = new PlaylistDAOImpl();
        UserDAOImpl u = new UserDAOImpl();
        SongDAOImpl s = new SongDAOImpl();
        Playlist playlist1 = new Playlist("gaetano", "5fd390f51059072084c0feac", "");
        Playlist playlist2 = new Playlist("", "5fd3a344f081e1488abf477c", "");

        User user1 = new User("lorenzo");
        User user2 = new User("gaetano");
        User user3 = new User("gesu");

        try {
            //u.followUser(user1, user2);
            //u.followPlaylist(user2, playlist2);
            //List<Song> list = p.getAllSongs(playlist);
            Song song = s.getSongById("5fd24df2dec1f62bba09f905");
            p.deleteSongFromFavourite(user1, song);
            List<Playlist> list = p.getSuggestedPlaylists(user1);
            for (int i = 0;i < list.size(); i++){
                System.out.println(list.get(i).getID() + "   " + list.get(i).getName());
            }

        } catch (ActionNotCompletedException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void createPlaylist(Playlist playlist)  throws ActionNotCompletedException{
        playlist.setID(ObjectId.get().toString());
        try {
            createPlaylistDocument(playlist);
            createPlaylistNode(playlist);
            logger.info("Created playlist " + playlist.getID());
        } catch (MongoException mongoEx) {
            logger.error(mongoEx.getMessage());
            throw new ActionNotCompletedException(mongoEx);
        } catch (Neo4jException neoEx) {
            logger.error(neoEx.getMessage());
            try {
                deletePlaylistDocument(playlist);
                throw new ActionNotCompletedException(neoEx);
            } catch (MongoException mongoEx) {
                logger.error(mongoEx.getMessage());
                throw new ActionNotCompletedException(mongoEx);
            }
        }
    }

    @Override
    public Playlist getPlaylist(String playlistID)  throws ActionNotCompletedException{
        MongoCollection<Document> usersCollection = MongoDriver.getInstance().getCollection(Collections.USERS);
        Playlist playlist = null;


        Bson match = match(eq("createdPlaylists.playlistId", playlistID));
        Bson unwind = unwind("$createdPlaylists");
        Bson project = project(fields(include("_id", "createdPlaylists")));

        try (MongoCursor<Document> cursor = usersCollection.aggregate(Arrays.asList(match, unwind, match, project)).iterator()) {
            if(cursor.hasNext()) {
                Document result = cursor.next();
                playlist = new Playlist(result.get("createdPlaylists", Document.class), result.getString("_id"));
            }
        }catch (MongoException mongoEx) {
            logger.error(mongoEx.getMessage());
            throw new ActionNotCompletedException(mongoEx);
        }
        return playlist;
    }

    @Override
    public Playlist getFavourite(User user) throws ActionNotCompletedException {
        MongoCollection<Document> usersCollection = MongoDriver.getInstance().getCollection(Collections.USERS);
        Playlist playlist = null;

        Bson match1 = match(eq("_id", user.getUsername()));
        Bson unwind = unwind("$createdPlaylists");
        Bson match2 = match(eq("createdPlaylists.isFavourite", true));
        Bson project = project(fields(include("createdPlaylists")));

        try (MongoCursor<Document> cursor = usersCollection.aggregate(Arrays.asList(match1, unwind, match2, project)).iterator()) {
            if(cursor.hasNext()) {
                Document result = cursor.next();
                playlist = new Playlist(result.get("createdPlaylists", Document.class), user.getUsername());
            }
        }catch (MongoException mongoEx) {
            logger.error(mongoEx.getMessage());
            throw new ActionNotCompletedException(mongoEx);
        }
        return playlist;
    }

    @Override
    public void addSong(Playlist playlist, Song song)  throws ActionNotCompletedException{
        try {
            MongoCollection<Document> usersCollection = MongoDriver.getInstance().getCollection(Collections.USERS);

            Document songDocument = new Document("songId", song.getID())
                    .append("title", song.getTitle())
                    .append("artist", song.getArtist())
                    .append("urlImage", song.getAlbum().getImage())
                    .append("genre", song.getGenre());

            Bson find = eq("createdPlaylists.playlistId", playlist.getID());
            Bson query = push("createdPlaylists.$.songs", songDocument);
            usersCollection.updateOne(find, query);
            logger.info("Added song " + song.getID() + " to playlist " + playlist.getID());
        } catch (MongoException mongoEx) {
            logger.error(mongoEx.getMessage());
            throw new ActionNotCompletedException(mongoEx);
        }

    }

    @Override
    public void deleteSong(Playlist playlist, Song song) throws ActionNotCompletedException{
        try {
            MongoCollection<Document> usersCollection = MongoDriver.getInstance().getCollection(Collections.USERS);
            Bson find = eq("createdPlaylists.playlistId", playlist.getID());
            Bson query = pull("createdPlaylists.$.songs", eq("songId", song.getID()));

            usersCollection.updateOne(find, query);
            logger.info("Deleted song " + song.getID() + " from playlist " + playlist.getID());
        } catch (MongoException mongoEx) {
            logger.error(mongoEx.getMessage());
            throw new ActionNotCompletedException(mongoEx);
        }
    }

    @Override
    public void addSongToFavourite(User user, Song song) throws ActionNotCompletedException{
        addSong(getFavourite(user), song);
    }

    @Override
    public void deleteSongFromFavourite(User user, Song song) throws ActionNotCompletedException{
        deleteSong(getFavourite(user), song);
    }
    @Override
    public void deletePlaylist(Playlist playlist)  throws ActionNotCompletedException{
        try {
            deletePlaylistDocument(playlist);
            deletePlaylistNode(playlist);
            logger.info("Deleted playlist " + playlist.getID());
        } catch (MongoException mongoEx) {
            logger.error(mongoEx.getMessage());
            throw new ActionNotCompletedException(mongoEx);
        } catch (Neo4jException neoEx) {
            logger.error(neoEx.getMessage());
            throw new ActionNotCompletedException(neoEx);
        }
    }

    @Override
    public List<Song> getAllSongs(Playlist playlist) throws ActionNotCompletedException{
        MongoCollection<Document> usersCollection = MongoDriver.getInstance().getCollection(Collections.USERS);
        List<Song> songs = new ArrayList<Song>();

        Bson match = match(eq("createdPlaylists.playlistId", playlist.getID()));
        Bson unwind1 = unwind("$createdPlaylists");
        Bson unwind2 = unwind("$createdPlaylists.songs");

        try (MongoCursor<Document> cursor = usersCollection.aggregate(Arrays.asList(match, unwind1, match, unwind2)).iterator()) {
            while(cursor.hasNext()) {
                Document result = cursor.next().get("createdPlaylists", Document.class).get("songs", Document.class);
                Song song = new Song();
                song.setID(result.getString("songId"));
                song.setTitle(result.getString("title"));
                song.setArtist(result.getString("artist"));
                song.setAlbum(new Album(null, result.getString("urlImage")));
                song.setGenre(result.getString("genre"));
                songs.add(song);
            }
        }catch (MongoException mongoEx) {
            logger.error(mongoEx.getMessage());
            throw new ActionNotCompletedException(mongoEx);
        }
        return songs;
    }

    @Override
    public List<Playlist> getSuggestedPlaylists(User user) throws ActionNotCompletedException{
        return getSuggestedPlaylists(user, 40);
    }

    @Override
    public List<Playlist> getSuggestedPlaylists(User user, int limit) throws ActionNotCompletedException{
        List<Playlist> list = new ArrayList<Playlist>();
        try( Session session = Neo4jDriver.getInstance().getDriver().session()) {
            list = session.readTransaction((TransactionWork<List<Playlist>>) tx -> {
                //first level suggestion
                Result result = tx.run(
                        "MATCH (me:User {username: $me})-[:FOLLOWS_USER]->(followed:User)"
                                + "-[:FOLLOWS_PLAYLIST]->(suggested:Playlist) WHERE NOT (me)-[:FOLLOWS_PLAYLISTS]->(suggested) "
                                + "RETURN suggested, count(*) AS Strength "
                                + "ORDER BY Strength DESC LIMIT $limit",
                        parameters("me", user.getUsername(), "limit", limit)
                );
                ArrayList<Playlist> playlists = new ArrayList<Playlist>();
                while ((result.hasNext())){
                    Record r = result.next();
                    System.out.println(r.toString());
                    playlists.add(new Playlist(r.get("suggested")));
                }
                return playlists;
            });
        } catch (Neo4jException n4jEx) {
            throw new ActionNotCompletedException(n4jEx);
        }
        return list;
    }
    /* ****************************************************
    DA METTERE NEL PACKAGE DI POPOLAMENTO
    ***************************************************** */
    public Song getRandomSong() throws ActionNotCompletedException{
        MongoCollection<Document> songsCollection = MongoDriver.getInstance().getCollection(Collections.SONGS);
        Song song = null;

        Bson sample = sample(1);

        try (MongoCursor<Document> cursor = songsCollection.aggregate(Arrays.asList(sample)).iterator()) {
            if(cursor.hasNext()) {
                Document result = cursor.next();
                song = new Song(result.toJson());
            }
        }catch (MongoException mongoEx) {
            logger.error(mongoEx.getMessage());
            throw new ActionNotCompletedException(mongoEx);
        }
        return song;
    }

    public User getRandomUser() throws ActionNotCompletedException{
        MongoCollection<Document> usersCollection = MongoDriver.getInstance().getCollection(Collections.USERS);
        User user = null;

        Bson sample = sample(1);

        try (MongoCursor<Document> cursor = usersCollection.aggregate(Arrays.asList(sample)).iterator()) {
            if(cursor.hasNext()) {
                Document result = cursor.next();
                user = new User(result);
            }
        }catch (MongoException mongoEx) {
            logger.error(mongoEx.getMessage());
            throw new ActionNotCompletedException(mongoEx);
        }

        return user;
    }

    //select some random users and add random number of playlist to them, with a random number of random songs
    public void createRandomPlaylist(int numUsers, int maxNumPlaylistsPerUser, int maxNumSongsPerPlaylists) throws ActionNotCompletedException{
        Random random = new Random();
        for (int i = 0; i < numUsers; i++){
            User user = getRandomUser();
            System.out.println(user.getUsername());
            int numPlaylists = random.nextInt(maxNumPlaylistsPerUser);
            for (int j = 0; j < numPlaylists; j++){
                Playlist playlist = new Playlist(user.getUsername(), "Playlist " + (j + 1) + " by " + user.getFirstName());
                createPlaylist(playlist);
                addRandomSongs(playlist, random.nextInt(maxNumSongsPerPlaylists));
            }
        }
    }

    public void addRandomSongs(Playlist playlist, int numSong) throws ActionNotCompletedException{
        for (int i = 0; i < numSong; i++){
            Song song = getRandomSong();
            addSong(playlist, song);
        }
    }

    //put some likes to random songs from a user
    public void likeRandomSongs(User user, int numLikes) throws ActionNotCompletedException{
        UserDAO userDao = new UserDAOImpl();

        for (int i = 0; i < numLikes; i++)
            userDao.likeSong(user, getRandomSong());
    }

    //put some likes to random songs from random users
    public void completelyRandomLikes(int numLikes) throws ActionNotCompletedException{
        UserDAO userDao = new UserDAOImpl();

        for (int i = 0; i < numLikes; i++)
            userDao.likeSong(getRandomUser(), getRandomSong());
    }
    //---------------------------------------------------------------------------------------------

    private void createPlaylistDocument(Playlist playlist) {
        UserDAOImpl userDAO = new UserDAOImpl();
        userDAO.addPlaylistToUserDocument(new User(playlist.getAuthor()), playlist);
    }

    private void createPlaylistNode(Playlist playlist) {
        try ( Session session = Neo4jDriver.getInstance().getDriver().session() )
        {
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run( "CREATE (p:Playlist {playlistId: $playlistId, name: $name, urlImage: $urlImage})",
                        parameters("playlistId", playlist.getID(), "name", playlist.getName(),
                                                "urlImage", playlist.getUrlImage()) );
                return null;
            });
        }
    }

    private void deletePlaylistDocument(Playlist playlist){
        MongoCollection<Document> usersCollection = MongoDriver.getInstance().getCollection(Collections.USERS);
        usersCollection.updateOne(new Document(), pull("createdPlaylists", eq("playlistId", playlist.getID())));
    }

    private void deletePlaylistNode(Playlist playlist){
        try ( Session session = Neo4jDriver.getInstance().getDriver().session() )
        {
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run( "MATCH (p:Playlist { playlistId: $playlistId }) DETACH DELETE p",
                        parameters("playlistId", playlist.getID()) );
                return null;
            });
        }
    }


}
