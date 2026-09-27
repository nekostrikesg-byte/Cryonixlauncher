package com.cryonix.launcher.minecraft;

import android.app.Activity;
import android.os.Bundle;

public final class MinecraftActivity extends Activity {
    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(com.cryonix.launcher.R.layout.screen_minecraft);
    }
}