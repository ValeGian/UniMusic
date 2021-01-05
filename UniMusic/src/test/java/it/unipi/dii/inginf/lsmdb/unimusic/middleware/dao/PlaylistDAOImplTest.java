package it.unipi.dii.inginf.lsmdb.unimusic.middleware.dao;

import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.Song;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.User;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.exception.ActionNotCompletedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PlaylistDAOImplTest {
    private PlaylistDAO playlistDAO;

    @Test
    public void WHEN_getPlaylist_has_playlistID_null_THEN_return_null() {
        try {
            Assertions.assertNull(playlistDAO.getPlaylist(null));
        } catch (ActionNotCompletedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void WHEN_getFavourite_has_user_null_THEN_return_null() {
        try {
            Assertions.assertNull(playlistDAO.getFavourite(null));
        } catch (ActionNotCompletedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void WHEN_isSongFavourite_has_user_null_THEN_return_false() {
        Assertions.assertFalse(playlistDAO.isSongFavourite(null, new Song()));
    }

    @Test
    public void WHEN_isSongFavourite_has_song_null_THEN_return_false() {
        Assertions.assertFalse(playlistDAO.isSongFavourite(new User(""), null));
    }

    @Test
    public void WHEN_getAllSongs_has_playlist_null_THEN_return_empty_list() {
        try {
            Assertions.assertTrue(playlistDAO.getAllSongs(null).size() == 0);
        } catch (ActionNotCompletedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void WHEN_getSuggestedPlaylists_has_user_null_THEN_return_empty_list() {
        try {
            Assertions.assertTrue(playlistDAO.getSuggestedPlaylists(null).size() == 0);
        } catch (ActionNotCompletedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void WHEN_getSuggestedPlaylists_has_limit_non_positive_THEN_return_empty_list() {
            try {
                Assertions.assertTrue(playlistDAO.getSuggestedPlaylists(new User(""), -1).size() == 0);
            } catch (ActionNotCompletedException e) {
                e.printStackTrace();
            }
        }

    @BeforeEach
    public void init(){
        playlistDAO = new PlaylistDAOImpl();
    }
}
