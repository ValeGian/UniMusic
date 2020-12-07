package it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities;

import java.util.ArrayList;

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
