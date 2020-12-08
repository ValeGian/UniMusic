package it.unipi.dii.inginf.lsmdb.unimusic.middleware.dao;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.Playlist;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.Song;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.User;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.exception.ActionNotCompletedException;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.persistence.mongoconnection.Collections;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.persistence.mongoconnection.MongoDriver;
import org.neo4j.driver.exceptions.Neo4jException;
import org.bson.Document;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.*;

public class PlaylistDAOImpl implements PlaylistDAO{

    @Override
    public void createPlaylist(Playlist playlist)  throws ActionNotCompletedException{

    }

    @Override
    public void createFavouritePlaylist(String user, String playlistId)  throws ActionNotCompletedException{
        Playlist playlist = new Playlist(user, "Favourites");
        playlist.setFavourite(true);
        try {
            createPlaylistDocument(playlist);
            createPlaylistNode(playlist);
        } catch (MongoException mongoEx) {
            //loggo
            throw new ActionNotCompletedException(mongoEx);
        } catch (Neo4jException neoEx) {
            //loggo
            try {
                //deleteSongDocument(song);
                throw new ActionNotCompletedException(neoEx);
            } catch (MongoException mongoEx) {
                //loggo
                throw new ActionNotCompletedException(mongoEx);
            }
        }

    }

    @Override
    public Playlist getPlaylist(String playlistID)  throws ActionNotCompletedException{
        return null;
    }

    @Override
    public Playlist getFavourite(User user) throws ActionNotCompletedException {
        return null;
    }

    @Override
    public void addSong(Playlist playlist, Song song)  throws ActionNotCompletedException{

    }

    @Override
    public void deletePlaylist(Playlist playlist)  throws ActionNotCompletedException{

    }

    //---------------------------------------------------------------------------------------------

    private void createPlaylistDocument(Playlist playlist) {
        MongoCollection<Document> usersCollection = MongoDriver.getInstance().getCollection(Collections.USERS);

        Document doc = new Document("playlistId", playlist.getID())
                .append("playlistName", playlist.getAuthor());

        usersCollection.updateOne(eq("username", playlist.getAuthor()), set("playlists", doc));

    }

    private void createPlaylistNode(Playlist playlist) {

    }
}
