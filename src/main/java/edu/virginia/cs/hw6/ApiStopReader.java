package edu.virginia.cs.hw6;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ApiStopReader implements StopReader {

    @Override
    public List<Stop> getStops() {
        ConfigSingleton config = ConfigSingleton.getInstance();
        String url1 = config.getBusStopsURL();
        List<Stop> stops = new ArrayList<>();
        try {
            URL url = new URL(url1);
            InputStream inputstream = url.openStream();
            InputStreamReader isreader = new InputStreamReader(inputstream, StandardCharsets.UTF_8);
            BufferedReader in = new BufferedReader(isreader);
            String fullstring = in.lines().collect(Collectors.joining());
            JSONObject allstops = new JSONObject(fullstring);

            JSONArray stopsArray = (JSONArray) allstops.get("stops");
            for (int i = 0; i < stopsArray.length(); i++) {
                JSONObject stopJson = stopsArray.getJSONObject(i);
                int id = stopJson.getInt("id");
                String name = stopJson.getString("name");
                JSONArray position = (JSONArray) stopJson.get("position");
                double latitude = ((BigDecimal) position.get(0)).doubleValue();
                double longitude = ((BigDecimal) position.get(1)).doubleValue();
                Stop stop = new Stop(id, name, latitude, longitude);
                stops.add(stop);
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return stops;
    }
}
