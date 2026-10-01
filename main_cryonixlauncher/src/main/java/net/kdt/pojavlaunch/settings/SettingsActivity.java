package net.kdt.pojavlaunch.settings;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/** Compatibility entry point: all settings launch into the Cryonix settings hub. */
public class SettingsActivity extends Activity {
    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        startActivity(new Intent(this, com.cryonix.launcher.settings.SettingsActivity.class));
        finish();
    }
}
