package it.unipi.dii.inginf.lsmdb.unimusic.middleware.dao;

import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.Playlist;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.Song;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.User;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.exception.ActionNotCompletedException;

public class PlaylistDAOImpl implements PlaylistDAO{

    @Override
    public void createPlaylist(Playlist playlist)  throws ActionNotCompletedException{

    }

    @Override
    public Playlist getPlaylist(long playlistID)  throws ActionNotCompletedException{
        return null;
    }

    @Override
    public Playlist getFavourite(User user) throws ActionNotCompletedException {
        return null;
    }

    @Override
    public void addSong(Playlist playlist, Song song)  throws ActionNotCompletedException{

    }

    @Override
    public void deletePlaylist(Playlist playlist)  throws ActionNotCompletedException{

    }

    //---------------------------------------------------------------------------------------------

    private void createPlaylistDocument(Playlist playlist) {

    }

    private void createPlaylistNode(Playlist playlist) {

    }
}
