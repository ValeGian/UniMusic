package it.unipi.dii.inginf.lsmdb.unimusic.scraping;

import com.mongodb.client.*;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

public class MusicScraper {

    public static void main(String[] args) {

        int miss = 0;
        int noResponse = 0;
        int i;

        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");

        MongoDatabase uniMusicDb = mongoClient.getDatabase("UniMusic");
        MongoCollection<Document> songCollection = uniMusicDb.getCollection("songs");
        MongoCollection<Document> urlCollection = uniMusicDb.getCollection("songUrls");

        // Sending get request
        for(i = 1; i < 1000000; i++) {

            StringBuffer responseGenius = getResponse("https://api.genius.com/songs/" + i, " Yfr3zMge1KSmUXSrHkp9BeT8nxcm_kPfJUUa4TvrNyjjL2HHKLPS88Atx1mfdPLr");
            if(responseGenius == null){
                noResponse++;
                continue;
            }

            JSONObject song;
            ArrayList<String> artists = new ArrayList<>();
            String title = "", releaseDate = "", albumImageUrl = "", albumTitle = "", artist = "";
            String [] urls = new String[4];
            int popularity = 0;

            try {
                song = new JSONObject(responseGenius.toString()).getJSONObject("response").getJSONObject("song");

                title = song.getString("full_title");
                urls[2] = song.getString("url");
                artist = song.getJSONObject("primary_artist").getString("name");
                JSONArray media = song.getJSONArray("media");

                for (int iter = 0; iter < media.length(); iter++) {
                    String provider = media.getJSONObject(iter).getString("provider");
                    if (provider.equals("youtube"))
                        urls[0] = media.getJSONObject(iter).getString("url");

                    else if (provider.equals("spotify")) {
                        urls[1] = media.getJSONObject(iter).getString("url");
                        urls[3] = media.getJSONObject(iter).getString("native_uri").split(":")[2];

                    }
                }
                if (urls[0] == null || urls[1] == null){
                    miss++;
                    continue;
                }
                // printing result from response
                System.out.format("Response:-\tAlbum: %s\tTitle: %s\tindex: %s\n\n", albumTitle, title, i);

            }catch (JSONException ex){
                miss++;
                continue;
            }

            StringBuffer responseSpotify = getResponse("https://api.spotify.com/v1/tracks/" + urls[3], " BQC7CQWPeMUafZM23nxtktD0_xdBRS5nQA6ANiFsDCpsSlJPImBfdl32c8zf_NXGeDwNxZcIPQdb3I183gYzeFuNlmpgc4dXYxNobKdqW9DO8nEqnFA9IhzZhd5Zhgr5CK7EEYsU6Lt5CzVhfzHHWgfjRp2nSio");
            if(responseSpotify == null) {
                miss++;
                System.out.println(urls[3]);
                continue;
            }


            try{
                albumImageUrl = song.getString("song_art_image_url");
            } catch (JSONException e) {
                albumImageUrl = null;
            }

            try{
                albumTitle = song.getJSONObject("album").getString("full_title");
            } catch (JSONException e) {
                albumTitle = null;
            }


            ObjectId songId = new ObjectId();
            Document songDocument = new Document("_id", songId)
                    .append("title", title);

            Document albumDocument = null;
            if(albumTitle != null)
                albumDocument = new Document("title", albumTitle);

            if(albumImageUrl != null){
                if(albumDocument == null)
                    albumDocument = new Document("image", albumImageUrl);
                else
                    albumDocument.append("image", albumImageUrl);
            }

            if(albumDocument != null)
                songDocument.append("album", albumDocument);

            songDocument.append("artist", artist);

            JSONArray featuredArtists;
            try{
                featuredArtists = song.getJSONArray("featured_artists");
                if(featuredArtists.length() == 0)
                    artists = null;
                else {
                    for (int iter = 0; iter < featuredArtists.length(); iter++)
                        artists.add(featuredArtists.getJSONObject(iter).getString("name"));
                    songDocument.append("featuredArtists", artists);
                }
            } catch (JSONException e) {

            }

            try{
                releaseDate = song.getString("release_date");
                songDocument.append("releaseDate", releaseDate);
            } catch (JSONException e) {

            }

            try{
                popularity = new JSONObject(responseSpotify.toString()).getInt("popularity");
                songDocument.append("popularity", popularity);
            }catch (JSONException ex){

            }

            songDocument.append("media", Arrays.asList(
                    new Document("provider", "youtube")
                            .append("url", urls[0]),
                    new Document("provider", "spotify")
                            .append("url", urls[1]),
                    new Document("provider", "genius")
                            .append("url", urls[2])
            ));
            songCollection.insertOne(songDocument);


            Document urlDocument = new Document("_id", songId)
                    .append("youtube", urls[0])
                    .append("spotify", urls[1])
                    .append("genius", urls[2]);

            urlCollection.insertOne(urlDocument);

        }
        System.out.format("Missed mandatory field: %d\tMissed Url: %s\tIndex: %d", miss, noResponse, i);
    }



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