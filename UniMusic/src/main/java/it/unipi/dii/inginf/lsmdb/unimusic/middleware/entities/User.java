package it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities;

import it.unipi.dii.inginf.lsmdb.unimusic.middleware.persistence.mongoconnection.UserFields;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.persistence.neo4jconnection.UserProperties;
import org.bson.BsonArray;
import org.bson.Document;
import org.neo4j.driver.Record;

import javax.print.Doc;

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
        username = userNeo4jRecord.get(UserProperties.USERNAME.toString()).asString();
    }

    public User(Document userDocument) {

    }

    public Document toBsonDocument() {
        Document document = new Document(UserFields.USERNAME.toString(), username);

        if(password != null)
            document.append(UserFields.PASSWORD.toString(), password);
        if(firstName != null)
            document.append(UserFields.FIRST_NAME.toString(), firstName);
        if(lastName != null)
            document.append(UserFields.LAST_NAME.toString(), lastName);
        if(age < 0)
            document.append(UserFields.AGE.toString(), age);

        document.append(UserFields.PRIVILEGE_LEVEL.toString(), privilegeLevel.toString());
        document.append(UserFields.CREATED_PLAYLISTS.toString(), new BsonArray());

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
