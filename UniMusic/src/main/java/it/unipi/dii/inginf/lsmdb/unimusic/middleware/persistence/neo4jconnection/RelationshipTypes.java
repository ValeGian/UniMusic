package it.unipi.dii.inginf.lsmdb.unimusic.middleware.persistence.neo4jconnection;

public enum RelationshipTypes {
    ESEMPIO("ESEMPIO");
    //to be added

    private String relTypeName;
    private RelationshipTypes(String collName) {
        this.relTypeName = collName;
    }

    public String toString() {
        return relTypeName;
    }
}