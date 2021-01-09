package it.unipi.dii.inginf.lsmdb.unimusic.middleware.dao;

import com.google.common.annotations.VisibleForTesting;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;

import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.*;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.exception.ActionNotCompletedException;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.log.UMLogger;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.persistence.mongoconnection.Collections;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.persistence.mongoconnection.MongoDriver;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.persistence.neo4jconnection.Neo4jDriver;
import javafx.util.Pair;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.TransactionWork;
import org.neo4j.driver.exceptions.Neo4jException;

import java.time.LocalDate;
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
        SongDAOImpl s = new SongDAOImpl();
        s.getSongsByPartialTitle("a");
    }

    //-----------------------------------------------  CREATE  -----------------------------------------------


    @Override
    public void createSong(Song song) throws ActionNotCompletedException{

        if(song == null || song.getID() == null)
            throw new IllegalArgumentException();

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

    /**
     * Add a song document in MongoDb.
     * @param song the song you want to add to mongoDb.
     * @throws MongoException when the database write fails.
     */
    private void createSongDocument(Song song) throws MongoException{

        MongoCollection<Document> songCollection = MongoDriver.getInstance().getCollection(Collections.SONGS);

        Document songDocument = song.toBsonDocument();

        songCollection.insertOne(songDocument);

    }

    /**
     * Add a song node in Neo4j.
     * @param song the song you want to add to Neo4j.
     * @throws Neo4jException when the database write fails.
     */
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

    //----------------------------------------------  RETRIEVE  ----------------------------------------------


    /**
     * @param songID the id of the song you wanto to return.
     * @return the song with the specified id.
     */
    @Override
    public Song getSongById(String songID) {

        MongoCollection<Document> songCollection = MongoDriver.getInstance().getCollection(Collections.SONGS);
        Song songToReturn = null;

        try (MongoCursor<Document> cursor = songCollection.find(eq("_id", songID)).iterator())
        {
            if(cursor.hasNext())
            {
                songToReturn = new Song(cursor.next());
            }
        } catch (MongoException mongoEx) {
            logger.error(mongoEx.getMessage());
        }
        return songToReturn;
    }


    /**
     * @param partialInput the partial input of the user.
     * @param maxNumber the max number of song you want to return.
     * @param attributeField the document's attribute you want to match.
     * @return songs where the specified attribute fields contains the partial input of the user (case insensitive).
     * @throws ActionNotCompletedException when a database error occurs.
     */
    @VisibleForTesting
    public List<Song> filterSong(String partialInput, int maxNumber, String attributeField) throws ActionNotCompletedException {

        if(attributeField == null || maxNumber <= 0)
            throw new IllegalArgumentException();

        MongoCollection<Document> songCollection = MongoDriver.getInstance().getCollection(Collections.SONGS);
        List<Song> songsToReturn = new ArrayList<>();

        String capitalPartialInput = partialInput.substring(0, 1).toUpperCase() + partialInput.substring(1);

        Bson match = match(regex(attributeField, "^" + capitalPartialInput + ".*"));
        Bson sortLike = sort(descending("likeCount"));
        try (MongoCursor<Document> cursor = songCollection.aggregate(Arrays.asList(match, sortLike, limit(maxNumber))).iterator()) {
            while(cursor.hasNext()) {
                songsToReturn.add(new Song(cursor.next()));
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
    public List<Pair<Integer, Pair<Album, Double>>> findTopRatedAlbumPerDecade() throws ActionNotCompletedException {

        MongoCollection<Document> songCollection = MongoDriver.getInstance().getCollection(Collections.SONGS);
        List<Pair<Integer, Pair<Album, Double>>> topAlbums = new ArrayList<>();

        Document computeExpression = Document.parse("{$multiply: [{ $floor:{ $divide: [ \"$releaseYear\", 10 ] }}, 10]}");

        Bson match = match(exists("releaseYear"));
        Bson project = project(fields(excludeId(), include("releaseYear"), include("album"), include("rating"), computed("decade", computeExpression)));

        Bson group = Document.parse("{$group: {_id: {title: \"$album.title\", url:\"$album.image\",  decade: \"$decade\"}, avgRating: {$avg: \"$rating\"}, numSong:{$sum:1}}}");
        Bson match2 = match(gt("numSong", 5));
        Bson sortRate = sort(ascending("avgRating"));

        Bson group2 = Document.parse("{$group:{" +
                "_id: \"$_id.decade\"," +
                "topAlbumTitle: {$last: \"$_id.title\"}," +
                "topAlbumUrl: {$last: \"$_id.url\"}," +
                "avgRating:{$last: \"$avgRating\"}" +
                "}}");
        Bson sortId = sort(ascending("_id"));
        Bson project2 = project(fields(excludeId(), computed("decade", "$_id"), include("topAlbumTitle"),  include("avgRating"), include("topAlbumUrl")));

        try (MongoCursor<Document> cursor = songCollection.aggregate(Arrays.asList(match, project, group, match2, sortRate, group2, sortId, project2)).iterator()){
            while(cursor.hasNext()) {
                Document record = cursor.next();

                Album albumToAdd = new Album(); albumToAdd.setTitle(record.getString("topAlbumTitle")); albumToAdd.setImage(record.getString("topAlbumUrl"));

                int decade = record.getDouble("decade").intValue();

                double avgRating = record.getDouble("avgRating");

                Pair<Integer, Pair<Album, Double>> resultToAdd= new Pair<>(decade, new Pair<>(albumToAdd, avgRating));
                topAlbums.add(resultToAdd);
            }
        } catch (MongoException mongoEx) {
            logger.error(mongoEx.getMessage());
            throw new ActionNotCompletedException(mongoEx);
        }
        return topAlbums;
    }


    @Override
    public List<Pair<String, Integer>> findArtistsWithMostNumberOfHit(int hitLimit, int maxNumber) throws ActionNotCompletedException {

        if(maxNumber <= 0)
            throw new IllegalArgumentException();

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


    @Override
    public List<Song> getHotSongs(int limit) throws ActionNotCompletedException {

        List<Song> hotSongs = new ArrayList<>();

        try ( Session session = Neo4jDriver.getInstance().getDriver().session() )
        {
            session.writeTransaction((TransactionWork<List<Song>>) tx -> {

                LocalDate lastMonth = LocalDate.now().minusDays(30);

                String query = "MATCH (s:Song)<-[l:LIKES]-(u:User) " +
                        "WHERE l.day > date($lastMonth) " +
                        "WITH s, COUNT(*) as num ORDER BY num DESC " +
                        "RETURN s.songId as songId, s.title as title, s.artist as artist, s.imageUrl as imageUrl " +
                        "LIMIT $limit";

                Result result = tx.run(query, parameters("lastMonth", lastMonth.toString(), "limit", limit));
                while(result.hasNext()) {
                    hotSongs.add(new Song(result.next()));
                }
                return hotSongs;
            });
        } catch (Neo4jException neoEx) {
            logger.error(neoEx.getMessage());
            throw new ActionNotCompletedException(neoEx);
        }
        return hotSongs;
    }


    //-----------------------------------------------  UPDATE  -----------------------------------------------


    @Override
    public void incrementLikeCount(Song song) throws ActionNotCompletedException{

        if(song == null || song.getID() == null)
            throw new IllegalArgumentException();

        MongoCollection<Document> songCollection = MongoDriver.getInstance().getCollection(Collections.SONGS);
        try{
            songCollection.updateOne(eq("_id", song.getID()), inc("likeCount", 1));
            song.setLikeCount(song.getLikeCount()+1);
        } catch (MongoException mongoEx) {
            logger.error(mongoEx.getMessage());
            throw new ActionNotCompletedException(mongoEx);
        }

    }


    @Override
    public void decrementLikeCount(Song song) throws ActionNotCompletedException{

        if(song == null || song.getID() == null)
            throw new IllegalArgumentException();

        MongoCollection<Document> songCollection = MongoDriver.getInstance().getCollection(Collections.SONGS);
        try{
            songCollection.updateOne(eq("_id", song.getID()), inc("likeCount", -1));
            song.setLikeCount(song.getLikeCount()-1);
        } catch (MongoException mongoEx) {
            logger.error(mongoEx.getMessage());
            throw new ActionNotCompletedException(mongoEx);
        }
    }


    @Override
    public int getTotalSongs() {
        try (Session session = Neo4jDriver.getInstance().getDriver().session())
        {
            return session.readTransaction((TransactionWork<Integer>) tx -> {

                Result result = tx.run("MATCH (:Song) RETURN COUNT(*) AS NUM");
                if(result.hasNext())
                    return result.next().get("NUM").asInt();
                else
                    return -1;
            });

        }catch (Neo4jException neo4){
            neo4.printStackTrace();
            return -1;
        }
    }

    //-----------------------------------------------  DELETE  -----------------------------------------------


    @Override
    public void deleteSong(Song song) throws ActionNotCompletedException, IllegalArgumentException {
        if(song == null) throw new IllegalArgumentException();

        try {
            deleteSongDocument(song);
            deleteSongNode(song);
            logger.info("DELETED Song " + song.getID());
        } catch (MongoException | Neo4jException mEx) {
            logger.error(mEx.getMessage());
            throw new ActionNotCompletedException(mEx);
        }
    }

    @Override
    public void deleteSongDocument(Song song) throws MongoException {
        MongoCollection<Document> songColl = MongoDriver.getInstance().getCollection(Collections.SONGS);
        songColl.deleteOne(eq("_id", song.getID()));
    }

    private void deleteSongNode(Song song) throws Neo4jException {
        try (Session session = Neo4jDriver.getInstance().getDriver().session())
        {
            session.writeTransaction((TransactionWork<Void>) tx -> {

                tx.run("MATCH (s:Song {songId: $songId})"
                                + "DETACH DELETE s",
                        parameters("songId", song.getID()));
                return null;
            });
        }
    }

}
