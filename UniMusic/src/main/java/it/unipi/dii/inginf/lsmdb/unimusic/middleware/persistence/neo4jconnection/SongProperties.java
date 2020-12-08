package it.unipi.dii.inginf.lsmdb.unimusic.middleware.persistence.neo4jconnection;

public enum SongProperties {
    ID("songId"),
    TITLE("title"),
    ARTIST("artist"),
    IMAGE("imageUrl");

    private String propertyName;
    private SongProperties(String collName) {
        this.propertyName = collName;
    }

    public String toString() {
        return propertyName;
    }
}
