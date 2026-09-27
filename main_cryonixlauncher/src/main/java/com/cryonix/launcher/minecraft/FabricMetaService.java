package com.cryonix.launcher.minecraft;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;

public final class FabricMetaService {
    private static final String GAME_URL = "https://meta.fabricmc.net/v2/versions/game";
    private static final String LOADER_URL = "https://meta.fabricmc.net/v2/versions/loader/";

    public List<String> fetchGameVersions() throws Exception {
        JSONArray json = getArray(GAME_URL);
        List<String> result = new ArrayList<>();
        for (int i = 0; i < json.length(); i++) {
            result.add(json.getJSONObject(i).optString("version"));
        }
        return result;
    }

    public List<String> fetchLoaderVersions(String gameVersion) throws Exception {
        JSONArray json = getArray(LOADER_URL + gameVersion);
        List<String> result = new ArrayList<>();
        for (int i = 0; i < json.length(); i++) {
            result.add(json.getJSONObject(i).getJSONObject("loader").optString("version"));
        }
        return result;
    }

    private JSONArray getArray(String endpoint) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(endpoint).openConnection();
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(15000);
        try {
            StringBuilder body = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) body.append(line);
            }
            return new JSONArray(body.toString());
        } finally {
            connection.disconnect();
        }
    }
}