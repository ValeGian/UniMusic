package it.unipi.dii.inginf.lsmdb.unimusic.middleware.log;

import it.unipi.dii.inginf.lsmdb.unimusic.middleware.dao.PlaylistDAOImpl;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.dao.SongDAOImpl;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.dao.UserDAOImpl;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class UMLogger {
    private static UMLogger umLogger = new UMLogger();

    private static final String LOG_FILE = "log4j.properties";

    private static final Logger userLogger = Logger.getLogger(UserDAOImpl.class);
    private static final Logger songLogger = Logger.getLogger(SongDAOImpl.class);
    private static final Logger playlistLogger = Logger.getLogger(PlaylistDAOImpl.class);

    private UMLogger() {
        try {
            Properties loggerProperties = new Properties();
            loggerProperties.load(new FileReader(LOG_FILE));
            PropertyConfigurator.configure(loggerProperties);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Logger getUserLogger(){
        return userLogger;
    }

    public static Logger getSongLogger(){
        return songLogger;
    }

    public static Logger getPlaylistLogger(){
        return playlistLogger;
    }
}
