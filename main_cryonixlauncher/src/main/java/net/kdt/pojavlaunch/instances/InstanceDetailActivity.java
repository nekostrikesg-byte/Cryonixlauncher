package net.kdt.pojavlaunch.instances;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.cryonix.launcher.R;

/** Read-only overview for an existing Minecraft profile. */
public class InstanceDetailActivity extends Activity {
    public static final String EXTRA_NAME = "instance_name";
    public static final String EXTRA_VERSION = "instance_version";

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.screen_instance_detail);
        String name = getIntent().getStringExtra(EXTRA_NAME);
        String version = getIntent().getStringExtra(EXTRA_VERSION);
        TextView title = findViewById(R.id.instance_detail_title);
        TextView summary = findViewById(R.id.instance_detail_summary);
        title.setText(name == null ? "Instance" : name);
        summary.setText("Minecraft Java Edition\n\nVersion   " + (version == null ? "—" : version)
                + "\nProfile   Existing launcher profile\n\nThis overview is read-only. Your current game profile and launch flow are unchanged.");
        findViewById(R.id.instance_detail_back).setOnClickListener(v -> finish());
        android.view.View.OnClickListener readOnlyAction = v ->
                android.widget.Toast.makeText(this, "Profile actions continue to use the existing launcher flow.", android.widget.Toast.LENGTH_SHORT).show();
        findViewById(R.id.instance_detail_launch).setOnClickListener(readOnlyAction);
        findViewById(R.id.instance_detail_edit).setOnClickListener(readOnlyAction);
        findViewById(R.id.instance_detail_duplicate).setOnClickListener(readOnlyAction);
        findViewById(R.id.instance_detail_servers).setOnClickListener(readOnlyAction);
        findViewById(R.id.instance_detail_delete).setOnClickListener(readOnlyAction);
    }
}
