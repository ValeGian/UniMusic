package it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities;

import org.bson.BsonArray;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.neo4j.driver.Record;

import java.util.ArrayList;
import java.util.Arrays;

public class Song {
    private String ID;
    private Album album;
    private String title;
    private String artist;
    private ArrayList<String> featuredArtists;
    private int releaseYear;
    private String genre;
    private double rating;
    private int likeCount;
    private String youtubeMediaURL;
    private String spotifyMediaURL;
    private String geniusMediaURL;

    public Song() {

    }

    /**
     * Constructs a song given a Neo4j Record initializing only fields that correspond to song Node properties.
     * @param songNeo4jRecord
     */
    public Song(Record songNeo4jRecord) {

        String tmp;
        ID = songNeo4jRecord.get("songId").asString();
        title = songNeo4jRecord.get("title").asString();
        artist = songNeo4jRecord.get("artist").asString();
        if ((tmp = songNeo4jRecord.get("imageUrl").asString()) != null)
            album = new Album(tmp);
    }

    /**
     * Constructs a song given a json string.
     * @param jsonSong
     */
    public Song(String jsonSong){

        JSONObject song = new JSONObject(jsonSong);

        ID = song.getString("_id");
        title = song.getString("title");
        artist = song.getString("artist");
        rating = song.getDouble("rating");
        likeCount = song.getInt("likeCount");

        JSONArray media = song.getJSONArray("media");
        youtubeMediaURL = media.getJSONObject(0).getString("url");
        spotifyMediaURL = media.getJSONObject(1).getString("url");
        geniusMediaURL = media.getJSONObject(2).getString("url");

        Album songAlbum = new Album();
        try{
            songAlbum.setTitle(song.getJSONObject("album").getString("title"));
        }catch (JSONException jex){}

        try{
            songAlbum.setImage(song.getJSONObject("album").getString("image"));
        }catch (JSONException jex){}

        album = songAlbum;
        featuredArtists = new ArrayList<>();
        try {
            JSONArray jsonFeaturedArtists = song.getJSONArray("featuredArtists");
            if (jsonFeaturedArtists.length() != 0) {
                for (int i = 0; i < jsonFeaturedArtists.length(); i++)
                    featuredArtists.add(jsonFeaturedArtists.getString(i));
            }
        }catch (JSONException jex){}

        try {
            releaseYear = song.getInt("releaseYear");
        }catch (JSONException jex){}

        try{
            genre = song.getString("genre");
        }catch (JSONException jex){}

    }

    /**
     * @return
     */
    public Document toBsonDocument() {

        Document songDocument = new Document("_id", ID)
                .append("title", title);

        Document albumDocument = null;
        if(album.getTitle() != null)
            albumDocument = new Document("title", album.getTitle());

        if(album.getImage() != null){
            if(albumDocument == null)
                albumDocument = new Document("image", album.getImage());
            else
                albumDocument.append("image", album.getImage());
        }

        if(albumDocument != null)
            songDocument.append("album", albumDocument);

        songDocument.append("artist", artist);

        if(genre != null)
            songDocument.append("genre", genre);

        if(featuredArtists != null)
            songDocument.append("featuredArtists", featuredArtists);

        if(releaseYear != 0)
            songDocument.append("releaseYear", releaseYear);

        songDocument.append("rating", rating);

        songDocument.append("likeCount", likeCount);

        songDocument.append("media", Arrays.asList(
                new Document("provider", "youtube")
                        .append("url", youtubeMediaURL),
                new Document("provider", "spotify")
                        .append("url", spotifyMediaURL),
                new Document("provider", "genius")
                        .append("url", geniusMediaURL)
        ));

        return songDocument;
    }

    public Song(String ID,
                String albumName,
                String albumImageURL,
                String artist,
                ArrayList<String> featuredArtists,
                int releaseYear,
                String genre,
                double rating,
                int likeCount,
                String youtubeMediaURL,
                String spotifyMediaURL,
                String geniusMediaURL) {
        this.ID = ID;
        this.genre = genre;
        this.album = new Album(albumName, albumImageURL);
        this.artist = artist;
        this.featuredArtists = featuredArtists;
        this.releaseYear = releaseYear;
        this.rating = rating;
        this.likeCount = likeCount;
        this.youtubeMediaURL = youtubeMediaURL;
        this.spotifyMediaURL = spotifyMediaURL;
        this.geniusMediaURL = geniusMediaURL;
    }

    public String getID() { return ID; }

    public void setID(String ID) { this.ID = ID; }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public ArrayList<String> getFeaturedArtists() {
        return featuredArtists;
    }

    public void setFeaturedArtists(ArrayList<String> featuredArtists) {
        this.featuredArtists = featuredArtists;
    }

    public int getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(int releaseYear) { this.releaseYear = releaseYear; }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public String getYoutubeMediaURL() {
        return youtubeMediaURL;
    }

    public void setYoutubeMediaURL(String youtubeMediaURL) {
        this.youtubeMediaURL = youtubeMediaURL;
    }

    public String getSpotifyMediaURL() {
        return spotifyMediaURL;
    }

    public void setSpotifyMediaURL(String spotifyMediaURL) {
        this.spotifyMediaURL = spotifyMediaURL;
    }

    public String getGeniusMediaURL() {
        return geniusMediaURL;
    }

    public void setGeniusMediaURL(String geniusMediaURL) {
        this.geniusMediaURL = geniusMediaURL;
    }

    public Album getAlbum() { return album; }

    public void setAlbum(Album album) { this.album = album; }

    public String getGenre() { return genre; }

    public void setGenre(String genre) { this.genre = genre; }

}
