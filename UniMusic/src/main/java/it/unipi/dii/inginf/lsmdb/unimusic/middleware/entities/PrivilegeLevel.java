package it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities;

public enum PrivilegeLevel {
    STANDARD_USER("standard_user"),
    ADMIN("admin");

    private String privilegeLevelName;
    private PrivilegeLevel(String privilegeLevelName) {
        this.privilegeLevelName = privilegeLevelName;
    }

    public String toString() {
        return privilegeLevelName;
    }
}
