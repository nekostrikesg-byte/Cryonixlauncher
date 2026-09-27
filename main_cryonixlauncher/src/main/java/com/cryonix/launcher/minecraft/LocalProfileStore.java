package com.cryonix.launcher.minecraft;

import android.content.Context;
import android.content.SharedPreferences;
import com.cryonix.launcher.minecraft.model.GameAccount;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class LocalProfileStore {
    private static final String PREFS = "cryonix_minecraft_profiles";
    private final SharedPreferences preferences;

    public LocalProfileStore(Context context) {
        preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public GameAccount create(String name) {
        String safeName = name == null ? "Player" : name.trim();
        if (safeName.isEmpty()) safeName = "Player";
        String id = UUID.nameUUIDFromBytes(
                ("cryonix:" + safeName).getBytes(StandardCharsets.UTF_8)).toString();
        preferences.edit().putString(id, safeName).apply();
        return new GameAccount(id, safeName, GameAccount.Type.LOCAL, false, false);
    }

    public void delete(String id) {
        preferences.edit().remove(id).apply();
    }
}