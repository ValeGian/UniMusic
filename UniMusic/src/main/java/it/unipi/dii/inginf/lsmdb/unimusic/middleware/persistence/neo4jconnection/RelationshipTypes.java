package it.unipi.dii.inginf.lsmdb.unimusic.middleware.persistence.neo4jconnection;

public enum RelationshipTypes {
    FOLLOW_PLAYLIST("FOLLOW_PLAYLIST"),
    FOLLOW_USER("FOLLOW_USER"),
    LIKE("LIKE");

    //to be added

    private String relTypeName;
    private RelationshipTypes(String collName) {
        this.relTypeName = collName;
    }

    public String toString() {
        return relTypeName;
    }
}