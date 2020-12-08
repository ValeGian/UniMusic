package it.unipi.dii.inginf.lsmdb.unimusic.middleware.dao;

import com.mongodb.MongoException;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.Playlist;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.Song;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.User;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.exception.ActionNotCompletedException;
import org.neo4j.driver.exceptions.Neo4jException;
import org.bson.Document;

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
        Document doc =
    }

    private void createPlaylistNode(Playlist playlist) {

    }
}
