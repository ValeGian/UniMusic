package it.unipi.dii.inginf.lsmdb.unimusic.middleware.dao;

import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.Song;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.exception.ActionNotCompletedException;

/**
 * Provides CRUD operations for Songs
 */
public interface SongDAO {

    public void createSong(Song song) throws ActionNotCompletedException;

    public Song getSong(String songID) throws ActionNotCompletedException;

}
