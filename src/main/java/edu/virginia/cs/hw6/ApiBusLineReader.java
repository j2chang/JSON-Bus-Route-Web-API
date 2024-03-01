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
import org.json.JSONObject;

public class ApiBusLineReader implements BusLineReader {
    ConfigSingleton config = ConfigSingleton.getInstance();
    String url2 = config.getBusLinesURL();
    String url3 = config.getBusStopsURL();

    @Override
    public List<BusLine> getBusLines() {
        List<BusLine> busLines = new ArrayList<>();

        try {
            URL url = new URL(url2);
            InputStream inputstream = url.openStream();
            InputStreamReader isreader = new InputStreamReader(inputstream, StandardCharsets.UTF_8);
            BufferedReader in = new BufferedReader(isreader);
            String fullstring = in.lines().collect(Collectors.joining());
            JSONObject lines = new JSONObject(fullstring);
            JSONArray linesArray = (JSONArray) lines.get("routes");

            for (int i = 0; i < linesArray.length(); i++) {
                JSONObject busLineJson = linesArray.getJSONObject(i);
                int id = busLineJson.getInt("id");
                boolean active = busLineJson.getBoolean("is_active");
                String lname = busLineJson.getString("long_name");
                String sname = busLineJson.getString("short_name");

                BusLine busLine = new BusLine(id, active, lname, sname);

                if (busLineJson.has("route")) {
                    JSONObject routeJson = busLineJson.getJSONObject("route");
                    JSONArray stopsJson = routeJson.getJSONArray("stops");
                    List<Stop> stops = new ArrayList<>();
                    for (int j = 0; j < stopsJson.length(); j++) {
                        JSONObject stopJson = stopsJson.getJSONObject(j);
                        int stopId = stopJson.getInt("id");
                        String name = stopJson.getString("name");
                        JSONArray position = (JSONArray) stopJson.get("position");
                        double lat = ((BigDecimal) position.get(0)).doubleValue();
                        double longi = ((BigDecimal) position.get(1)).doubleValue();
                        stops.add(new Stop(stopId, name, lat, longi));
                    }
                    Route route = new Route(stops);
                    busLine.setRoute(route);
                }

                busLines.add(busLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return busLines;
    }
}
