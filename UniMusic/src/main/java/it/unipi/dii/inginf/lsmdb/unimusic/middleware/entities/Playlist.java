package it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities;

public class Playlist {
    private String author;
    private String ID;
    private String name;
    private boolean isFavourite;

    public Playlist() {

    }

    public Playlist(String author,
                    String name) {
        this.author = author;
        this.name = name;
    }

    public Playlist(String author,
                    String ID,
                    String name) {
        this.author = author;
        this.ID = ID;
        this.name = name;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public boolean isFavourite() {
        return isFavourite;
    }

    public void setFavourite(boolean favourite) {
        isFavourite = favourite;
    }
}
