package it.unipi.dii.inginf.lsmdb.unimusic.middleware.dao;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.Song;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.exception.ActionNotCompletedException;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.persistence.mongoconnection.Collections;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.persistence.mongoconnection.MongoDriver;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.persistence.neo4jconnection.Neo4jDriver;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.neo4j.driver.Session;
import org.neo4j.driver.TransactionWork;
import org.neo4j.driver.exceptions.Neo4jException;

import java.util.Arrays;

import static org.neo4j.driver.Values.parameters;

public class SongDAOImpl implements SongDAO{

    @Override
    public void createSong(Song song)  throws ActionNotCompletedException{
        try {
            createSongDocument(song);
            createSongNode(song);
        } catch (MongoException mongoEx) {
            //loggo
            throw new ActionNotCompletedException(mongoEx);
        } catch (Neo4jException neoEx) {
            //loggo
            try {
                deleteSongDocument(song);
                throw new ActionNotCompletedException(neoEx);
            } catch (MongoException mongoEx) {
                //loggo
                throw new ActionNotCompletedException(mongoEx);
            }
        }

    }

    private void deleteSongDocument(Song song) {
    }

    @Override
    public Song getSong(String songID)  throws ActionNotCompletedException{
        return null;
    }

    //---------------------------------------------------------------------------------------------

    private void createSongDocument(Song song) throws MongoException{

        MongoCollection<Document> songCollection = MongoDriver.getInstance().getCollection(Collections.SONGS);

        Document songDocument = new Document("_id", song.getID())
                .append("title", song.getTitle());

        Document albumDocument = null;
        if(song.getAlbum().getTitle() != null)
            albumDocument = new Document("title", song.getAlbum().getTitle());

        if(!song.getAlbum().getImageURL().equals("")){
            if(albumDocument == null)
                albumDocument = new Document("image", song.getAlbum().getImageURL());
            else
                albumDocument.append("image", song.getAlbum().getImageURL());
        }

        if(albumDocument != null)
            songDocument.append("album", albumDocument);

        songDocument.append("artist", song.getArtist());

        if(song.getGenre() != null)
            songDocument.append("genre", song.getGenre());

        if(song.getFeaturedArtists() != null)
            songDocument.append("featuredArtists", song.getFeaturedArtists());

        if(song.getReleaseYear() != 0)
        songDocument.append("releaseYear", song.getReleaseYear());

        songDocument.append("rating", song.getRating());

        songDocument.append("media", Arrays.asList(
                new Document("provider", "youtube")
                        .append("url", song.getYoutubeMediaURL()),
                new Document("provider", "spotify")
                        .append("url", song.getSpotifyMediaURL()),
                new Document("provider", "genius")
                        .append("url", song.getGeniusMediaURL())
        ));
        songCollection.insertOne(songDocument);

    }

    private void createSongNode(Song song) throws Neo4jException{

        try ( Session session = Neo4jDriver.getInstance().getDriver().session() )
        {
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run( "MERGE (p:Song {title: $title, artist: $artist, imageUrl: $imageUrl})",
                        parameters( "title", song.getTitle(), "artist", song.getArtist(), "imageUrl", song.getAlbum().getImageURL() ) );
                return null;
            });
        }
    }
}
