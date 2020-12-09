package it.unipi.dii.inginf.lsmdb.unimusic.middleware.dao;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;

import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.Song;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.exception.ActionNotCompletedException;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.log.UMLogger;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.persistence.mongoconnection.Collections;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.persistence.mongoconnection.MongoDriver;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.persistence.neo4jconnection.Neo4jDriver;
import javafx.util.Pair;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.TransactionWork;
import org.neo4j.driver.exceptions.Neo4jException;

import java.util.*;

import static com.mongodb.client.model.Accumulators.sum;
import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.*;
import static com.mongodb.client.model.Sorts.ascending;
import static com.mongodb.client.model.Sorts.descending;
import static com.mongodb.client.model.Updates.inc;

import static org.neo4j.driver.Values.parameters;

public class SongDAOImpl implements SongDAO{
    private static final Logger logger = UMLogger.getSongLogger();

    public static void main(String[] args) throws ActionNotCompletedException {
        SongDAOImpl song = new SongDAOImpl();
        //Song songExample = song.getSongById("5fd0caea9ab23875a76c9819");
        List<Song> songExamples = song.getSongsByPartialArtist("c");

        for (Song songExample : songExamples) {
            song.incrementLikeCount(songExample);
            if(new Random().nextInt(2)%2 == 0){
                song.incrementLikeCount(songExample);
            }
            if(new Random().nextInt(2)%2 == 0){
                song.incrementLikeCount(songExample);
            }
            if(new Random().nextInt(2)%2 == 0){
                song.incrementLikeCount(songExample);
            }
            song.incrementLikeCount(songExample);
            song.incrementLikeCount(songExample);
            System.out.format("%s\t%s\t%s\t%d\n", songExample.getID(), songExample.getLikeCount(), songExample.getAlbum().getTitle(), songExample.getReleaseYear());
        }

        List<Pair<String, Integer>> ranking = song.findArtistsWithMostNumberOfHit(2, 10);
        for (Pair<String, Integer> entry : ranking) {
            System.out.println("Artista = " + entry.getKey() + ", Value = " + entry.getValue());
        }

        List<Document> list = song.findTopRatedAlbumPerDecade();
        for(Document doc: list){
            logger.info(doc.toString());
        }
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

    void incrementLikeCount(Song song) throws ActionNotCompletedException{
        MongoCollection<Document> songCollection = MongoDriver.getInstance().getCollection(Collections.SONGS);
        try{
            songCollection.updateOne(eq("_id", song.getID()), inc("likeCount", 1));
            song.setLikeCount(song.getLikeCount()+1);
        } catch (MongoException mongoEx) {
            logger.error(mongoEx.getMessage());
            throw new ActionNotCompletedException(mongoEx);
        }

    }

    void decrementLikeCount(Song song) throws ActionNotCompletedException{
        MongoCollection<Document> songCollection = MongoDriver.getInstance().getCollection(Collections.SONGS);
        try{
            songCollection.updateOne(eq("_id", song.getID()), inc("likeCount", -1));
            song.setLikeCount(song.getLikeCount()-1);
        } catch (MongoException mongoEx) {
            logger.error(mongoEx.getMessage());
            throw new ActionNotCompletedException(mongoEx);
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
        } catch (MongoException mongoEx) {
            logger.error(mongoEx.getMessage());
            throw new ActionNotCompletedException(mongoEx);
        }
        return songToReturn;
    }

    public List<Document> findTopRatedAlbumPerDecade() throws ActionNotCompletedException {

        MongoCollection<Document> songCollection = MongoDriver.getInstance().getCollection(Collections.SONGS);
        List<Document> topAlbum = new ArrayList<>();

        Document computeExpression = Document.parse("{$multiply: [{ $floor:{ $divide: [ \"$releaseYear\", 10 ] }}, 10]}");

        Bson match = match(exists("releaseYear"));
        Bson project = project(fields(excludeId(), include("releaseYear"), include("album"), include("rating"), computed("decade", computeExpression)));

        Bson group = Document.parse("{$group: {_id: {album: \"$album.title\", decade: \"$decade\"}, avgRating: {$avg: \"$rating\"}}}");
        Bson sortRate = sort(ascending("avgRating"));

        Bson group2 = Document.parse("{$group:{" +
                "_id: \"$_id.decade\"," +
                "topAlbum: {$last: \"$_id.album\"}," +
                "avgRating:{$last: \"$avgRating\"}" +
                "}}");
        Bson sortId = sort(ascending("_id"));
        Bson project2 = project(fields(excludeId(), computed("decade", "$_id"), include("topAlbum"),  include("avgRating")));

        try (MongoCursor<Document> cursor = songCollection.aggregate(Arrays.asList(match, project,group, sortRate, group2, sortId, project2)).iterator()){
            while(cursor.hasNext()) {
                Document record = cursor.next();
                topAlbum.add(record);
            }
        } catch (MongoException mongoEx) {
            logger.error(mongoEx.getMessage());
            throw new ActionNotCompletedException(mongoEx);
        }
        return topAlbum;
    }

    public List<Pair<String, Integer>> findArtistsWithMostNumberOfHit(int hitLimit, int maxNumber) throws ActionNotCompletedException {

        MongoCollection<Document> songCollection = MongoDriver.getInstance().getCollection(Collections.SONGS);

        List<Pair<String, Integer>> artistRank = new ArrayList<>();

        Bson match = match(gte("likeCount", hitLimit));
        Bson group = group("$artist", sum("numberOfHits", 1));
        Bson sortHits = sort(descending("numberOfHits"));
        Bson limit = limit(maxNumber);
        Bson project = project(fields(excludeId(), computed("artist", "$_id"), include("numberOfHits")));
        try (MongoCursor<Document> cursor = songCollection.aggregate(Arrays.asList(match, group, sortHits, limit, project)).iterator()) {
            while(cursor.hasNext()) {
                Document artist = cursor.next();
                artistRank.add(new Pair<>(artist.getString("artist"), artist.getInteger("numberOfHits")));
            }
        } catch (MongoException mongoEx) {
            logger.error(mongoEx.getMessage());
            throw new ActionNotCompletedException(mongoEx);
        }
        return artistRank;
    }




    private List<Song> filterSong(String partialInput, int maxNumber, String filterType) throws ActionNotCompletedException {

        MongoCollection<Document> songCollection = MongoDriver.getInstance().getCollection(Collections.SONGS);
        List<Song> songsToReturn = new ArrayList<>();

        Bson match = match(regex(filterType, "(?i)^" + partialInput + ".*"));
        Bson sortLike = sort(descending("likeCount"));
        try (MongoCursor<Document> cursor = songCollection.aggregate(Arrays.asList(match, sortLike, limit(maxNumber))).iterator()) {
            while(cursor.hasNext()) {
                String jsonSong = cursor.next().toJson();
                songsToReturn.add(new Song(jsonSong));
            }
        } catch (MongoException mongoEx) {
            logger.error(mongoEx.getMessage());
            throw new ActionNotCompletedException(mongoEx);
        }
        return songsToReturn;
    }

    @Override
    public List<Song> getSongsByPartialAlbum(String partialAlbum, int limit) throws ActionNotCompletedException {
        return filterSong(partialAlbum, limit, "album.title");
    }


    @Override
    public List<Song> getSongsByPartialAlbum(String partialAlbum) throws ActionNotCompletedException {
        return getSongsByPartialAlbum(partialAlbum, 20);
    }


    @Override
    public List<Song> getSongsByPartialTitle(String partialTitle, int limit) throws ActionNotCompletedException {
        return filterSong(partialTitle, limit, "title");
    }


    @Override
    public List<Song> getSongsByPartialTitle(String partialTitle) throws ActionNotCompletedException {
        return getSongsByPartialTitle(partialTitle, 20);
    }

    @Override
    public List<Song> getSongsByPartialArtist(String partialArtist, int limit) throws ActionNotCompletedException {
        return filterSong(partialArtist, limit, "artist");
    }


    @Override
    public List<Song> getSongsByPartialArtist(String partialArtist) throws ActionNotCompletedException {
        return getSongsByPartialArtist(partialArtist, 20);
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
        } catch (Neo4jException neoEx) {
            logger.error(neoEx.getMessage());
            throw new ActionNotCompletedException(neoEx);
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
