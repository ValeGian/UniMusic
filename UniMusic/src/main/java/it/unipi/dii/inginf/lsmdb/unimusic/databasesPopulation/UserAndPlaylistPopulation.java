package it.unipi.dii.inginf.lsmdb.unimusic.databasesPopulation;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import it.unipi.dii.inginf.lsmdb.unimusic.frontend.MiddlewareConnector;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.dao.UserDAO;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.dao.UserDAOImpl;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.Playlist;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.Song;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.User;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.exception.ActionNotCompletedException;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.persistence.mongoconnection.Collections;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.persistence.mongoconnection.MongoDriver;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.time.Year;
import java.util.Arrays;
import java.util.Random;

import static com.mongodb.client.model.Aggregates.sample;

public class UserAndPlaylistPopulation {
    private static MiddlewareConnector connector = MiddlewareConnector.getInstance();

    public static void main(String[] args) throws ActionNotCompletedException {
        UserAndPlaylistPopulation instance = new UserAndPlaylistPopulation();

        /*
        instance.populateWithUser(400);
        instance.completelyRandomLikes(3000);
        instance.completelyRandomUserFollows(2000);
         */

        UserDAO userDAO = new UserDAOImpl();
        Playlist playlist1 = new Playlist(); playlist1.setID("5fd520bb5c73c35dc654fd80");
        Playlist playlist2 = new Playlist(); playlist2.setID("5fd520bb5c73c35dc654fd7f");
        Playlist playlist3 = new Playlist(); playlist3.setID("5fd520bb5c73c35dc654fd7e");
        Playlist playlist4 = new Playlist(); playlist4.setID("5fd520bb5c73c35dc654fd7d");
        Playlist playlist5 = new Playlist(); playlist5.setID("5fd520bb5c73c35dc654fd7c");

        userDAO.followPlaylist(instance.getRandomUser(), playlist1);
        userDAO.followPlaylist(instance.getRandomUser(), playlist1);
        userDAO.followPlaylist(instance.getRandomUser(), playlist2);
        userDAO.followPlaylist(instance.getRandomUser(), playlist2);
        userDAO.followPlaylist(instance.getRandomUser(), playlist2);
        userDAO.followPlaylist(instance.getRandomUser(), playlist2);
        userDAO.followPlaylist(instance.getRandomUser(), playlist3);
        userDAO.followPlaylist(instance.getRandomUser(), playlist4);
        userDAO.followPlaylist(instance.getRandomUser(), playlist4);
        userDAO.followPlaylist(instance.getRandomUser(), playlist5);
        userDAO.followPlaylist(instance.getRandomUser(), playlist5);
        userDAO.followPlaylist(instance.getRandomUser(), playlist5);
        userDAO.followPlaylist(instance.getRandomUser(), playlist5);
        userDAO.followPlaylist(instance.getRandomUser(), playlist5);
        userDAO.followPlaylist(instance.getRandomUser(), playlist5);

    }

    private void populateWithUser(int howManyUsers){
        Random generator = new Random();

        String[] firstName =  new String[] {"Emily","Hannah","Madison","Ashley","Sarah","Alexis","Samantha","Jessica","Elizabeth","Taylor","Lauren","Alyssa","Kayla","Abigail","Brianna","Olivia","Emma","Megan","Grace","Victoria","Rachel","Anna","Sydney","Destiny","Morgan","Jennifer","Jasmine","Haley","Julia","Kaitlyn","Nicole","Amanda","Katherine","Natalie","Hailey","Alexandra","Adam", "Alex", "Aaron", "Ben", "Carl", "Dan", "David", "Edward", "Fred", "Frank", "George", "Hal", "Hank", "Ike", "John", "Jack", "Joe", "Larry", "Monte", "Matthew", "Mark", "Nathan", "Otto", "Paul", "Peter", "Roger", "Roger", "Steve", "Thomas", "Tim", "Ty", "Victor", "Walter"};

        String[] lastName = new String[] {"Anderson", "Ashwoon", "Aikin", "Bateman", "Bongard", "Bowers", "Boyd", "Cannon", "Cast", "Deitz", "Dewalt", "Ebner", "Frick", "Hancock", "Haworth", "Hesch", "Hoffman", "Kassing", "Knutson", "Lawless", "Lawicki", "Mccord", "McCormack", "Miller", "Myers", "Nugent", "Ortiz", "Orwig", "Ory", "Paiser", "Pak", "Pettigrew", "Quinn", "Quizoz", "Ramachandran", "Resnick", "Sagar", "Schickowski", "Schiebel", "Sellon", "Severson", "Shaffer", "Solberg", "Soloman", "Sonderling", "Soukup", "Soulis", "Stahl", "Sweeney", "Tandy", "Trebil", "Trusela", "Trussel", "Turco", "Uddin", "Uflan", "Ulrich", "Upson", "Vader", "Vail", "Valente", "Van Zandt", "Vanderpoel", "Ventotla", "Vogal", "Wagle", "Wagner", "Wakefield", "Weinstein", "Weiss", "Woo", "Yang", "Yates", "Yocum", "Zeaser", "Zeller", "Ziegler", "Bauer", "Baxster", "Casal", "Cataldi", "Caswell", "Celedon", "Chambers", "Chapman", "Christensen", "Darnell", "Davidson", "Davis", "DeLorenzo", "Dinkins", "Doran", "Dugelman", "Dugan", "Duffman", "Eastman", "Ferro", "Ferry", "Fletcher", "Fietzer", "Hylan", "Hydinger", "Illingsworth", "Ingram", "Irwin", "Jagtap", "Jenson", "Johnson", "Johnsen", "Jones", "Jurgenson", "Kalleg", "Kaskel", "Keller", "Leisinger", "LePage", "Lewis", "Linde", "Lulloff", "Maki", "Martin", "McGinnis", "Mills", "Moody", "Moore", "Napier", "Nelson", "Norquist", "Nuttle", "Olson", "Ostrander", "Reamer", "Reardon", "Reyes", "Rice", "Ripka", "Roberts", "Rogers", "Root", "Sandstrom", "Sawyer", "Schlicht", "Schmitt", "Schwager", "Schutz", "Schuster", "Tapia", "Thompson", "Tiernan", "Tisler"};

        for(int i = 0; i < howManyUsers; i++){
            String fName = firstName[generator.nextInt(firstName.length)];
            String lName = lastName[generator.nextInt(lastName.length)];
            int age = generator.nextInt(30) + 20;
            String username;
            String password = "";
            for(int j = 0; j < 6; j++){
                password += (char) (generator.nextInt(26) + 'a');
            }
            password = Integer.toString(generator.nextInt(100));

            try {
                username = fName.substring(0, 3) + lName.substring(0, 3) + (Year.now().getValue() - age);
            }catch (IndexOutOfBoundsException index){
                continue;
            }
            User user = new User(username, password,fName, lName, age);

            UserDAOImpl userDAO = new UserDAOImpl();

            try {
                userDAO.createUser(user);
            } catch (ActionNotCompletedException e) {
                e.printStackTrace();
            }
        }

    }

    public Song getRandomSong() throws ActionNotCompletedException{
        MongoCollection<Document> songsCollection = MongoDriver.getInstance().getCollection(Collections.SONGS);
        Song song = null;

        Bson sample = sample(1);

        try (MongoCursor<Document> cursor = songsCollection.aggregate(Arrays.asList(sample)).iterator()) {
            if(cursor.hasNext()) {
                Document result = cursor.next();
                song = new Song(result.toJson());
            }
        }catch (MongoException mongoEx) {
            System.out.println(mongoEx.getMessage());
            throw new ActionNotCompletedException(mongoEx);
        }
        return song;
    }

    public User getRandomUser() throws ActionNotCompletedException{
        MongoCollection<Document> usersCollection = MongoDriver.getInstance().getCollection(Collections.USERS);
        User user = null;

        Bson sample = sample(1);

        try (MongoCursor<Document> cursor = usersCollection.aggregate(Arrays.asList(sample)).iterator()) {
            if(cursor.hasNext()) {
                Document result = cursor.next();
                user = new User(result);
            }
        }catch (MongoException mongoEx) {
            System.out.println(mongoEx.getMessage());
            throw new ActionNotCompletedException(mongoEx);
        }

        return user;
    }

    //select some random users and add random number of playlist to them, with a random number of random songs
    public void createRandomPlaylist(int numUsers, int maxNumPlaylistsPerUser, int maxNumSongsPerPlaylists) throws ActionNotCompletedException{
        Random random = new Random();
        for (int i = 0; i < numUsers; i++){
            User user = getRandomUser();
            System.out.println(user.getUsername());
            int numPlaylists = random.nextInt(maxNumPlaylistsPerUser);
            for (int j = 0; j < numPlaylists; j++){
                Playlist playlist = new Playlist(user.getUsername(), "Playlist " + (j + 1) + " by " + user.getFirstName());
                connector.createPlaylist(playlist);
                addRandomSongs(playlist, random.nextInt(maxNumSongsPerPlaylists));
            }
        }
    }

    public void addRandomSongs(Playlist playlist, int numSong) throws ActionNotCompletedException{
        for (int i = 0; i < numSong; i++){
            Song song = getRandomSong();
            connector.addSong(playlist, song);
        }
    }

    //put some likes to random songs from a user
    public void likeRandomSongs(User user, int numLikes) throws ActionNotCompletedException{
        UserDAO userDao = new UserDAOImpl();

        for (int i = 0; i < numLikes; i++)
            userDao.likeSong(user, getRandomSong());
    }

    //put some likes to random songs from random users
    public void completelyRandomLikes(int numLikes) throws ActionNotCompletedException{
        UserDAO userDao = new UserDAOImpl();

        for (int i = 0; i < numLikes; i++)
            userDao.likeSong(getRandomUser(), getRandomSong());
    }

    public void completelyRandomUserFollows(int numFollows) throws ActionNotCompletedException {
        UserDAO userDAO = new UserDAOImpl();

        for (int i = 0; i < numFollows; i++) {
            userDAO.followUser(getRandomUser(), getRandomUser());

        }
    }

}
