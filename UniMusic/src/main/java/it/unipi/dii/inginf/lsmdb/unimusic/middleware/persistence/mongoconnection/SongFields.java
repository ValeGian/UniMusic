package it.unipi.dii.inginf.lsmdb.unimusic.middleware.persistence.mongoconnection;
import java.util.ArrayList;

public enum SongFields {
    ID("_id"),
    ALBUM("album"),
    TITLE("title"),
    ARTIST("artist"),
    FEATURED_ARTISTS("featuredArtists"),
    RELEASE_YEAR("releaseYear"),
    GENRE("genre"),
    RATING("rating"),
    LIKE_COUNT("likeCount"),
    YOUTUBE_MEDIA_URL("youtubeMediaURL"),
    SPOTIFY_MEDIA_URL("spotifyMediaURL"),
    GENIUS_MEDIA_URL("geniusMediaURL");


    private String songFieldName;
    private SongFields(String collName) {
        this.songFieldName = collName;
    }

    public String toString() {
        return songFieldName;
    }
}
