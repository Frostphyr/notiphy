package com.frostphyr.notiphy;

import androidx.multidex.MultiDexApplication;

import com.frostphyr.notiphy.io.TokenUpdateWorker;
import com.frostphyr.notiphy.settings.Setting;
import com.google.android.gms.ads.MobileAds;

public class NotiphyApplication extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();

        MobileAds.initialize(getApplicationContext());
        TokenUpdateWorker.schedule(getApplicationContext());
        Setting.init(getApplicationContext());
    }

}
