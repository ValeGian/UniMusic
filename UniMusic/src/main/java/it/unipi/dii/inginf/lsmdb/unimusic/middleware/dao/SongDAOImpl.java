package it.unipi.dii.inginf.lsmdb.unimusic.middleware.dao;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.Album;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.Song;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.exception.ActionNotCompletedException;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.log.UMLogger;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.persistence.mongoconnection.Collections;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.persistence.mongoconnection.MongoDriver;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.persistence.neo4jconnection.Neo4jDriver;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.TransactionWork;
import org.neo4j.driver.exceptions.Neo4jException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.regex;
import static com.mongodb.client.model.Sorts.descending;
import static org.neo4j.driver.Values.parameters;

public class SongDAOImpl implements SongDAO{
    private static final Logger logger = UMLogger.getSongLogger();

    public static void main(String[] args) throws ActionNotCompletedException {
        SongDAOImpl song = new SongDAOImpl();
        Song songExample = song.getSongById("5fd0caea9ab23875a76c9819");
        //List<Song> songExamples = song.getSongsByPartialTitle("AL");
        //(for(Song songExample: songExamples)
        System.out.format("%s\t%s\t%s\t%d\n",songExample.getTitle(), songExample.getAlbum().getImage(), songExample.getAlbum().getTitle(), songExample.getReleaseYear());
    }

    @Override
    public void createSong(Song song)  throws ActionNotCompletedException{
        try {
            createSongDocument(song);
            createSongNode(song);
            logger.info("Created song <" + song.getID() + ">");

        } catch (MongoException mongoEx) {
            logger.error(mongoEx.getMessage());
            throw new ActionNotCompletedException(mongoEx);
        } catch (Neo4jException neoEx) {
            logger.error(neoEx.getMessage());
            try {
                deleteSongDocument(song);
                throw new ActionNotCompletedException(neoEx);
            } catch (MongoException mongoEx) {
                logger.error(mongoEx.getMessage());
                throw new ActionNotCompletedException(mongoEx);
            }
        }

    }

    private void deleteSongDocument(Song song) {
        MongoCollection<Document> songCollection = MongoDriver.getInstance().getCollection(Collections.SONGS);
        songCollection.deleteOne(eq("_id", song.getID()));
    }

    @Override
    public Song getSongById(String songID)  throws ActionNotCompletedException{

        MongoCollection<Document> songCollection = MongoDriver.getInstance().getCollection(Collections.SONGS);
        Song songToReturn = null;

        try (MongoCursor<Document> cursor = songCollection.find(eq("_id", songID)).iterator())
        {
            if(cursor.hasNext())
            {
                String jsonSong = cursor.next().toJson();
                songToReturn = new Song(jsonSong);
            }
        }
        return songToReturn;
    }

    @Override
    public List<Song> getSongsByPartialTitle(String partialTitle, int maxNumber) throws ActionNotCompletedException {

        MongoCollection<Document> songCollection = MongoDriver.getInstance().getCollection(Collections.SONGS);
        List<Song> songsToReturn = new ArrayList<>();

        Bson match = match(regex("title", "(?i)^" + partialTitle + ".*"));
        Bson sortLike = sort(descending("likeCount"));
        try (MongoCursor<Document> cursor = songCollection.aggregate(Arrays.asList(match, sortLike, limit(maxNumber))).iterator()) {
            while(cursor.hasNext()) {
                String jsonSong = cursor.next().toJson();
                songsToReturn.add(new Song(jsonSong));
            }
        }
        return songsToReturn;
    }

    @Override
    public List<Song> getSongsByPartialTitle(String partialTitle) throws ActionNotCompletedException {
        return getSongsByPartialTitle(partialTitle, 20);
    }

    @Override
    public List<Song> getHotSongs() throws ActionNotCompletedException {

        List<Song> hotSongs = new ArrayList<>();

        try ( Session session = Neo4jDriver.getInstance().getDriver().session() )
        {
            session.writeTransaction((TransactionWork<List<Song>>) tx -> {

                String query = "MATCH (s:Song)-[l:LIKES{day:date()}]-(u:User)" +
                        "WITH s, COUNT(*) as num ORDER BY num DESC" +
                        "RETURN s.songId as songId, s.title as title, s.artist as artist, s.imageUrl as imageUrl";

                Result result = tx.run(query);
                while(result.hasNext()){
                    Record record = result.next();
                    hotSongs.add(new Song(record));
                }
                return hotSongs;
            });
        }
        return null;
    }


    //---------------------------------------------------------------------------------------------

    private void createSongDocument(Song song) throws MongoException{

        MongoCollection<Document> songCollection = MongoDriver.getInstance().getCollection(Collections.SONGS);

        Document songDocument = song.toBsonDocument();

        songCollection.insertOne(songDocument);

    }


    private void createSongNode(Song song) throws Neo4jException{

        try ( Session session = Neo4jDriver.getInstance().getDriver().session() )
        {
            session.writeTransaction((TransactionWork<Void>) tx -> {

                tx.run( "CREATE (p:Song {songId: $songId, title: $title, artist: $artist, imageUrl: $imageUrl})",
                        parameters("songId", song.getID(), "title", song.getTitle(),
                                "artist", song.getArtist(), "imageUrl", song.getAlbum().getImage() ) );
                return null;
            });
        }
    }



}
