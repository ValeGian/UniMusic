package it.unipi.dii.inginf.lsmdb.unimusic.middleware.dao;

import it.unipi.dii.inginf.lsmdb.unimusic.middleware.exception.ActionNotCompletedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class SongDAOImplTest {

    private SongDAOImpl song;

    @Test
    public void WHEN_crateSong_has_song_null_THEN_throws_IllegalArgumentException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            song.createSong(null);
        });
    }

    @Test
    public void WHEN_filterSong_has_partialInput_null_THEN_return_empty_list() throws ActionNotCompletedException {
        Assertions.assertTrue(song.filterSong(null, 3, "title").size() == 0);
    }

    @Test
    public void WHEN_filterSong_has_maxNumber_non_positive_THEN_throws_IllegalArgumentException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            song.filterSong("test", 0, "title");
        });
    }

    @Test
    public void WHEN_filterSong_has_attributeField_null_THEN_throws_IllegalArgumentException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            song.filterSong("test", 3, null);
        });
    }

    @Test
    public void WHEN_filterSong_has_attributeField_that_does_not_exist_THEN_return_empty_list() {
        try {
            Assertions.assertTrue(song.filterSong("test", 3, "test").size() == 0);
        } catch (ActionNotCompletedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void WHEN_findArtistsWithMostNumberOfHit_has_maxNumber_non_positive_THEN_throws_IllegalArgumentException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            song.findArtistsWithMostNumberOfHit(2, 0);
        });
    }

    @Test
    public void WHEN_getSongByID_has_songId_Null_THEN_return_NULL() {
        Assertions.assertNull(song.getSongById(null));
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
