package it.unipi.dii.inginf.lsmdb.unimusic.middleware.dao;

import com.google.common.annotations.VisibleForTesting;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;

import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.Song;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.User;
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

import java.time.Year;
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
        //song.populateWithUser();

        User alessio = new User("ale98", "root", "Alessio", "Serra", 22);
        UserDAOImpl userDAO = new UserDAOImpl();
        //userDAO.createUser(alessio);

        //Song songExample = song.getSongById("5fd0caea9ab23875a76c9819");
        List<Song> songExamples = song.getSongsByPartialArtist("c");

        for (Song songExample : songExamples) {
            song.incrementLikeCount(songExample);
            userDAO.likeSong(alessio, songExample);
            if(new Random().nextInt(2)%2 == 0){
                userDAO.likeSong(alessio, songExample);
                song.incrementLikeCount(songExample);
            }
            if(new Random().nextInt(2)%2 == 0){
                userDAO.likeSong(alessio, songExample);
                song.incrementLikeCount(songExample);
            }
            if(new Random().nextInt(2)%2 == 0){
                userDAO.likeSong(alessio, songExample);
                song.incrementLikeCount(songExample);
            }
            song.incrementLikeCount(songExample);
            song.incrementLikeCount(songExample);
            System.out.format("%s\t%s\t%s\t%d\n", songExample.getID(), songExample.getLikeCount(), songExample.getAlbum().getTitle(), songExample.getReleaseYear());
        }

        List<Song> songExample2 = song.getHotSongs();
        for(Song s: songExample2) {
            if(s != null)
                logger.info(s.toString());
        }

        List<Pair<String, Integer>> ranking = song.findArtistsWithMostNumberOfHit(2, 10);
        for (Pair<String, Integer> entry : ranking) {
            System.out.println("Artista = " + entry.getKey() + ", Value = " + entry.getValue());
        }

        List<Document> list = song.findTopRatedAlbumPerDecade();
        for(Document doc: list){
            logger.info(doc.toString());
        }
        logger.info("FINISH");
    }

    //-----------------------------------------------  CREATE  -----------------------------------------------

    /**
     * Add a song in both databases and handle possible errors:
     * 1) if the song isn't added to MongoDb throws ActionNotCompletedException.
     * 2) if the song isn't added to Neo4j delete also the document in MongoDb to avoid inconsitency and then throws  ActionNotCompletedException.
     * In any case errors are logged.
     * @param song the song you want to add to databases.
     * @throws ActionNotCompletedException
     */
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
     * @throws MongoException
     */
    private void createSongDocument(Song song) throws MongoException{

        MongoCollection<Document> songCollection = MongoDriver.getInstance().getCollection(Collections.SONGS);

        Document songDocument = song.toBsonDocument();

        songCollection.insertOne(songDocument);

    }

    /**
     * Add a song node in Neo4j.
     * @param song the song you want to add to Neo4j.
     * @throws Neo4jException
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
     * @throws ActionNotCompletedException
     */
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


    /**
     * @param partialInput the partial input of the user.
     * @param maxNumber the max number of song you want to return.
     * @param attributeField the document's attribute you want to match.
     * @return songs where the specified attribute fields contains the partial input of the user (case insensitive).
     * @throws ActionNotCompletedException
     */
    @VisibleForTesting
    List<Song> filterSong(String partialInput, int maxNumber, String attributeField) throws ActionNotCompletedException {

        if(attributeField == null || maxNumber < 0)
            throw new IllegalArgumentException();

        MongoCollection<Document> songCollection = MongoDriver.getInstance().getCollection(Collections.SONGS);
        List<Song> songsToReturn = new ArrayList<>();

        Bson match = match(regex(attributeField, "(?i)^" + partialInput + ".*"));
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

    /**
     * It's an Analytic function.
     * @return the album with the highest average of rating for every decade.
     * @throws ActionNotCompletedException
     */
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

    /**
     * It's an Analytic function.
     * @param hitLimit the threshold to consider a song as a hit.
     * @param maxNumber the max number of artists you want to return.
     * @return artists which made the highest number of “hit songs”. A song is a “hit” if it received more than hitLimit likes.
     * @throws ActionNotCompletedException
     */
    public List<Pair<String, Integer>> findArtistsWithMostNumberOfHit(int hitLimit, int maxNumber) throws ActionNotCompletedException {

        if(maxNumber < 0)
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

    /**
     * It's an Analytic function.
     * @return songs that received more likes in the current day.
     * @throws ActionNotCompletedException
     */
    @Override
    public List<Song> getHotSongs() throws ActionNotCompletedException {

        List<Song> hotSongs = new ArrayList<>();

        try ( Session session = Neo4jDriver.getInstance().getDriver().session() )
        {
            session.writeTransaction((TransactionWork<List<Song>>) tx -> {

                String query = "MATCH (s:Song)<-[l:LIKES {day:date()}]-(u:User) " +
                        "WITH s, COUNT(*) as num ORDER BY num DESC " +
                        "RETURN s.songId as songId, s.title as title, s.artist as artist, s.imageUrl as imageUrl";

                Result result = tx.run(query);
                while(result.hasNext())
                    hotSongs.add(new Song(result.next()));

                return hotSongs;
            });
        } catch (Neo4jException neoEx) {
            logger.error(neoEx.getMessage());
            throw new ActionNotCompletedException(neoEx);
        }
        return hotSongs;
    }

    //-----------------------------------------------  UPDATE  -----------------------------------------------

    /**
     * Update the song Document in MongoDb incrementing the likeCount field.
     * @param song the song you want to update.
     * @throws ActionNotCompletedException
     */
    void incrementLikeCount(Song song) throws ActionNotCompletedException{

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

    /**
     * Update the song Document in MongoDb decrementing the likeCount field.
     * @param song the song you want to update.
     * @throws ActionNotCompletedException
     */
    void decrementLikeCount(Song song) throws ActionNotCompletedException{

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

    //-----------------------------------------------  DELETE  -----------------------------------------------

    /**
     * @param song the song you want to delete.
     */
    private void deleteSongDocument(Song song) {

        if(song == null || song.getID() == null)
            throw new IllegalArgumentException();

        MongoCollection<Document> songCollection = MongoDriver.getInstance().getCollection(Collections.SONGS);
        songCollection.deleteOne(eq("_id", song.getID()));
    }

    private void populateWithUser(){
        Random generator = new Random();

        String[] firstName =  new String[] {"Emily","Hannah","Madison","Ashley","Sarah","Alexis","Samantha","Jessica","Elizabeth","Taylor","Lauren","Alyssa","Kayla","Abigail","Brianna","Olivia","Emma","Megan","Grace","Victoria","Rachel","Anna","Sydney","Destiny","Morgan","Jennifer","Jasmine","Haley","Julia","Kaitlyn","Nicole","Amanda","Katherine","Natalie","Hailey","Alexandra","Adam", "Alex", "Aaron", "Ben", "Carl", "Dan", "David", "Edward", "Fred", "Frank", "George", "Hal", "Hank", "Ike", "John", "Jack", "Joe", "Larry", "Monte", "Matthew", "Mark", "Nathan", "Otto", "Paul", "Peter", "Roger", "Roger", "Steve", "Thomas", "Tim", "Ty", "Victor", "Walter"};

        String[] lastName = new String[] {"Anderson", "Ashwoon", "Aikin", "Bateman", "Bongard", "Bowers", "Boyd", "Cannon", "Cast", "Deitz", "Dewalt", "Ebner", "Frick", "Hancock", "Haworth", "Hesch", "Hoffman", "Kassing", "Knutson", "Lawless", "Lawicki", "Mccord", "McCormack", "Miller", "Myers", "Nugent", "Ortiz", "Orwig", "Ory", "Paiser", "Pak", "Pettigrew", "Quinn", "Quizoz", "Ramachandran", "Resnick", "Sagar", "Schickowski", "Schiebel", "Sellon", "Severson", "Shaffer", "Solberg", "Soloman", "Sonderling", "Soukup", "Soulis", "Stahl", "Sweeney", "Tandy", "Trebil", "Trusela", "Trussel", "Turco", "Uddin", "Uflan", "Ulrich", "Upson", "Vader", "Vail", "Valente", "Van Zandt", "Vanderpoel", "Ventotla", "Vogal", "Wagle", "Wagner", "Wakefield", "Weinstein", "Weiss", "Woo", "Yang", "Yates", "Yocum", "Zeaser", "Zeller", "Ziegler", "Bauer", "Baxster", "Casal", "Cataldi", "Caswell", "Celedon", "Chambers", "Chapman", "Christensen", "Darnell", "Davidson", "Davis", "DeLorenzo", "Dinkins", "Doran", "Dugelman", "Dugan", "Duffman", "Eastman", "Ferro", "Ferry", "Fletcher", "Fietzer", "Hylan", "Hydinger", "Illingsworth", "Ingram", "Irwin", "Jagtap", "Jenson", "Johnson", "Johnsen", "Jones", "Jurgenson", "Kalleg", "Kaskel", "Keller", "Leisinger", "LePage", "Lewis", "Linde", "Lulloff", "Maki", "Martin", "McGinnis", "Mills", "Moody", "Moore", "Napier", "Nelson", "Norquist", "Nuttle", "Olson", "Ostrander", "Reamer", "Reardon", "Reyes", "Rice", "Ripka", "Roberts", "Rogers", "Root", "Sandstrom", "Sawyer", "Schlicht", "Schmitt", "Schwager", "Schutz", "Schuster", "Tapia", "Thompson", "Tiernan", "Tisler"};

        for(int i = 0; i < 100; i++){
            String fName = firstName[generator.nextInt(firstName.length)];
            String lName = lastName[generator.nextInt(lastName.length)];
            int age = generator.nextInt(30) + 20;
            String username;
            String password = "";
            for(int j = 0; j < 6; j++){
                password += (char) (generator.nextInt(26) + 'a');
            }
            password = Integer.toString(generator.nextInt(100));

            try {
                username = fName.substring(0, 3) + lName.substring(0, 3) + (Year.now().getValue() - age);
            }catch (IndexOutOfBoundsException index){
                continue;
            }
            User user = new User(username, password,fName, lName, age);

            UserDAOImpl userDAO = new UserDAOImpl();

            try {
                userDAO.createUser(user);
            } catch (ActionNotCompletedException e) {
                e.printStackTrace();
            }
        }

    }

}
