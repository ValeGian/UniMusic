package it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities;

import org.bson.Document;
import org.json.JSONObject;
import org.neo4j.driver.Value;

public class Playlist {
    private String author;
    private String ID = null;
    private String name;
    private String urlImage = null;
    private boolean isFavourite = false;

    public Playlist() {

    }

    public Playlist(String author) {
        this.author = author;
        this.name = "My playlist";
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

    public Playlist(String author,
                    String ID,
                    String name,
                    String urlImage) {
        this.author = author;
        this.ID = ID;
        this.name = name;
        this.urlImage = urlImage;
    }

    public Playlist(Document mongoDocument){
        String json = mongoDocument.toJson();
        JSONObject jsonObject = new JSONObject(json);
        ID = jsonObject.getString("playlistId");
        name = jsonObject.getString("name");
        if (jsonObject.has("isFavourite"))
            isFavourite = true;
        if (jsonObject.has("urlImage"))
            urlImage = jsonObject.getString("urlImage");
    }

    public Playlist(Document mongoDocument,
                    String author){
        this(mongoDocument);
        this.author = author;
    }

    public Playlist(Value playlistNeo4jValue) {
        ID = playlistNeo4jValue.get("playlistId").asString();
        name = playlistNeo4jValue.get("name").asString();
        urlImage = playlistNeo4jValue.get("urlImage").asString();
    }

    public Document toBsonDocument() {
        Document document = new Document("playlistId", ID);

        document.append("name", name);
        if (isFavourite)
            document.append("isFavourite", true);
        if (urlImage != null)
            document.append("urlImage", urlImage);

        return document;
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

    public String getUrlImage() {
        return urlImage;
    }

    public void setUrlImage(String urlImage) {
        this.urlImage = urlImage;
    }

    public boolean isFavourite() {
        return isFavourite;
    }

    public void setFavourite(boolean favourite) {
        isFavourite = favourite;
    }
}
