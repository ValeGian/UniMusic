package it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities;

public enum PrivilegeLevel {
    STANDARD_USER("standard_user"),
    ADMIN("admin");

    private final String privilegeLevelName;
    PrivilegeLevel(String privilegeLevelName) {
        this.privilegeLevelName = privilegeLevelName;
    }

    public String toString() {
        return privilegeLevelName;
    }

    public static PrivilegeLevel getPrivilegeLevel(String privilegeLevelName) {
        switch (privilegeLevelName) {
            case "admin":
                return PrivilegeLevel.ADMIN;
            case "standard_user":
            default:
                return PrivilegeLevel.STANDARD_USER;
        }
    }
}
