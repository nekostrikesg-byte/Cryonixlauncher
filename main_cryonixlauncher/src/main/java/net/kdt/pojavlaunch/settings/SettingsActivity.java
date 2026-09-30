package net.kdt.pojavlaunch.settings;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ScrollView;
import android.widget.Switch;
import android.view.WindowManager;

import com.cryonix.launcher.R;

/** Landscape settings dashboard styled for Cryonix's blue/monochrome interface. */
public class SettingsActivity extends Activity {
    private ScrollView scroll;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.screen_settings);
        scroll = findViewById(R.id.settings_scroll);
        findViewById(R.id.settings_back).setOnClickListener(v -> finish());
        findViewById(R.id.settings_home).setOnClickListener(v -> finish());
        Switch keepScreenOn = findViewById(R.id.settings_keep_screen_on);
        keepScreenOn.setOnCheckedChangeListener((button, enabled) -> {
            if (enabled) getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            else getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        });

        bindSection(R.id.settings_sidebar_renderer, R.id.settings_section_renderer);
        bindSection(R.id.settings_sidebar_game, R.id.settings_section_game);
        bindSection(R.id.settings_sidebar_controls, R.id.settings_section_controls);
        bindSection(R.id.settings_sidebar_launcher, R.id.settings_section_launcher);
        bindSection(R.id.settings_sidebar_java, R.id.settings_section_java);
        bindSection(R.id.settings_overview_general, R.id.settings_section_renderer);
        bindSection(R.id.settings_overview_game, R.id.settings_section_game);
        bindSection(R.id.settings_overview_display, R.id.settings_section_gamepad);
        bindSection(R.id.settings_overview_controls, R.id.settings_section_controls);
        bindSection(R.id.settings_overview_advanced, R.id.settings_section_launcher);
    }

    private void bindSection(int controlId, int targetId) {
        View control = findViewById(controlId);
        View target = findViewById(targetId);
        if (control != null && target != null) {
            control.setOnClickListener(v -> scroll.smoothScrollTo(0, target.getTop()));
        }
    }
}
