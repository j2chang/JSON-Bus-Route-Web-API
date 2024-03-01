package edu.virginia.cs.hw6;

import org.json.JSONObject;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;

public class ConfigSingleton {
    private static final String configurationFileName = "config.json";
    private static ConfigSingleton instance;
    private String busStopsURL;
    private String busLinesURL;
    private String databaseName;

    private ConfigSingleton() {
        setFieldsFromJSON();
    }

    public static ConfigSingleton getInstance() {
        if (instance == null) {
            instance = new ConfigSingleton();
        }
        return instance;
    }

    public String getBusStopsURL() {
        return busStopsURL;
    }

    public String getBusLinesURL() {
        return busLinesURL;
    }

    public String getDatabaseFilename() {
        return databaseName;
    }

    private void setFieldsFromJSON() {
        // build jsonStr from config.json
        String jsonStr = "";
        ClassLoader classLoader = getClass().getClassLoader();
        String fileName = classLoader.getResource("edu.virginia.cs.hw6/config.json").getFile();
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            while (line != null) {
                sb.append(line).append('\n');
                line = br.readLine();
            }
            jsonStr = sb.toString();
            br.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        // create a new JSONObject and set the class fields from it
        JSONObject newJSON = new JSONObject(jsonStr);
        JSONObject endpoints = newJSON.getJSONObject("endpoints");
        busStopsURL = endpoints.getString("stops");
        busLinesURL = endpoints.getString("lines");
        databaseName = newJSON.getString("database");
    }
}
