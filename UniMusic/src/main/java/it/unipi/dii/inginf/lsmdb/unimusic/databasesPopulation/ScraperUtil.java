package it.unipi.dii.inginf.lsmdb.unimusic.databasesPopulation;

import java.io.IOException;
import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class ScraperUtil {
    public static void main(String[] args){
        System.out.println(getGenre("https://genius.com/Aretha-franklin-respect-lyrics"));
    }
    public static String getGenre(String url){
        Document doc = null;
        String genre = null;
        String base = "primary&quot;:true,&quot;url&quot;:&quot;https://genius.com/tags/";
        boolean endParsing = false;
        try {
            doc = Jsoup.connect(url).userAgent("Mozilla/4.0").ignoreHttpErrors(true).followRedirects(true).timeout(100000).ignoreContentType(true).get();;
            String html = doc.html(); //gets all the html of the page
            //cut all the html before the genre
            int indexBase = html.indexOf(base);
            if (indexBase == -1) {
                base = "primaryTag\\\":{\\\"url\\\":\\\"https://genius.com/tags/";
                indexBase = html.indexOf(base);
                if (indexBase == -1)
                    return null;
            }

            int startIndex = html.indexOf(base) + base.length();
            String newHtml = html.substring(startIndex); //string in format: genre& or genre\....
            //parsing the string
            for (int i = 0; !endParsing && i < newHtml.length(); i++){
                if (newHtml.charAt(i) == '&' || newHtml.charAt(i) == '\\') {
                    genre = newHtml.substring(0, i);
                    endParsing = true;
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        //********************************************************************************************
        //********************************************************************************************
        //ROBA ILLEGALE DA ELIMINARE
        //********************************************************************************************
        String[] rapGenres = {"rap", "underground hip-hop", "trap", "alternative hip-hop"};
        String[] rockGenres = {"rock", "soft-rock", "hard-rock", "alternative-rock"};
        String[] popGenres = {"pop", "indie-pop", "pop-rock", "dance"};
        Random random = new Random();
        int n = random.nextInt(10);
        int index = 0;

        if (n < 4)
            index = 0;
        if (n >= 4 && n < 6)
            index = 1;
        if (n >= 6 && n < 8)
            index = 2;
        if (n >= 8)
            index = 3;

        if(genre == null)
            genre = "pop";

        switch (genre) {
            case "rap":
                genre = rapGenres[index];
                break;
            case "rock":
                genre = rockGenres[index];
                break;
            case "pop":
                genre = popGenres[index];
                break;
            default:
                break;
        }
        //***************************************************************************
        return genre;
    }


    public static double getPopularity(String url){
        String tooltip = null;
        Document doc = null;
        double result = -1;
        try {
            doc = Jsoup.connect(url).get();
            String html = doc.html(); //prendo tutto l'html della pagina

            //elimino l'html precedente al documento json sentimentBarRenderer
            int startIndex = html.indexOf("sentimentBarRenderer"); //alternativa: ytInitialData
            //se non trovo la stringa ritorno -1
            if (startIndex == -1)
                return -1;

            String newHtml = html.substring(startIndex);

            String jsonString = getCurlyBracketsContent(newHtml); //prendo la stringa json dell'oggetto sentimentBarRenderer
            //System.out.println(jsonString);
            JSONObject json = null;
            try {
                json = new JSONObject(jsonString);
                tooltip = json.getString("tooltip");
                //tooltip è in formato like / dislike, effettuo il parsing per dividere la stringa
                String likeStr, dislikeStr;
                double like, dislike;
                int slashPosition = tooltip.indexOf("/");
                likeStr = tooltip.substring(0, slashPosition - 1).replace(".", "");
                dislikeStr = tooltip.substring(slashPosition + 2).replace(".", "");


                like = Double.parseDouble(likeStr);
                dislike = Double.parseDouble(dislikeStr);

                if (like == 0)
                    return 0;
                if (dislike == 0)
                    return 100;

                result = (like / (like + dislike)) * 100;

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return result;

    }


    //ritorna la stringa contenente il primo oggetto json contenuto nella stringa str
    private static String getCurlyBracketsContent(String str){
        boolean countStarted = false, countEnded = false;
        int brackets = 0; //count delle parentesi graffe non chiuse
        int start = 0, end = str.length();

        for (int i = 0; !countEnded && i < str.length(); i++){
            //se il parsing è iniziato tengo conto delle parentesi graffe aperte
            if (countStarted){
                if (str.charAt(i) == '{')
                    brackets++;
                if (str.charAt(i) == '}')
                    brackets--;

                //se le parentesi aperte restano zero allora il parsing può terminare
                if (brackets == 0){
                    end = i + 1;
                    countEnded = true;
                }

            }
            //controllo se trovo la prima parentesi graffa per iniziare il parsing
            if (!countStarted && str.charAt(i) == '{'){
                start = i;
                countStarted = true;
                brackets = 1;
            }
        }
        return str.substring(start, end);
    }
}
