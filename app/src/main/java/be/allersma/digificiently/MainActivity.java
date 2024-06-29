package be.allersma.digificiently;

import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.RemoteException;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import be.allersma.digificiently.adapters.AppInfoAdapter;

import java.util.*;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSIONS = 1;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        textView = findViewById(R.id.txt);

        if (!hasPermissions()) {
            requestPermissions();
        } else {
//            getDataUsage();
            try {
                getTopAppsDataUsage();
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private boolean hasPermissions() {
        boolean hasPermissions = ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED;

        // Checking for USAGE_STATS permission by directing user to Usage Access settings
//        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
//        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
//                android.os.Process.myUid(), getPackageName());
//
//        return hasPermissions && mode == AppOpsManager.MODE_ALLOWED;
        return hasPermissions;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{
                        android.Manifest.permission.READ_PHONE_STATE
                },
                REQUEST_PERMISSIONS
        );

        // Directing user to usage access settings for USAGE_STATS permission
//        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
//        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (hasPermissions()) {
                    getDataUsage();
                } else {
                    Toast.makeText(this, "Usage Stats permission not granted", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "Permissions denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void getTopAppsDataUsage() throws RemoteException {
        NetworkStatsManager networkStatsManager = (NetworkStatsManager) getSystemService(Context.NETWORK_STATS_SERVICE);
        long startTime = 0; // Set this to your desired start time
        long endTime = System.currentTimeMillis(); // Current time in milliseconds

        // Create a Calendar instance
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

        // Set the date to 21st June of the current year
//        calendar.set(Calendar.MONTH, Calendar.JUNE);
        calendar.set(Calendar.DAY_OF_MONTH, 1);

        // Ensure the time is set to midnight (00:00:00)
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // Get the time in milliseconds since epoch
        long millis = calendar.getTimeInMillis();

        PackageManager pm = getPackageManager();
        List<ApplicationInfo> apps = pm.getInstalledApplications(0);

        Map<ApplicationInfo, Long> dataUsageMap = new HashMap<>();

        for (ApplicationInfo app : apps) {
            int uid = app.uid;
            NetworkStats networkStats = networkStatsManager.queryDetailsForUid(
                    NetworkCapabilities.TRANSPORT_CELLULAR,
                    null, // subscriberId is null
                    millis,
                    endTime,
                    uid
            );

            long totalBytes = 0;
            NetworkStats.Bucket bucket = new NetworkStats.Bucket();

            while (networkStats.hasNextBucket()) {
                networkStats.getNextBucket(bucket);
                totalBytes += bucket.getRxBytes() + bucket.getTxBytes();
            }

            if (totalBytes > 0) {
                dataUsageMap.put(app, totalBytes / 1000000); // Divide since we want MBs
            }
        }

        NetworkStats networkStats = networkStatsManager.queryDetails(
                NetworkCapabilities.TRANSPORT_CELLULAR,
                null,
                millis,
                endTime
        );

        long totalBytes = 0;
        NetworkStats.Bucket bucket = new NetworkStats.Bucket();

        while (networkStats.hasNextBucket()) {
            networkStats.getNextBucket(bucket);
            totalBytes += bucket.getRxBytes() + bucket.getTxBytes();
        }
        TextView totalUsage = findViewById(R.id.usage);
        String usage = String.format("%d MB", totalBytes / 1000000);
        runOnUiThread(() -> totalUsage.setText(usage));

        List<Map.Entry<ApplicationInfo, Long>> sortedDataUsageList = new ArrayList<>(dataUsageMap.entrySet());
        sortedDataUsageList.sort((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()));

//        StringBuilder topAppsData = new StringBuilder("Top 5 Apps by Data Usage:\n");
        List<Map.Entry<ApplicationInfo, Long>> topFiveApps = sortedDataUsageList.subList(
                0,
                Math.min(5, sortedDataUsageList.size())
        );
        RecyclerView recyclerView = findViewById(R.id.top_apps_usage);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        AppInfoAdapter adapter = new AppInfoAdapter(this, topFiveApps);
        recyclerView.setAdapter(adapter);
//        for (int i = 0; i < Math.min(5, sortedDataUsageList.size()); i++) {
//            Map.Entry<ApplicationInfo, Long> entry = sortedDataUsageList.get(i);
//            topAppsData.append(String.format("%s: %d mb\n", entry.getKey(), entry.getValue() / 1000000));
//        }

//        TextView textView = findViewById(R.id.txt);
//        runOnUiThread(() -> textView.setText(topAppsData.toString()));

    }

//    @RequiresApi(api = Build.VERSION_CODES.M)
    private void getDataUsage() {
        NetworkStatsManager networkStatsManager = (NetworkStatsManager) getSystemService(Context.NETWORK_STATS_SERVICE);
        long startTime = 0; // Set this to your desired start time
        long endTime = System.currentTimeMillis(); // Current time in milliseconds

        try {
            NetworkStats networkStats = networkStatsManager.querySummary(
                    ConnectivityManager.TYPE_MOBILE,
                    null, // subscriberId is null
                    startTime,
                    endTime
            );

            long totalRxBytes = 0;
            long totalTxBytes = 0;
            NetworkStats.Bucket bucket = new NetworkStats.Bucket();

            while (networkStats.hasNextBucket()) {
                networkStats.getNextBucket(bucket);
                totalRxBytes += bucket.getRxBytes();
                totalTxBytes += bucket.getTxBytes();
            }

            final long rxBytes = totalRxBytes;
            final long txBytes = totalTxBytes;

            runOnUiThread(() -> textView.setText(String.format("Received: %d bytes, Transmitted: %d bytes", rxBytes, txBytes)));

        } catch (RemoteException e) {
            e.printStackTrace();
            runOnUiThread(() -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show());
        }
    }
}
