package be.allersma.digificiently.adapters;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import be.allersma.digificiently.R;

import java.util.List;
import java.util.Map;

public class AppInfoAdapter extends RecyclerView.Adapter<AppInfoAdapter.AppInfoViewHolder> {

    private final List<Map.Entry<ApplicationInfo, Long>> appInfoList;
    private final PackageManager packageManager;

    public AppInfoAdapter(Context context, List<Map.Entry<ApplicationInfo, Long>> appInfoList) {
        this.appInfoList = appInfoList;
        this.packageManager = context.getPackageManager();
    }

    @NonNull
    @Override
    public AppInfoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.app_usage_item, parent, false);
        return new AppInfoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppInfoViewHolder holder, int position) {
        Map.Entry<ApplicationInfo, Long> appInfo = appInfoList.get(position);

        String appLabel = String.format(
                "%s\n%d MB",
                appInfo.getKey().loadLabel(packageManager),
                appInfo.getValue()
        );
        holder.appLabel.setText(appLabel);
        holder.packageName.setText(appInfo.getKey().packageName);
        holder.appIcon.setImageDrawable(appInfo.getKey().loadIcon(packageManager));
    }

    @Override
    public int getItemCount() {
        return appInfoList.size();
    }

    static class AppInfoViewHolder extends RecyclerView.ViewHolder {
        ImageView appIcon;
        TextView appLabel;
        TextView packageName;

        public AppInfoViewHolder(@NonNull View itemView) {
            super(itemView);
            appIcon = itemView.findViewById(R.id.app_icon);
            appLabel = itemView.findViewById(R.id.app_label);
            packageName = itemView.findViewById(R.id.package_name);
        }
    }
}
