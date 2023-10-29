package invaders;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ConfigReader {
    private static ConfigReader instance = null; // Singleton instance
    private JSONObject gameInfo;
    private JSONObject playerInfo;
    private JSONArray bunkersInfo;
    private JSONArray enemiesInfo;

    private Map<String, JSONObject> gameConfigs = new HashMap<>();

    private ConfigReader() {
    }


    public static ConfigReader getInstance() {
        if (instance == null) {
            instance = new ConfigReader();
        }
        return instance;
    }

    public void parse(String difficulty, String configPath) {
        if (!gameConfigs.containsKey(difficulty)) {
            JSONParser parser = new JSONParser();
            try {
                JSONObject configObject = (JSONObject) parser.parse(new FileReader(configPath));
                gameConfigs.put(difficulty, configObject);
                // Reading game section
                gameInfo = (JSONObject) configObject.get("Game");

                // Reading player section
                playerInfo = (JSONObject) configObject.get("Player");

                // Reading bunker section
                bunkersInfo = (JSONArray) configObject.get("Bunkers");

                // Reading enemies section
                enemiesInfo = (JSONArray) configObject.get("Enemies");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public JSONObject getGameInfo(String difficulty) {
        JSONObject configObject = gameConfigs.get(difficulty);
        if (configObject != null) {
            return (JSONObject) configObject.get("Game");
        }
        return null;
    }


    public JSONObject getPlayerInfo(String difficulty) {
        JSONObject configObject = gameConfigs.get(difficulty);
        if (configObject != null) {
            return (JSONObject) configObject.get("Player");
        }
        return null;
    }

    public JSONArray getBunkersInfo(String difficulty) {
        JSONObject configObject = gameConfigs.get(difficulty);
        if (configObject != null) {
            return (JSONArray) configObject.get("Bunkers");
        }
        return null;
    }

    public JSONArray getEnemiesInfo(String difficulty) {
        JSONObject configObject = gameConfigs.get(difficulty);
        if (configObject != null) {
            return (JSONArray) configObject.get("Enemies");
        }
        return null;
    }

}
