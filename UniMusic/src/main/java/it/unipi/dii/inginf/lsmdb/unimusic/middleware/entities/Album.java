package it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities;

public class Album {
    private String album;
    private String albumImageURL;

    public Album() {

    }

    public Album(String album,
                 String albumImageURL) {
        this.album = album;
        this.albumImageURL = albumImageURL;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getAlbumImageURL() {
        return albumImageURL;
    }

    public void setAlbumImageURL(String albumImageURL) {
        this.albumImageURL = albumImageURL;
    }
}
