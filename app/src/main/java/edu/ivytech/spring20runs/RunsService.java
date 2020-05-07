package edu.ivytech.spring20runs;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import edu.ivytech.spring20runs.database.RunsDB;

public class RunsService extends Service {
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    public static final int UPDATE_INTERVAL = 5000;
    public static final int FASTEST_UPDATE_INTERVAL = 2000;
    private RunsDB mDB;
    private static final String TAG = "RunsService";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mDB = RunsDB.get(getApplicationContext());
        mFusedLocationProviderClient = LocationServices
                .getFusedLocationProviderClient(getApplicationContext());
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_UPDATE_INTERVAL);
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                mDB.insertLocation(locationResult.getLastLocation());
            }
        };
        try {
            mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback,
                    Looper.myLooper());
        } catch (SecurityException ex) {
            Log.e(TAG, ex.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
        super.onDestroy();

    }
}
