package be.allersma.digificiently;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionActivity extends AppCompatActivity {
    private static final int REQUEST_PERMISSIONS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);

        Button donePermissionsBtn = findViewById(R.id.permissions_done_btn);
        donePermissionsBtn.setOnClickListener(view -> checkPermissions());
        checkPermissions();
    }

    private void checkPermissions() {
        boolean phoneStatePermission = hasPhoneStatePermission();
        boolean appUsagePermission = hasAppUsagePermission();

        if (phoneStatePermission) {
            TextView phoneStateText = findViewById(R.id.phone_state_txt);
            Button phoneStateBtn = findViewById(R.id.phone_state_btn);

            phoneStateBtn.setVisibility(View.GONE);
            phoneStateText.setVisibility(View.GONE);
        } else {
            Button phoneStatePermissionBtn = findViewById(R.id.phone_state_btn);
            phoneStatePermissionBtn.setOnClickListener(view -> {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{
                                android.Manifest.permission.READ_PHONE_STATE
                        },
                        REQUEST_PERMISSIONS
                );
            });
        }

        if (appUsagePermission) {
            TextView appUsageText = findViewById(R.id.app_usage_txt);
            Button appUsageBtn = findViewById(R.id.app_usage_btn);

            appUsageBtn.setVisibility(View.GONE);
            appUsageText.setVisibility(View.GONE);
        } else {
            Button appUsageBtn = findViewById(R.id.app_usage_btn);
            appUsageBtn.setOnClickListener(view -> {
                // Directing user to usage access settings for USAGE_STATS permission
                Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                startActivity(intent);
            });
        }

        if (phoneStatePermission && appUsagePermission) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private boolean hasPhoneStatePermission() {
        return ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean hasAppUsagePermission() {
        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), getPackageName());

        return mode == AppOpsManager.MODE_ALLOWED;
    }
}