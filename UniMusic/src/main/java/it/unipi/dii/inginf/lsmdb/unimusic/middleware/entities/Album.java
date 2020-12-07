package it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities;

public class Album {
    private String title;
    private String imageURL;

    public Album() {

    }

    public Album(String title,
                 String imageURL) {
        this.title = title;
        this.imageURL = imageURL;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) { this.title = title; }

    public String getImageURL() { return imageURL; }

    public void setImageURL(String imageURL) { this.imageURL = imageURL; }
}
