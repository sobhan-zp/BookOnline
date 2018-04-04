package com.loper.ebook.firebase;

import android.app.Application;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.loper.ebook.R;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;


public class Analytics extends Application {

    private static FirebaseAnalytics mFirebaseAnalytics;
    @Override
    public void onCreate() {
        super.onCreate();
        this.mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/iransans.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
    }

    public static FirebaseAnalytics getFirebaseAnalytics() {
        return mFirebaseAnalytics;
    }
}