package it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities;

import org.bson.BsonArray;
import org.bson.Document;
import org.neo4j.driver.Record;
import org.neo4j.driver.Value;

public class User {
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private int age;
    private PrivilegeLevel privilegeLevel;
    private String country;

    public User(String username) {
        this.username = username;
        this.privilegeLevel = PrivilegeLevel.STANDARD_USER;
    }

    public User(String username,
                String password,
                String firstName,
                String lastName,
                int age,
                String country) {
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
        this.privilegeLevel = PrivilegeLevel.STANDARD_USER;
        this.country = country;
    }

    public User(Record userNeo4jRecord) {
        username = userNeo4jRecord.get("username").asString();
    }

    public User(Value userNeo4jValue) {
        username = userNeo4jValue.get("username").asString();
    }

    public User(Document userDocument) {
        username = userDocument.get("_id").toString();
        password = userDocument.get("password").toString();
        firstName = userDocument.get("firstName").toString();
        lastName = userDocument.get("lastName").toString();
        age = Integer.parseInt(userDocument.get("age").toString());
        privilegeLevel = PrivilegeLevel.getPrivilegeLevel(userDocument.getString("privilegeLevel"));
        //country = userDocument.get("country").toString();
    }

    public Document toBsonDocument() {
        Document document = new Document("_id", username);

        document.append("password", password);
        document.append("firstName", firstName);
        document.append("lastName", lastName);
        document.append("age", age);
        document.append("privilegeLevel", privilegeLevel.toString());
        document.append("country", country);
        document.append("createdPlaylists", new BsonArray());

        return document;
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

    public String getCountry() {return country; }

    public void setCountry(String country) { this.country = country; }
}
