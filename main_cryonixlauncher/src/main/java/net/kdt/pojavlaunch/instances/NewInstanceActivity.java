package net.kdt.pojavlaunch.instances;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.cryonix.launcher.R;

/** Presentation-only setup form; profile persistence remains in the existing launcher flow. */
public class NewInstanceActivity extends Activity {
    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.screen_new_instance);
        fillSpinner(R.id.new_instance_version, new String[]{"Latest release", "Latest snapshot", "1.21.4", "1.20.1", "1.16.5"});
        fillSpinner(R.id.new_instance_loader, new String[]{"Vanilla", "Fabric", "Forge", "Quilt"});
        fillSpinner(R.id.new_instance_loader_version, new String[]{"Recommended", "Latest", "Select manually"});
        fillSpinner(R.id.new_instance_memory, new String[]{"2048 MB", "4096 MB", "6144 MB", "8192 MB"});
        fillSpinner(R.id.new_instance_resolution, new String[]{"System default", "1280 × 720", "1920 × 1080"});
        findViewById(R.id.new_instance_back).setOnClickListener(v -> finish());
        TextView status = findViewById(R.id.new_instance_status);
        status.setText("Setup options preview · existing profile data will not be changed here");
        findViewById(R.id.new_instance_create).setOnClickListener(v -> {
            EditText name = findViewById(R.id.new_instance_name);
            if (name.getText().toString().trim().isEmpty()) {
                name.setError("Enter an instance name");
                return;
            }
            Toast.makeText(this, "Instance setup is a UI preview; no profile data was changed.", Toast.LENGTH_LONG).show();
        });
    }

    private void fillSpinner(int id, String[] options) {
        Spinner spinner = findViewById(id);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }
}
