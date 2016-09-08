package com.ahmedadeltito.retrofitmultiplerequests;

import android.app.Application;

import com.ahmedadeltito.retrofitmultiplerequests.internetconnectivity.ConnectivityReceiver;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by Ahmed Adel on 9/7/16.
 */
public class MainApplication extends Application {

    private static MainApplication singleton;

    private Realm realm;

    public static MainApplication getInstance() {
        if (singleton == null) {
            singleton = new MainApplication();
            return singleton;
        } else {
            return singleton;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public Realm getRealm() {
        RealmConfiguration realmConfig = new RealmConfiguration
                .Builder(this)
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(realmConfig);
        return Realm.getDefaultInstance();
    }

    public void setConnectivityListener(ConnectivityReceiver.ConnectivityReceiverListener listener) {
        ConnectivityReceiver.connectivityReceiverListener = listener;
    }
}
