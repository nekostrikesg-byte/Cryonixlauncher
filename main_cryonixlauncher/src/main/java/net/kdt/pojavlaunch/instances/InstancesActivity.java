package net.kdt.pojavlaunch.instances;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cryonix.launcher.R;

import net.kdt.pojavlaunch.value.launcherprofiles.LauncherProfiles;
import net.kdt.pojavlaunch.value.launcherprofiles.MinecraftProfile;

import java.util.Map;

/** Landscape profile browser. This screen only presents the launcher's existing profiles. */
public class InstancesActivity extends Activity {
    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.screen_instances);

        View back = findViewById(R.id.instances_back);
        back.setOnClickListener(v -> finish());
        findViewById(R.id.instances_add).setOnClickListener(v ->
                startActivity(new Intent(this, NewInstanceActivity.class)));

        LinearLayout list = findViewById(R.id.instances_list);
        TextView empty = findViewById(R.id.instances_empty);
        try {
            Map<String, MinecraftProfile> profiles = LauncherProfiles.mainProfileJson == null
                    ? null : LauncherProfiles.mainProfileJson.profiles;
            if (profiles != null && !profiles.isEmpty()) {
                empty.setVisibility(View.GONE);
                for (Map.Entry<String, MinecraftProfile> entry : profiles.entrySet()) {
                    MinecraftProfile profile = entry.getValue();
                    if (profile == null) continue;
                    String name = profile.name == null || profile.name.trim().isEmpty() ? "Minecraft profile" : profile.name;
                    String version = profile.lastVersionId == null ? "Version not selected" : profile.lastVersionId;
                    LinearLayout card = new LinearLayout(this);
                    card.setOrientation(LinearLayout.VERTICAL);
                    card.setGravity(Gravity.CENTER_VERTICAL);
                    card.setPadding(dp(16), dp(10), dp(16), dp(10));
                    card.setBackgroundResource(R.drawable.bg_cryonix_row);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, dp(66));
                    params.bottomMargin = dp(7);
                    list.addView(card, params);

                    TextView title = new TextView(this);
                    title.setText(name);
                    title.setTextColor(getColor(R.color.cryonix_text));
                    title.setTextSize(14);
                    title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
                    card.addView(title);
                    TextView detail = new TextView(this);
                    detail.setText("Minecraft Java  ·  " + version + "     ›");
                    detail.setTextColor(getColor(R.color.cryonix_text_secondary));
                    detail.setTextSize(10);
                    LinearLayout.LayoutParams detailParams = new LinearLayout.LayoutParams(-1, -2);
                    detailParams.topMargin = dp(3);
                    card.addView(detail, detailParams);
                    card.setOnClickListener(v -> {
                        Intent intent = new Intent(this, InstanceDetailActivity.class);
                        intent.putExtra(InstanceDetailActivity.EXTRA_NAME, name);
                        intent.putExtra(InstanceDetailActivity.EXTRA_VERSION, version);
                        startActivity(intent);
                    });
                }
            }
        } catch (RuntimeException ignored) {
            // The empty state stays visible when profile data is not available yet.
        }
    }

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
