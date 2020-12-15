package it.unipi.dii.inginf.lsmdb.unimusic.frontend;

import it.unipi.dii.inginf.lsmdb.unimusic.middleware.dao.*;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.*;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.exception.ActionNotCompletedException;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.persistence.mongoconnection.MongoDriver;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.persistence.neo4jconnection.Neo4jDriver;

import java.util.ArrayList;
import java.util.List;
import javafx.util.Pair;

public class MiddlewareConnector {
    private static final MiddlewareConnector instance = new MiddlewareConnector();

    private final UserDAO userDAO = new UserDAOImpl();
    private final PlaylistDAO playlistDAO = new PlaylistDAOImpl();
    private final SongDAO songDAO = new SongDAOImpl();

    private User loggedUser = new User("AleLew1996", "43", "Alex", "Lewis", 24);
    //private User loggedUser = new User("valegiann", "root", "Valerio", "Giannini", 22);
    private MiddlewareConnector() {
        loggedUser.setPrivilegeLevel(PrivilegeLevel.ADMIN);
    }

    public static MiddlewareConnector getInstance() { return instance; }

    public void closeApplication() {
        MongoDriver.getInstance().closeConnection();
        Neo4jDriver.getInstance().closeDriver();
    }

    public User getLoggedUser() { return loggedUser; }

    //-----------------USER-------------------------------------------------------------------

    public void registerUser(String username,
                             String password,
                             String firstName,
                             String lastName,
                             int age) throws ActionNotCompletedException {
        User user = new User(username, password, firstName, lastName, age);
        userDAO.createUser(user);
    }

    public boolean loginUser(String username, String password) {
        if(userDAO.checkUserPassword(username, password)) {
            try {
                loggedUser = userDAO.getUserByUsername(username);
            } catch (ActionNotCompletedException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
        return false;
    }

    public User getUser(User user) throws ActionNotCompletedException {return userDAO.getUserByUsername(user.getUsername()); }


    public List<User> getUsersByPartialInput(String partialUsername) throws ActionNotCompletedException {return userDAO.getUserByPartialUsername(partialUsername);}


    public void logout() {
        loggedUser = null;
    }


    public void deleteUser(User user) throws ActionNotCompletedException{userDAO.deleteUser(user);}


    public List<User> getSuggestedUsers() {
        List<User> suggUsers;
        try {
            suggUsers = userDAO.getSuggestedUsers(loggedUser);
        } catch (ActionNotCompletedException ancEx) {
            ancEx.printStackTrace();
            return new ArrayList<User>();
        }
        return suggUsers;
    }


    public List<User> getFollowedUsers(User user) {
        List<User> followedUsers;
        try {
            followedUsers = userDAO.getFollowedUsers(user);
        } catch (ActionNotCompletedException ancEx) {
            ancEx.printStackTrace();
            return new ArrayList<User>();
        }
        return followedUsers;
    }


    public List<User> getFollowers(User user) {
        List<User> followingUsers;
        try {
            followingUsers = userDAO.getFollowers(user);
        } catch (ActionNotCompletedException ancEx) {
            ancEx.printStackTrace();
            return new ArrayList<User>();
        }
        return followingUsers;
    }


    public boolean follows(User followed) {
        return userDAO.isFollowedBy(followed, loggedUser);
    }


    public boolean isFollowedBy(User following) {
        return userDAO.isFollowedBy(loggedUser, following);
    }


    public void follow(User userToBeFollowed) throws ActionNotCompletedException {
        userDAO.followUser(loggedUser, userToBeFollowed);
    }

    public void unfollow(User userToBeUnfollowed) throws ActionNotCompletedException {userDAO.unfollowUser(loggedUser, userToBeUnfollowed);}

    public List<String> getTopFavouriteGenres(int limit) {
        List<String> favouriteGenres;
        try {
            favouriteGenres = userDAO.getFavouriteGenres(limit);
        } catch (ActionNotCompletedException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
        return favouriteGenres;
    }

    //--------------------------SONG-------------------------------------------------------------------

    public List<Song> getHotSongs() {
        List<Song> hotSongs;
        try {
            hotSongs = songDAO.getHotSongs(40);
        } catch (ActionNotCompletedException ancEx) {
            ancEx.printStackTrace();
            return new ArrayList<Song>();
        }
        return hotSongs;
    }

    public boolean isLikedSong(Song song){return userDAO.userLikesSong(loggedUser, song);}


    public boolean isFavouriteSong(Song song){
        return playlistDAO.isSongFavourite(loggedUser, song);
    }


    public void addSongToFavourites(Song song) throws ActionNotCompletedException {playlistDAO.addSongToFavourite(loggedUser, song);}


    public void removeSongFromFavourites(Song song) throws ActionNotCompletedException {playlistDAO.deleteSongFromFavourite(loggedUser, song);}


    public Song getSongById(Song song){
        return songDAO.getSongById(song.getID());
    }


    public void likeSong(Song song) throws ActionNotCompletedException {userDAO.likeSong(loggedUser, song);}


    public void deleteLike(Song song) throws ActionNotCompletedException {userDAO.deleteLike(loggedUser, song);}


    public List<Song> filterSong(String partialInput, String attributeField) throws ActionNotCompletedException {

        if(attributeField.equals("Title"))
            return songDAO.getSongsByPartialTitle(partialInput);
        else if(attributeField.equals("Artist"))
            return songDAO.getSongsByPartialArtist(partialInput);
        else
            return songDAO.getSongsByPartialAlbum(partialInput);
    }

    public List<Pair<String, Integer>> getTopArtists(int hitThreshold, int limit) {
        List<Pair<String, Integer>> topArtists;
        try {
            topArtists = songDAO.findArtistsWithMostNumberOfHit(hitThreshold, limit);
        } catch (ActionNotCompletedException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
        return topArtists;
    }

    public List<Pair<Integer, Pair<Album, Double>>> getTopAlbumForDecade() {
        List<Pair<Integer, Pair<Album, Double>>> topAlbums;
        try {
            topAlbums = songDAO.findTopRatedAlbumPerDecade();
        } catch (ActionNotCompletedException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
        return topAlbums;
    }

    //-----------------PLAYLIST-------------------------------------------------------------------

    public void addSong(Playlist playlist, Song song) throws ActionNotCompletedException { playlistDAO.addSong(playlist, song);}

    public void addSongToFavourite(User user, Song song) throws ActionNotCompletedException {playlistDAO.addSongToFavourite(user, song);}

    public void createPlaylist(Playlist playlist) throws ActionNotCompletedException {playlistDAO.createPlaylist(playlist);}

    public List<Playlist> getSuggestedPlaylists() {
        List<Playlist> suggestedPlaylists;
        try {
            suggestedPlaylists = playlistDAO.getSuggestedPlaylists(loggedUser);
        } catch (ActionNotCompletedException e) {
            return new ArrayList<Playlist>();
        }
        return suggestedPlaylists;
    }

    public Playlist getPlaylistById(String ID) throws ActionNotCompletedException{return playlistDAO.getPlaylist(ID);}

    public List<Song> getPlaylistSongs(Playlist playlist){
        List<Song> result = null;

        try {
            result = playlistDAO.getAllSongs(playlist);
        } catch (ActionNotCompletedException e) {
            e.printStackTrace();
        }
        return result;
    }


    public List<Playlist> getUserPlaylists() {return getUserPlaylists(loggedUser);}

    public List<Playlist> getUserPlaylists(User user) {
        List<Playlist> playlistList = new ArrayList<>();
        try {
            playlistList = userDAO.getAllPlaylist(user);
            playlistList.addAll(userDAO.getFollowedPlaylist(user));
        } catch (ActionNotCompletedException e) {
            e.printStackTrace();
            return new ArrayList<Playlist>();
        }
        return playlistList;
    }


    public boolean isFollowingPlaylist(Playlist playlist) {return userDAO.isFollowingPlaylist(loggedUser, playlist);}


    public void followPlaylist(Playlist playlist) throws ActionNotCompletedException {userDAO.followPlaylist(loggedUser, playlist);}


    public void unfollowPlaylist(Playlist playlist) throws ActionNotCompletedException {userDAO.unfollowPlaylist(loggedUser, playlist);}
}
