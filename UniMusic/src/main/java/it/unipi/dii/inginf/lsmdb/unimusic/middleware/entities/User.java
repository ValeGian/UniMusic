package it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities;

public class User {
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private int age;
    private PrivilegeLevel privilegeLevel;

    public User(String username) {
        this.username = username;
    }

    public User(String username,
                String password,
                String firstName,
                String lastName,
                int age) {
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
        this.privilegeLevel = PrivilegeLevel.STANDARD_USER;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public PrivilegeLevel getPrivilegeLevel() { return privilegeLevel; }

    public void setPrivilegeLevel(PrivilegeLevel privilegeLevel) { this.privilegeLevel = privilegeLevel; }
}
