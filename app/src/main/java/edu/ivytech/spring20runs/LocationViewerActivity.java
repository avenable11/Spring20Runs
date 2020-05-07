package edu.ivytech.spring20runs;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;

public class LocationViewerActivity extends SingleFragmentActivity {
    private static final int PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int GPS_HIGH_ACCURACY_REQUEST = 2;
    /*public static final int UPDATE_INTERVAL = 5000;
    public static final int FASTEST_UPDATE_INTERVAL = 2000;

    private TextView coordinatesTextView;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;*/


    @Override
    protected Fragment createFragment() {
        return new StopwatchFragment();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // setContentView(R.layout.activity_location_viewer);
       // coordinatesTextView = findViewById(R.id.coordinatesTextView);
        /*mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_UPDATE_INTERVAL);
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                onLocationChanged(locationResult.getLastLocation());
            }
        };*/
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkLocationPermission();
    }



    private void checkLocationPermission() {
        if (Build.VERSION.SDK_INT > 22) {
            if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSION_REQUEST_ACCESS_FINE_LOCATION);
            }
            checkGPSAccuracy();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_ACCESS_FINE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //requestLocation();
                    Snackbar.make(findViewById(R.id.fragment_container),
                            R.string.location_permission_granted,Snackbar.LENGTH_SHORT).show();
                }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        final LocationSettingsStates states = LocationSettingsStates.fromIntent(data);
        switch (requestCode) {
            case GPS_HIGH_ACCURACY_REQUEST:
                switch(resultCode) {
                    case Activity.RESULT_OK:
                        //requestLocation();
                        Snackbar.make(findViewById(R.id.fragment_container),
                                R.string.high_accuracy_GPS,Snackbar.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Snackbar.make(findViewById(R.id.fragment_container),
                                R.string.no_gps, Snackbar.LENGTH_INDEFINITE)
                                .setAction("Action", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        checkGPSAccuracy();
                                    }
                                }).show();
                        break;
                }
        }
    }

    /*private void requestLocation() {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if(location != null) {
                            coordinatesTextView.setText(location.getLatitude() + "|"
                                    + location.getLongitude());
                        }
                    }
                });
        mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest,mLocationCallback, Looper.myLooper());
    }

    public void onLocationChanged(Location location) {
        coordinatesTextView.setText(location.getLatitude() + "|"
                + location.getLongitude());
    }
    */
    private void checkGPSAccuracy() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);
        Task<LocationSettingsResponse> responseTask = LocationServices.getSettingsClient(this)
                .checkLocationSettings(builder.build());
        responseTask.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                } catch (ApiException exception) {
                    switch(exception.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                ResolvableApiException resolvableApiException = (ResolvableApiException) exception;
                                resolvableApiException
                                        .startResolutionForResult(LocationViewerActivity.this,
                                                GPS_HIGH_ACCURACY_REQUEST);
                            } catch (IntentSender.SendIntentException e) {

                            } catch (ClassCastException e) {

                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            Snackbar.make(findViewById(R.id.fragment_container),
                                    R.string.no_gps, Snackbar.LENGTH_INDEFINITE).show();
                            break;
                    }

                }
            }
        });
    }
}
