package it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities;

import org.bson.BsonArray;
import org.bson.Document;
import org.neo4j.driver.Record;

public class User {
    private String username;
    private String password = null;
    private String firstName = null;
    private String lastName = null;
    private int age = -1;
    private PrivilegeLevel privilegeLevel;

    public User(String username) {
        this.username = username;
        this.privilegeLevel = PrivilegeLevel.STANDARD_USER;
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

    public User(Record userNeo4jRecord) {
        /* solo per esempio
        String tmp;
        if((tmp = userNeo4jRecord.get(UserProperties.USERNAME.toString()).asString()) != null)
            username = tmp;
        if((tmp = userNeo4jRecord.get(UserProperties.ALTRA_PROPERTY.toString()).asString()) != null)
            altraProperty = tmp;
         */
        username = userNeo4jRecord.get("username").asString();
    }

    public User(Document userDocument) {

    }

    public Document toBsonDocument() {
        Document document = new Document("_id", username);

        if(password != null)
            document.append("password", password);
        if(firstName != null)
            document.append("firstName", firstName);
        if(lastName != null)
            document.append("lastName", lastName);
        if(age < 0)
            document.append("age", age);

        document.append("privilegeLevel", privilegeLevel.toString());
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
}
