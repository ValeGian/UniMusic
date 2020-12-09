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
import org.bson.types.ObjectId;
import org.json.JSONObject;
import org.neo4j.driver.Session;
import org.neo4j.driver.TransactionWork;
import org.neo4j.driver.exceptions.Neo4jException;
import org.bson.Document;

import javax.print.Doc;
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
        Playlist playlist;
        try {
            //playlist = p.getPlaylist("2");
            playlist = new Playlist("lorenzo", "5fd0e66206871c0d404a2ebd", "terza");
            playlist.setFavourite(false);
            //p.createPlaylist(playlist);
            //playlist = p.getPlaylist("5fd0e138c5452d6017ff69a3");
            //playlist = p.getFavourite(new User("lorenzo"));
            //System.out.println(playlist.getAuthor() + "   " + playlist.getID() + "   " + playlist.getName());
            p.deletePlaylist(playlist);
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

        Bson unwind = unwind("$createdPlaylists");
        Bson match = match(eq("createdPlaylists.playlistId", playlistID));
        Bson project = project(fields(include("_id", "createdPlaylists")));

        Document result = usersCollection.aggregate(Arrays.asList(unwind, match, project)).first();
        playlist = new Playlist(result.get("createdPlaylists", Document.class), result.getString("_id"));
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

        Document result = usersCollection.aggregate(Arrays.asList(match1, unwind, match2, project)).first();

        playlist = new Playlist(result.get("createdPlaylists", Document.class), user.getUsername());
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

    private void createPlaylistDocument(Playlist playlist) {
        UserDAOImpl userDAO = new UserDAOImpl();
        userDAO.addPlaylistToUserDocument(new User(playlist.getAuthor()), playlist);
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
