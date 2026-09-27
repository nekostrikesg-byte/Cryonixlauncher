package com.cryonix.launcher;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import com.cryonix.launcher.minecraft.MinecraftActivity;

public final class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(com.cryonix.launcher.R.layout.activity_launcher);

        Button minecraft = findViewById(com.cryonix.launcher.R.id.all_apps_button);
        minecraft.setText("Minecraft");
        minecraft.setOnClickListener(v ->
                startActivity(new Intent(this, MinecraftActivity.class)));
    }
}