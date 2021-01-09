package it.unipi.dii.inginf.lsmdb.unimusic.databasesPopulation;

import it.unipi.dii.inginf.lsmdb.unimusic.middleware.dao.SongDAOImpl;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.Album;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.Song;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.exception.ActionNotCompletedException;
import org.bson.types.ObjectId;
import org.json.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Class that takes care of performing all actions related to scraping.
 * It is used to fill databases with all the information needed about songs.
 */
public class MusicScraper {

    public static void main(String[] args) throws ActionNotCompletedException {
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

                if (songToInsert.getYoutubeMediaURL() == null || songToInsert.getSpotifyMediaURL() == null){
                    miss++;
                    continue;
                }


            }catch (JSONException ex){
                miss++;
                continue;
            }

            songToInsert.setID(new ObjectId().toString());


            StringBuffer responseSpotify = getResponse("https://api.spotify.com/v1/tracks/" + uriSpotify, spotifyBearer);

            if(responseSpotify == null) {
                System.out.println("Spotify response missed, check the spotify bearer if the problem persists!");
                miss++;
                continue;
            }

            double spotifyRating = new JSONObject(responseSpotify.toString()).getInt("popularity");


            double youtubeRating = ScraperUtil.getPopularity(songToInsert.getYoutubeMediaURL());

            double rating = spotifyRating * 0.7 + youtubeRating * 0.3;

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
                }catch(JSONException ignored){}
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
            } catch (JSONException ignored) {
            }

            try{
                int year = Integer.parseInt(song.getString("release_date").split("-")[0]);
                songToInsert.setReleaseYear(year);
            } catch (JSONException ignored) {
            }

            new SongDAOImpl().createSong(songToInsert);

        }
        System.out.format("Missed mandatory field: %d\tMissed Url: %s\tIndex: %d", miss, noResponse, i);
    }


    /**
     * @param inputUrl url of the interested source.
     * @param bearer authorization key.
     * @return The response from the given API, specified by inputUrl, using the bearer to authentication.
     */
    private static StringBuffer getResponse(String inputUrl, String bearer){
        StringBuffer response = new StringBuffer();
        try {
            URL url = new URL(inputUrl);
            HttpURLConnection conn;

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