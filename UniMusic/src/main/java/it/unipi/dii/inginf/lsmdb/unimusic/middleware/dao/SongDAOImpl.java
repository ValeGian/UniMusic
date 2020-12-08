package it.unipi.dii.inginf.lsmdb.unimusic.middleware.dao;

import com.google.gson.Gson;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.Album;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.Song;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.exception.ActionNotCompletedException;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.log.UMLogger;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.persistence.mongoconnection.Collections;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.persistence.mongoconnection.MongoDriver;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.persistence.mongoconnection.SongFields;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.persistence.mongoconnection.UserFields;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.persistence.neo4jconnection.Labels;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.persistence.neo4jconnection.Neo4jDriver;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.persistence.neo4jconnection.SongProperties;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.persistence.neo4jconnection.UserProperties;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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
    //private static final Logger logger = UMLogger.getSongLogger();

    public static void main(String[] args) throws ActionNotCompletedException {
        SongDAOImpl song = new SongDAOImpl();
        //Song songExample = song.getSongById("5fcf90bcfbeaba48dac81ce9");
        List<Song> songExamples = song.getSongsByPartialTitle("Reasonab");
        for(Song songExample: songExamples)
        System.out.format("%s\t%s\t%s\t%d\n",songExample.getTitle(), songExample.getAlbum().getImage(), songExample.getAlbum().getTitle(), songExample.getReleaseYear());
    }

    @Override
    public void createSong(Song song)  throws ActionNotCompletedException{
        try {
            createSongDocument(song);
            createSongNode(song);
            //logger.info("Created song <" + song.getID() + ">");

        } catch (MongoException mongoEx) {
            //logger.error(mongoEx.getMessage());
            throw new ActionNotCompletedException(mongoEx);
        } catch (Neo4jException neoEx) {
            //logger.error(neoEx.getMessage());
            try {
                deleteSongDocument(song);
                throw new ActionNotCompletedException(neoEx);
            } catch (MongoException mongoEx) {
                //logger.error(mongoEx.getMessage());
                throw new ActionNotCompletedException(mongoEx);
            }
        }

    }

    private void deleteSongDocument(Song song) {
        MongoCollection<Document> songCollection = MongoDriver.getInstance().getCollection(Collections.SONGS);
        songCollection.deleteOne(eq(SongFields.ID.toString(), song.getID()));
    }

    @Override
    public Song getSongById(String songID)  throws ActionNotCompletedException{

        MongoCollection<Document> songCollection = MongoDriver.getInstance().getCollection(Collections.SONGS);
        Song songToReturn = new Song();

        try (MongoCursor<Document> cursor = songCollection.find(eq(SongFields.ID.toString(), songID)).iterator())
        {
            if(cursor.hasNext())
            {
                String jsonSong = cursor.next().toJson();
                songToReturn = new Gson().fromJson(jsonSong, Song.class);
            }
        }
        return songToReturn;
    }

    @Override
    public List<Song> getSongsByPartialTitle(String partialTitle, int maxNumber) throws ActionNotCompletedException {

        MongoCollection<Document> songCollection = MongoDriver.getInstance().getCollection(Collections.SONGS);
        List<Song> songsToReturn = new ArrayList<>();

        Bson match = match(regex(SongFields.TITLE.toString(), "(?i)^" + partialTitle + ".*"));
        Bson sortLike = sort(descending(SongFields.LIKE_COUNT.toString()));
        try (MongoCursor<Document> cursor = songCollection.aggregate(Arrays.asList(match, sortLike, limit(maxNumber))).iterator()) {
            while(cursor.hasNext()) {
                String jsonSong = cursor.next().toJson();
                songsToReturn.add(new Gson().fromJson(jsonSong, Song.class));
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

                String query = "MATCH (s:SONG)-[l:LIKE{day:date()}]-(u:USER)" +
                        "WHIT s, COUNT(*) as num ORDER BY num DESC" +
                        "RETURN s.title as title, s.artist as artist, s.imageUrl as imageUrl";

                Result result = tx.run(query);

                while(result.hasNext()){
                    Record record = result.next();

                    Song hotSong = new Song();
                    hotSong.setTitle(record.get("title").asString());
                    hotSong.setArtist(record.get("artist").asString());
                    hotSong.setAlbum(new Album(record.get("imageUrl").asString()));

                    hotSongs.add(hotSong);
                }
                return hotSongs;
            });
        }
        return null;
    }


    //---------------------------------------------------------------------------------------------

    private void createSongDocument(Song song) throws MongoException{

        MongoCollection<Document> songCollection = MongoDriver.getInstance().getCollection(Collections.SONGS);

        song.setGenre(null);
/*
//PER ORA LO LASCIO COSI' PER AVERE TUTTI LO STESSO DOCUMENT
        Document songDocument = Document.parse(new Gson().toJson(song));

        songCollection.insertOne(songDocument);
 */
        Document songDocument = new Document("_id", song.getID())
                .append("title", song.getTitle());

        Document albumDocument = null;
        if(song.getAlbum().getTitle() != null)
            albumDocument = new Document("title", song.getAlbum().getTitle());

        if(!song.getAlbum().getImage().equals("")){
            if(albumDocument == null)
                albumDocument = new Document("image", song.getAlbum().getImage());
            else
                albumDocument.append("image", song.getAlbum().getImage());
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

                tx.run( "MERGE (p:" + Labels.SONG + " {" + SongProperties.ID + ": $songId," + SongProperties.TITLE +
                                ": $title," + SongProperties.ARTIST + ": $artist," + SongProperties.IMAGE + ": $imageUrl})",
                        parameters(SongProperties.ID, song.getID(), SongProperties.TITLE, song.getTitle(),
                                SongProperties.ARTIST, song.getArtist(), SongProperties.IMAGE, song.getAlbum().getImage() ) );
                return null;
            });
        }
    }


}
