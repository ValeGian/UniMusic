package it.unipi.dii.inginf.lsmdb.unimusic.middleware.persistence.neo4jconnection;

public enum LikeProperties {
    DAY("day");

    private String propertyName;
    private LikeProperties(String collName) {
        this.propertyName = collName;
    }

    public String toString() {
        return propertyName;
    }
}
