package it.unipi.dii.inginf.lsmdb.unimusic.middleware.persistence.mongoconnection;

public enum Collections {
    USERS("users"),
    SONGS("songs"),
    PLAYLISTS("playlists");

    private String collName;
    private Collections(String collName) {
        this.collName = collName;
    }

    public String toString() {
        return collName;
    }
}
