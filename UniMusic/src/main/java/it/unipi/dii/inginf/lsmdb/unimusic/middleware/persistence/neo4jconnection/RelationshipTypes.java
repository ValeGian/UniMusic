package it.unipi.dii.inginf.lsmdb.unimusic.middleware.persistence.neo4jconnection;

public enum RelationshipTypes {
    FOLLOW_USER("FOLLOWS_USER"),
    FOLLOW_PLAYLIST("FOLLOWS_PLAYLIST"),
    LIKE("LIKES");

    private String relTypeName;
    private RelationshipTypes(String collName) {
        this.relTypeName = collName;
    }

    public String toString() {
        return relTypeName;
    }
}