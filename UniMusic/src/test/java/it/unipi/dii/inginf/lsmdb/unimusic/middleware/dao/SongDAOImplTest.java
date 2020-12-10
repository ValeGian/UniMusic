package it.unipi.dii.inginf.lsmdb.unimusic.middleware.dao;

import com.mongodb.client.MongoCollection;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.Song;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.exception.ActionNotCompletedException;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.persistence.mongoconnection.Collections;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.persistence.mongoconnection.MongoDriver;
import javafx.util.Pair;
import org.bson.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

public class SongDAOImplTest {

    private SongDAOImpl song;

    @Test
    public void WHEN_crateSong_has_song_null_THEN_throws_IllegalArgumentException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            song.createSong(null);
        });
    }

    @Test
    public void WHEN_filterSong_has_partialInput_null_THEN_return_list_is_empty() throws ActionNotCompletedException {
        List<Song> listTest = song.filterSong(null, 3, "title");
        Assertions.assertTrue(listTest.size() == 0);
    }

    @Test
    public void WHEN_filterSong_has_maxNumber_negative_THEN_throws_IllegalArgumentException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            song.filterSong("as", -1, "title");
        });
    }

    @Test
    public void WHEN_filterSong_has_attributeField_null_THEN_throws_IllegalArgumentException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            song.filterSong("as", 3, null);
        });
    }

    @Test
    public void WHEN_findArtistsWithMostNumberOfHit_has_maxNumber_negative_THEN_throws_IllegalArgumentException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            song.findArtistsWithMostNumberOfHit(2, -1);
        });
    }

    @Test
    public void WHEN_getSongByID_has_songId_Null_THEN_return_NULL() throws ActionNotCompletedException {
        Song songTest = song.getSongById(null);
        Assertions.assertNull(songTest);
    }

    @Test
    public void WHEN_incrementLikeCount_has_song_null_THEN_throws_IllegalArgumentException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            song.incrementLikeCount(null);
        });
    }

    @Test
    public void WHEN_decrementLikeCount_has_song_null_THEN_throws_IllegalArgumentException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            song.decrementLikeCount(null);
        });
    }

    @BeforeEach
    public void init(){
        song = new SongDAOImpl();

    }


}
