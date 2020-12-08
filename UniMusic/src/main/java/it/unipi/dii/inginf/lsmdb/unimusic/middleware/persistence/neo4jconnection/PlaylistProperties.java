package it.unipi.dii.inginf.lsmdb.unimusic.middleware.persistence.neo4jconnection;

public enum PlaylistProperties {
    ID("ID");

    private String propertyName;
    private PlaylistProperties(String collName) {
        this.propertyName = collName;
    }

    public String toString() {
        return propertyName;
    }
}
