package com.wildcatbank;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.content.Intent;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Hashtable;

public class LocateActivity extends Activity implements AccountTabs.OnFragmentInteractionListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleMap map;
    private GoogleApiClient mGoogleApiClient;
    private Location currentLocation;
    private Marker marker;
    private Hashtable bankMarkers;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locate);


    }

    private void initializeMap() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
        }
        if (map == null){
            map = ((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap();
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_COARSE);
            criteria.setCostAllowed(false);

            LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
            String provider = lm.getBestProvider(criteria, false);
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
            try {
                lm.requestLocationUpdates(provider, 200, 1, (LocationListener) this);
            } catch (Exception e){

            }

        }
        if (map != null) {
            map.setMyLocationEnabled(true);
            map.getUiSettings().setZoomControlsEnabled(true);
            map.getUiSettings().setCompassEnabled(true);
            map.getUiSettings().setMyLocationButtonEnabled(true);
            map.getUiSettings().setRotateGesturesEnabled(true);
            MapsInitializer.initialize(this);


        }
        if (mGoogleApiClient == null){
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addOnConnectionFailedListener(this)
                    .addConnectionCallbacks(this)
                    .build();
        }



    }
    public void onResume () {
        super.onResume();
        try {
            // Loading map
            initializeMap();
            mGoogleApiClient.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }
    @Override
    public void onAccountTabFragmentInteraction(Uri uri) {

    }
    public void selectAddressButton(View view) {
        TextView addressText = (TextView)findViewById(R.id.editText);
        addressText.setEnabled(true);
    }
    public void selectLocationButton(View view) {
        TextView addressText = (TextView)findViewById(R.id.editText);
        addressText.setEnabled(false);

    }

    @Override
    public void onConnected(Bundle bundle) {
        Location location = map.getMyLocation();
        if (location != null){
            Log.d("location", location.toString());
        }


    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
    public void onLocationChanged(Location location){
         if ((currentLocation == null) || (currentLocation.getLatitude() != location.getLatitude() || currentLocation.getLongitude() != location.getLongitude()) ){
            if (marker != null){
                marker.remove();
            }
            marker = map.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title("Marker"));
            currentLocation = location;
            updateMapCamera();
        }

    }

    /**
     * update this to have pretend addresses for bank locations
     */
    private void updateMapCamera(){
        if (marker != null) {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(marker.getPosition());
            LatLngBounds bounds = builder.build();
            int padding = 250;
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            map.animateCamera(cu);
        }
    }
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("Latitude","status");
    }
    @Override
    public void onProviderDisabled(String provider) {
        Log.d("Latitude","disable");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("Latitude","enable");
    }
}

