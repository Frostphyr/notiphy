package com.frostphyr.notiphy;

import androidx.multidex.MultiDexApplication;

import com.frostphyr.notiphy.io.TokenUpdateWorker;
import com.frostphyr.notiphy.settings.Setting;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;

public class NotiphyApplication extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseApp.initializeApp(getApplicationContext());
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance());

        MobileAds.initialize(getApplicationContext());
        TokenUpdateWorker.schedule(getApplicationContext());
        Setting.init(getApplicationContext());
    }

}
