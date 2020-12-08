package it.unipi.dii.inginf.lsmdb.unimusic.middleware.persistence.neo4jconnection;

public enum Labels {
    USER("User"),
    SONG("Song"),
    PLAYLIST("Playlist");

    private String labelName;
    private Labels(String collName) {
        this.labelName = collName;
    }

    public String toString() {
        return labelName;
    }
}
