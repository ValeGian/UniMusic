package it.unipi.dii.inginf.lsmdb.unimusic.middleware.dao;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Updates;
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
import org.json.JSONObject;
import org.neo4j.driver.Session;
import org.neo4j.driver.TransactionWork;
import org.neo4j.driver.exceptions.Neo4jException;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
        try {
            Playlist playlist;

            //p.createFavouritePlaylist("manolo", "13");
            //p.createPlaylist(new Playlist("manolo", "14", "abracadabra"));
            //p.createPlaylist(new Playlist("manolo", "15", "sadsadsa"));

            User user = new User("manolo", "1", "1", "1", 22);
            playlist = p.getPlaylist("13");
            System.out.println(playlist.getID() + " " + playlist.getAuthor() + " " + playlist.getName());
            playlist = p.getFavourite(user);
            System.out.println(playlist.getID() + " " + playlist.getAuthor() + " " + playlist.getName());
            p.deletePlaylistNode(new Playlist("manolo", "14", "abracadabra"));
        } catch (ActionNotCompletedException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void createPlaylist(Playlist playlist)  throws ActionNotCompletedException{
        createPlaylistConcrete(playlist);
    }

    @Override
    public void createFavouritePlaylist(String user, String playlistId)  throws ActionNotCompletedException{
        Playlist playlist = new Playlist(user, playlistId, "Favourites");
        playlist.setFavourite(true);
        createPlaylistConcrete(playlist);
    }

    @Override
    public Playlist getPlaylist(String playlistID)  throws ActionNotCompletedException{
        MongoCollection<Document> usersCollection = MongoDriver.getInstance().getCollection(Collections.USERS);
        Playlist playlist = null;

        Bson unwind = unwind("$playlists");
        Bson match = match(eq("playlists.playlistId", playlistID));
        Bson project = project(fields(include("username", "playlists")));

        String result = usersCollection.aggregate(Arrays.asList(unwind, match, project)).first().toJson();

        playlist = getPlaylistFromJson(result, null);
        return playlist;
    }

    @Override
    public Playlist getFavourite(User user) throws ActionNotCompletedException {
        MongoCollection<Document> usersCollection = MongoDriver.getInstance().getCollection(Collections.USERS);
        Playlist playlist = null;

        Bson match1 = match(eq("username", user.getUsername()));
        Bson unwind = unwind("$playlists");
        Bson match2 = match(eq("playlists.isFavourite", true));
        Bson project = project(fields(include("playlists")));

        String result = usersCollection.aggregate(Arrays.asList(match1, unwind, match2, project)).first().toJson();

        playlist = getPlaylistFromJson(result, user.getUsername());
        return playlist;
    }

    @Override
    public void addSong(Playlist playlist, Song song)  throws ActionNotCompletedException{
        MongoCollection<Document> usersCollection = MongoDriver.getInstance().getCollection(Collections.USERS);

        Document doc = new Document("songId", song.getID())
                            .append("title", song.getTitle())
                            .append("artist", song.getArtist())
                            .append("link", song.getYoutubeMediaURL());

        //da fare
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

    //---------------------------------------------------------------------------------------------

    private void createPlaylistConcrete(Playlist playlist) throws ActionNotCompletedException{
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

    private void createPlaylistDocument(Playlist playlist) {
        MongoCollection<Document> usersCollection = MongoDriver.getInstance().getCollection(Collections.USERS);

        Document doc = new Document("playlistId", playlist.getID())
                .append("playlistName", playlist.getName());
        if (playlist.isFavourite())
            doc.append("isFavourite", true);

        usersCollection.updateOne(eq("username", playlist.getAuthor()), push("playlists", doc));
    }

    private void createPlaylistNode(Playlist playlist) {
        try ( Session session = Neo4jDriver.getInstance().getDriver().session() )
        {
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run( "MERGE (p:Playlist {playlistId: $playlistId})",
                        parameters("playlistId", playlist.getID()) );
                return null;
            });
        }
    }

    private Playlist getPlaylistFromJson(String json, String author){
        JSONObject jsonObject = new JSONObject(json);

        if (author == null)
            author = jsonObject.getString("username");

        String idPlaylist = jsonObject.getJSONObject("playlists").getString("playlistId");
        String playlistName = jsonObject.getJSONObject("playlists").getString("playlistName");
        Playlist playlist = new Playlist(author, idPlaylist, playlistName);
        if (jsonObject.getJSONObject("playlists").has("isFavourite"))
            playlist.isFavourite();

        return playlist;
    }

    private void deletePlaylistDocument(Playlist playlist){
        //da fare
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
