package it.unipi.dii.inginf.lsmdb.unimusic.databasesPopulation;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import it.unipi.dii.inginf.lsmdb.unimusic.frontend.MiddlewareConnector;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.dao.*;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.Playlist;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.Song;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.User;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.exception.ActionNotCompletedException;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.persistence.mongoconnection.Collections;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.persistence.mongoconnection.MongoDriver;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.persistence.neo4jconnection.Neo4jDriver;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.TransactionWork;

import java.time.Year;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

import static com.mongodb.client.model.Aggregates.sample;
import static org.neo4j.driver.Values.parameters;

public class UserAndPlaylistPopulation {
    private static final MiddlewareConnector connector = MiddlewareConnector.getInstance();

    public static void main(String[] args) { }

    private void populateWithUser(int howManyUsers){
        Random generator = new Random();

        String[] firstName =  new String[] {"Emily","Hannah","Madison","Ashley","Sarah","Alexis","Samantha","Jessica","Elizabeth","Taylor","Lauren","Alyssa","Kayla","Abigail","Brianna","Olivia","Emma","Megan","Grace","Victoria","Rachel","Anna","Sydney","Destiny","Morgan","Jennifer","Jasmine","Haley","Julia","Kaitlyn","Nicole","Amanda","Katherine","Natalie","Hailey","Alexandra","Adam", "Alex", "Aaron", "Ben", "Carl", "Dan", "David", "Edward", "Fred", "Frank", "George", "Hal", "Hank", "Ike", "John", "Jack", "Joe", "Larry", "Monte", "Matthew", "Mark", "Nathan", "Otto", "Paul", "Peter", "Roger", "Roger", "Steve", "Thomas", "Tim", "Ty", "Victor", "Walter", "Alessio", "Valerio", "Lorenzo", "Giacomo", "Marco", "Mario", "Salvatore"};

        String[] lastName = new String[] {"Anderson", "Ashwoon", "Aikin", "Bateman", "Bongard", "Bowers", "Boyd", "Cannon", "Cast", "Deitz", "Dewalt", "Ebner", "Frick", "Hancock", "Haworth", "Hesch", "Hoffman", "Kassing", "Knutson", "Lawless", "Lawicki", "Mccord", "McCormack", "Miller", "Myers", "Nugent", "Ortiz", "Orwig", "Ory", "Paiser", "Pak", "Pettigrew", "Quinn", "Quizoz", "Ramachandran", "Resnick", "Sagar", "Schickowski", "Schiebel", "Sellon", "Severson", "Shaffer", "Solberg", "Soloman", "Sonderling", "Soukup", "Soulis", "Stahl", "Sweeney", "Tandy", "Trebil", "Trusela", "Trussel", "Turco", "Uddin", "Uflan", "Ulrich", "Upson", "Vader", "Vail", "Valente", "Van Zandt", "Vanderpoel", "Ventotla", "Vogal", "Wagle", "Wagner", "Wakefield", "Weinstein", "Weiss", "Woo", "Yang", "Yates", "Yocum", "Zeaser", "Zeller", "Ziegler", "Bauer", "Baxster", "Casal", "Cataldi", "Caswell", "Celedon", "Serra", "Giannini", "Binchi", "Rossi", "Chambers", "Chapman", "Christensen", "Darnell", "Davidson", "Davis", "DeLorenzo", "Dinkins", "Doran", "Dugelman", "Dugan", "Duffman", "Eastman", "Ferro", "Ferry", "Fletcher", "Fietzer", "Hylan", "Hydinger", "Illingsworth", "Ingram", "Irwin", "Jagtap", "Jenson", "Johnson", "Johnsen", "Jones", "Jurgenson", "Kalleg", "Kaskel", "Keller", "Leisinger", "LePage", "Lewis", "Linde", "Lulloff", "Maki", "Martin", "McGinnis", "Mills", "Moody", "Moore", "Napier", "Nelson", "Norquist", "Nuttle", "Olson", "Ostrander", "Reamer", "Reardon", "Reyes", "Rice", "Ripka", "Roberts", "Rogers", "Root", "Sandstrom", "Sawyer", "Schlicht", "Schmitt", "Schwager", "Schutz", "Schuster", "Tapia", "Thompson", "Tiernan", "Tisler"};

        String[] countries = {"Afghanistan","Albania","Algeria","Andorra","Angola","Anguilla","Antigua-Barbuda","Argentina","Armenia","Aruba","Australia","Austria","Azerbaijan","Bahamas","Bahrain","Bangladesh","Barbados","Belarus","Belgium","Belize","Benin","Bermuda","Bhutan","Bolivia","Bosnia-Herzegovina","Botswana","Brazil","British Virgin Islands","Brunei","Bulgaria","Burkina Faso","Burundi","Cambodia","Cameroon","Cape Verde","Cayman Islands","Chad","Chile","China","Colombia","Congo","Cook Islands","Costa Rica","Cote D Ivoire","Croatia","Cruise Ship","Cuba","Cyprus","Czech Republic","Denmark","Djibouti","Dominica","Dominican Republic","Ecuador","Egypt","El Salvador","Equatorial Guinea","Estonia","Ethiopia","Falkland Islands","Faroe Islands","Fiji","Finland","France","French Polynesia","French West Indies","Gabon","Gambia","Georgia","Germany","Ghana","Gibraltar","Greece","Greenland","Grenada","Guam","Guatemala","Guernsey","Guinea","Guinea Bissau","Guyana","Haiti","Honduras","Hong Kong","Hungary","Iceland","India","Indonesia","Iran","Iraq","Ireland","Isle of Man","Israel","Italy","Jamaica","Japan","Jersey","Jordan","Kazakhstan","Kenya","Kuwait","Kyrgyz Republic","Laos","Latvia","Lebanon","Lesotho","Liberia","Libya","Liechtenstein","Lithuania","Luxembourg","Macau","Macedonia","Madagascar","Malawi","Malaysia","Maldives","Mali","Malta","Mauritania","Mauritius","Mexico","Moldova","Monaco","Mongolia","Montenegro","Montserrat","Morocco","Mozambique","Namibia","Nepal","Netherlands","Netherlands Antilles","New Caledonia","New Zealand","Nicaragua","Niger","Nigeria","Norway","Oman","Pakistan","Palestine","Panama","Papua New Guinea","Paraguay","Peru","Philippines","Poland","Portugal","Puerto Rico","Qatar","Reunion","Romania","Russia","Rwanda","Saint Pierre-Miquelon","Samoa","San Marino","Satellite","Saudi Arabia","Senegal","Serbia","Seychelles","Sierra Leone","Singapore","Slovakia","Slovenia","South Africa","South Korea","Spain","Sri Lanka","St Kitts-Nevis","St Lucia","St Vincent","St. Lucia","Sudan","Suriname","Swaziland","Sweden","Switzerland","Syria","Taiwan","Tajikistan","Tanzania","Thailand","Timor L'Este","Togo","Tonga","Trinidad-Tobago","Tunisia","Turkey","Turkmenistan","Turks-Caicos","Uganda","Ukraine","United Arab Emirates","United Kingdom","Uruguay","Uzbekistan","Venezuela","Vietnam","Virgin Islands (US)","Yemen","Zambia","Zimbabwe"};


        for(int i = 0; i < howManyUsers; i++){
            String fName = firstName[generator.nextInt(firstName.length)];
            String lName = lastName[generator.nextInt(lastName.length)];
            int age = generator.nextInt(57) + 18;
            String username;
            String password = "";
            for(int j = 0; j < 6; j++){
                password += (char) (generator.nextInt(26) + 'a');
            }
            password = Integer.toString(generator.nextInt(100));
            String country = countries[generator.nextInt(countries.length)];

            try {
                username = fName.substring(0, 3) + lName.substring(0, 3) + (Year.now().getValue() - age);
            }catch (IndexOutOfBoundsException index){
                continue;
            }
            User user = new User(username, password,fName, lName, age, country);

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
                song = new Song(cursor.next());
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
                playlist.setUrlImage(getRandomSong().getAlbum().getImage());
                connector.createPlaylist(playlist);
                addRandomSongs(playlist, random.nextInt(maxNumSongsPerPlaylists));
            }
        }
    }

    public void addRandomSongs(Playlist playlist, int numSong) throws ActionNotCompletedException{

        for (int i = 0; i < numSong; i++){
            Song song;
            do {
                song = getRandomSong();
            }while(song.getAlbum().getImage() == null && song.getArtist().equals("Gucci Mane"));

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

    public Playlist getRandomPlaylist() {
        Playlist playlist;
        try ( Session session = Neo4jDriver.getInstance().getDriver().session() )
        {
            playlist = session.readTransaction((TransactionWork<Playlist>) tx -> {
                Result result = tx.run(     "MATCH (p:Playlist) \n" +
                                "RETURN p, rand() as r\n" +
                                "ORDER BY r\n" +
                                "LIMIT 1;");
                return new Playlist(result.next().get("p"));
            });
        }
        return playlist;
    }

    public void completelyRandomPlaylistFollow(int numFollow) throws ActionNotCompletedException{
        UserDAO userDAO = new UserDAOImpl();
        for (int i = 0; i < numFollow; i++)
            userDAO.followPlaylist(getRandomUser(), getRandomPlaylist());
    }

    private void resolveSongInconsistencies() {

        MongoCollection<Document> songCollection = MongoDriver.getInstance().getCollection(Collections.SONGS);
        SongDAO songDAO = new SongDAOImpl();
        try (MongoCursor<Document> cursor = songCollection.find().iterator()) {
            while (cursor.hasNext()) {
                Song mongoSong = new Song(cursor.next());
                try (Session session = Neo4jDriver.getInstance().getDriver().session()) {
                    String query = "MATCH (s:Song) " +
                            "WHERE s.songId = $songId " +
                            "RETURN s.songId as songId " ;
                    Result result = session.run(query, parameters("songId", mongoSong.getID()));

                    if(!result.hasNext()){
                        System.out.println("Sure to delete id: " + mongoSong.getID());
                        System.out.print("> ");
                        String response = new Scanner(System.in).nextLine();
                        if(response.equals("yes"))
                            songDAO.deleteSongDocument(mongoSong);
                        else
                            System.out.println("Not deleted");
                    }
                }
            }
        }
    }
/*
    private void resolveNode() throws ActionNotCompletedException {
        try (Session session = Neo4jDriver.getInstance().getDriver().session()) {
            Result result = session.run("MATCH (u:User) RETURN u.username AS username");
            UserDAO userDAO = new UserDAOImpl();
            while(result.hasNext()) {
                System.out.println("O");
                String username = result.next().get("username").asString();
                User user = userDAO.getUserByUsername(username);
                if (user == null) {
                    user = new User(username);
                    userDAO.deleteUserNode(user);
                    System.out.println("cancellato");
                }
            }
        }catch (Neo4jException neo4){
            neo4.printStackTrace();
        }
    }

 */
    private void resolvePlaylistInconsistencies() throws ActionNotCompletedException {

        MongoCollection<Document> songCollection = MongoDriver.getInstance().getCollection(Collections.USERS);
        PlaylistDAO playlistDAO = new PlaylistDAOImpl();
        try (MongoCursor<Document> cursor = songCollection.find().iterator()) {
            while (cursor.hasNext()) {
                Playlist mongoPlaylist = new Playlist(cursor.next());
                try (Session session = Neo4jDriver.getInstance().getDriver().session()) {
                    String query = "MATCH (p:Playlist) " +
                            "WHERE p.playlistId = $playlistId " +
                            "RETURN p.playlistId as p.playlistId " ;
                    Result result = session.run(query, parameters("playlistId", mongoPlaylist.getID()));

                    if(!result.hasNext()){
                        System.out.println("Sure to delete id: " + mongoPlaylist.getID());
                        System.out.print("> ");
                        String response = new Scanner(System.in).nextLine();
                        if(response.equals("yes"))
                            playlistDAO.deletePlaylistDocument(mongoPlaylist);
                        else
                            System.out.println("Not deleted");
                    }
                }
            }
        }
    }

    private void resolveUserInconsistencies() {

        MongoCollection<Document> songCollection = MongoDriver.getInstance().getCollection(Collections.USERS);
        UserDAO userDAO = new UserDAOImpl();
        try (MongoCursor<Document> cursor = songCollection.find().iterator()) {
            while (cursor.hasNext()) {
                User mongoUser = new User(cursor.next());
                try (Session session = Neo4jDriver.getInstance().getDriver().session()) {
                    String query = "MATCH (u:User) " +
                            "WHERE u.username = $username " +
                            "RETURN u.username as username " ;
                    Result result = session.run(query, parameters("username", mongoUser.getUsername()));

                    if(!result.hasNext()){
                        System.out.println("Sure to delete id: " + mongoUser.getUsername());
                        userDAO.deleteUserDocument(mongoUser);
                        System.out.println("deleted");
                    }
                }
            }
        }
    }


}
