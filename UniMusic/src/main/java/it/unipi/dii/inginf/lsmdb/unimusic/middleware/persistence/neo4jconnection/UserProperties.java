package it.unipi.dii.inginf.lsmdb.unimusic.middleware.persistence.neo4jconnection;

public enum UserProperties {
    USERNAME("username");

    private String propertyName;
    private UserProperties(String collName) {
        this.propertyName = collName;
    }

    public String toString() {
        return propertyName;
    }
}