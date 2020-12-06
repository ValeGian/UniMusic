package it.unipi.dii.inginf.lsmdb.unimusic.middleware.dao;

import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.Song;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.exception.ActionNotCompletedException;

public class SongDAOImpl implements SongDAO{

    @Override
    public void createSong(Song song)  throws ActionNotCompletedException{

    }

    @Override
    public Song getSong(long songID)  throws ActionNotCompletedException{
        return null;
    }

    //---------------------------------------------------------------------------------------------

    private void createSongDocument(Song song) {

    }

    private void createSongNode(Song song) {

    }
}
