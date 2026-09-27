package com.cryonix.launcher.minecraft;

import com.cryonix.launcher.minecraft.model.MinecraftVersion;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public final class VersionManifestService {
    public static final String MANIFEST_URL =
            "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json";

    public List<MinecraftVersion> fetchVersions() throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(MANIFEST_URL).openConnection();
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(15000);
        connection.setRequestMethod("GET");

        StringBuilder body = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) body.append(line);
        } finally {
            connection.disconnect();
        }

        JSONArray versions = new JSONObject(body.toString()).getJSONArray("versions");
        List<MinecraftVersion> result = new ArrayList<>();
        for (int i = 0; i < versions.length(); i++) {
            JSONObject item = versions.getJSONObject(i);
            result.add(new MinecraftVersion(
                    item.optString("id"),
                    item.optString("type"),
                    item.optString("url"),
                    item.optString("sha1")));
        }
        return result;
    }
}