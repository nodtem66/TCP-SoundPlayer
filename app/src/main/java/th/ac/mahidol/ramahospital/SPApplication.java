package th.ac.mahidol.ramahospital;

import android.app.Application;

import timber.log.Timber;

public class SPApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        if(BuildConfig.DEBUG){
            Timber.plant(new Timber.DebugTree());
        }
    }
}
