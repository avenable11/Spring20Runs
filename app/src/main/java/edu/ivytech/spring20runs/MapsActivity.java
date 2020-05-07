package edu.ivytech.spring20runs;

import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import edu.ivytech.spring20runs.database.RunsDB;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, OnSuccessListener<Location> {

    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;

    private static final String TAG = "MapsActivity";

    private Button mStopwatchButton;
    private Intent mStopwatchIntent;

    private Timer mTimer;
    private static final int INTERVAL_REFERSH = 10*1000;
    private RunsDB mDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        mStopwatchButton = findViewById(R.id.buttonViewStopwatch);
        mStopwatchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(mStopwatchIntent);
            }
        });
        mStopwatchIntent = new Intent(getApplicationContext(), LocationViewerActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        mDB = RunsDB.get(getApplicationContext());
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mMap == null) {
            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        try {
            updateMap();
            mMap.animateCamera(CameraUpdateFactory.zoomTo(16.5f));
            setMapToRefresh();
        } catch (SecurityException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    public void onSuccess(Location location) {
        if (location != null) {
            setCurrentLocationMarker(location);
            displayRun();
        }
    }

    private void setCurrentLocationMarker(Location location) {
        if(mMap != null) {
            if(location != null) {
                float mapZoom = mMap.getCameraPosition().zoom;
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                .target(new LatLng(location.getLatitude(), location.getLongitude()))
                .zoom(mapZoom)
                .bearing(0)
                .tilt(25)
                .build()));
                mMap.clear();
                mMap.addMarker(new MarkerOptions()
                .position(new LatLng(location.getLatitude(), location.getLongitude()))
                .title("You are here"));
            }
        }
    }

    private void updateMap() {
        try {
            mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
            mFusedLocationProviderClient.getLastLocation().addOnSuccessListener(this,this);
        } catch (SecurityException ex) {
            Log.e(TAG, ex.getMessage());
        }
    }

    private void setMapToRefresh() {
        mTimer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                MapsActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateMap();
                    }
                });
            }
        };
        mTimer.schedule(task, INTERVAL_REFERSH, INTERVAL_REFERSH);
    }

    private void displayRun() {
        if(mMap != null) {
            PolylineOptions polyLine = new PolylineOptions();
            ArrayList<Location> list = mDB.getLocations();
            if(list.size() > 0) {
                for(Location l : list) {
                    LatLng point = new LatLng(l.getLatitude(), l.getLongitude());
                    polyLine.add(point);
                }
            }
            polyLine.width(10);
            polyLine.color(Color.RED);
            mMap.addPolyline(polyLine);
        }
    }
}
