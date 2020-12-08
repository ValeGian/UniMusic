package it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities;

public class Album {
    private String title;
    private String image;

    public Album() {

    }

    public Album(String title,
                 String image) {
        this.title = title;
        this.image = image;
    }

    public Album(String image){
        this(null, image);
    }

    public String getTitle() { return title; }

    public void setTitle(String title) { this.title = title; }

    public String getImage() { return image; }

    public void setImage(String image) { this.image = image; }
}
