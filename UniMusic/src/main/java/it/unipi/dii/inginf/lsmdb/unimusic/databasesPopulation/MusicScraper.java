package it.unipi.dii.inginf.lsmdb.unimusic.databasesPopulation;

import com.mongodb.ConnectionString;
import com.mongodb.client.*;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.dao.SongDAOImpl;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.Album;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.Song;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.exception.ActionNotCompletedException;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;

/**
 * Class that takes care of performing all actions related to scraping.
 * It is used to fill databases with all the information needed about songs.
 */
public class MusicScraper {

    public static void main(String[] args) throws ActionNotCompletedException {

        /*
        MongoClient client = MongoClients.create(new ConnectionString("mongodb://localhost:27017"));
        // connect with the database
        MongoDatabase mongoDB = client.getDatabase("UniMusic");
        MongoCollection songs = mongoDB.getCollection("songs");

        int i = 0;
        try (MongoCursor<Document> cursor = songs.find().iterator()){
            while(cursor.hasNext()) {
                String jsonSong = cursor.next().toJson();
                Song song = new Song(jsonSong);
                try {
                    new SongDAOImpl().createSong(song);
                }catch (ActionNotCompletedException ex){
                    ex.printStackTrace();
                    continue;
                }catch(NullPointerException ex){
                    System.out.println(jsonSong);
                    break;
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }


        }
        DA FAR PARTIRE COSI COME E'*/

        populateWithSong(530001, 534000, "BQBAdzoql0K9SxXMvmATIaEPdXWmMLL4o35BgbSzs7AGlFJwSZ-nv1aYe4wSqtC-hbVgLwLWJPrJ_oEhMPnzxpaxPNe_NP-Z_48AVgkGle8eHbIYkcOMlJ79m6EZeEqJEOaPwAalCI4IsCkGsircI52TRol4VR6mpsRISWcMNA");
    }


    private static void populateWithSong(int startId, int endId, String spotifyBearer) throws ActionNotCompletedException {

        int miss = 0;
        int noResponse = 0;
        int i;

        for(i = startId; i < endId; i++) {

            StringBuffer responseGenius = getResponse(
                    "https://api.genius.com/songs/" + i, "Yfr3zMge1KSmUXSrHkp9BeT8nxcm_kPfJUUa4TvrNyjjL2HHKLPS88Atx1mfdPLr");
            if(responseGenius == null){
                noResponse++;
                continue;
            }

            JSONObject song;
            Song songToInsert = new Song();
            Album songAlbum = new Album();
            ArrayList<String> artists = new ArrayList<>();
            String uriSpotify = "";

            try {
                song = new JSONObject(responseGenius.toString()).getJSONObject("response").getJSONObject("song");

                songToInsert.setTitle(song.getString("title"));
                songToInsert.setGeniusMediaURL(song.getString("url"));
                songToInsert.setArtist(song.getJSONObject("primary_artist").getString("name"));

                JSONArray media = song.getJSONArray("media");

                for (int iter = 0; iter < media.length(); iter++) {
                    String provider = media.getJSONObject(iter).getString("provider");
                    if (provider.equals("youtube"))
                        songToInsert.setYoutubeMediaURL(media.getJSONObject(iter).getString("url"));

                    else if (provider.equals("spotify")) {
                        songToInsert.setSpotifyMediaURL(media.getJSONObject(iter).getString("url"));
                        uriSpotify = media.getJSONObject(iter).getString("native_uri").split(":")[2];
                    }
                }
                /* VERSIONE BUONA
                if (songToInsert.getYoutubeMediaURL() == null || songToInsert.getSpotifyMediaURL() == null){
                    miss++;
                    continue;
                }
                 */

                //VERSION DA CANCELLARE
                if (songToInsert.getYoutubeMediaURL() == null){
                    songToInsert.setYoutubeMediaURL(getYoutubeUrl());
                }
                if(songToInsert.getSpotifyMediaURL() == null){
                    songToInsert.setSpotifyMediaURL(getSpotifyUrl());
                }

            }catch (JSONException ex){
                miss++;
                continue;
            }

            songToInsert.setID(new ObjectId().toString());

            /*
            StringBuffer responseSpotify = getResponse("https://api.spotify.com/v1/tracks/" + uriSpotify, spotifyBearer);

            if(responseSpotify == null) {
                System.out.println("Spotify response missed, check the spotify bearer if the problem persists!");
                miss++;
                continue;
            }

            double spotifyRating = new JSONObject(responseSpotify.toString()).getInt("popularity");


            double youtubeRating = ScraperUtil.getPopularity(songToInsert.getYoutubeMediaURL());

            double rating = spotifyRating * 0.7 + youtubeRating * 0.3;
*/
            double rating = new Random().nextInt(50) + 50;
            songToInsert.setRating(rating);

            try{
                songAlbum.setImage(song.getJSONObject("album").getString("cover_art_url"));
            } catch (JSONException e) {
                songAlbum.setImage("");
            }

            try{
                songAlbum.setTitle(song.getJSONObject("album").getString("name"));
            } catch (JSONException e) {
                try {
                    songAlbum.setTitle(song.getJSONObject("album").getString("full_title"));
                }catch(JSONException ex){}
            }

            System.out.format("Response:-\tAlbum: %s\tTitle: %s\tindex: %s\n\n", songAlbum.getImage(), songToInsert.getTitle(), i);

            songToInsert.setAlbum(songAlbum);
            songToInsert.setGenre(ScraperUtil.getGenre(songToInsert.getGeniusMediaURL()));
            JSONArray featuredArtists;
            try{
                featuredArtists = song.getJSONArray("featured_artists");
                if(featuredArtists.length() != 0) {
                    for (int iter = 0; iter < featuredArtists.length(); iter++)
                        artists.add(featuredArtists.getJSONObject(iter).getString("name"));

                    songToInsert.setFeaturedArtists(artists);
                }
            } catch (JSONException e) {
            }

            try{
                int year = Integer.parseInt(song.getString("release_date").split("-")[0]);
                songToInsert.setReleaseYear(year);
            } catch (JSONException e) {
            }

            new SongDAOImpl().createSong(songToInsert);

        }
        System.out.format("Missed mandatory field: %d\tMissed Url: %s\tIndex: %d", miss, noResponse, i);
    }

    private static String getYoutubeUrl() {
        String fakeUrl = "http://www.youtube.com/watch?v=";
        Random generator = new Random();
        for(int j = 0; j < 11; j++){
            int type = generator.nextInt(2);
            if(type == 0)
                fakeUrl += (char) (generator.nextInt(26) + 'a');
            else
                fakeUrl += (char) (generator.nextInt(26) + 'A');
        }
        return fakeUrl;
    }

    private static String getSpotifyUrl() {
        String fakeUrl = "https://open.spotify.com/track/";
        Random generator = new Random();
        for(int j = 0; j < 22; j++){
            int type = generator.nextInt(3);
            if(type == 0)
                fakeUrl += (char) (generator.nextInt(26) + 'a');
            else if(type == 1)
                fakeUrl += (char) (generator.nextInt(26) + 'A');
            else
                fakeUrl +=  (generator.nextInt(10));
        }
        return fakeUrl;
    }


    /**
     * @param inputUrl
     * @param bearer
     * @return The response from the given API, specified by inputUrl, using the bearer to authentication.
     */
    private static StringBuffer getResponse(String inputUrl, String bearer){
        StringBuffer response = new StringBuffer();
        try {
            URL url = new URL(inputUrl);
            HttpURLConnection conn = null;

            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Authorization", "Bearer " + bearer);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestMethod("GET");

            if(conn.getResponseCode() == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String output;

                while ((output = in.readLine()) != null)
                    response.append(output);

                in.close();
            }else {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return response;
    }

}