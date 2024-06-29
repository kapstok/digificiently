package be.allersma.digificiently;

import android.app.Application;
import android.content.Context;

public class DigiFiciently extends Application {
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        DigiFiciently.context = getApplicationContext();
    }

    public static Context getContext() {
        return DigiFiciently.context;
    }
}
