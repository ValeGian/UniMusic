package it.unipi.dii.inginf.lsmdb.unimusic.middleware.persistence.mongoconnection;

public enum UserFields {
    USERNAME("_id"),
    PASSWORD("password"),
    FIRST_NAME("firstName"),
    LAST_NAME("lastName"),
    AGE("age"),
    PRIVILEGE_LEVEL("privilegeLevel");

    private String userFieldName;
    private UserFields(String collName) {
        this.userFieldName = collName;
    }

    public String toString() {
        return userFieldName;
    }
}
