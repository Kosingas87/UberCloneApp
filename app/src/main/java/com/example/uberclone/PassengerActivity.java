package com.example.uberclone;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.LogOutCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.util.List;

public class PassengerActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener {
    private Button btnRequestCar;
    private GoogleMap mMap;

    private LocationManager locationManager;
    private LocationListener locationListener;
    private boolean isUberCancelled= true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        btnRequestCar=findViewById(R.id.btnRequestACar);
        btnRequestCar.setOnClickListener(PassengerActivity.this);

        ParseQuery<ParseObject> carRequestQuery=ParseQuery.getQuery("RequestCar");
        carRequestQuery.whereEqualTo("username",ParseUser.getCurrentUser().getUsername());
        carRequestQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
             if(objects.size() > 0 && e==null){
                isUberCancelled= false;
                btnRequestCar.setText("Cancel your uber request");
             }
            }
        });
        findViewById(R.id.btnLogOutFromPassengerActivity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseUser.logOutInBackground(new LogOutCallback() {
                    @Override
                    public void done(ParseException e) {
                        if(e==null){
                            finish();
                        }
                    }
                });

            }
        });
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
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener= new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                updateCameraPassengerLocation(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        if(Build.VERSION.SDK_INT<23) {
//            if(ContextCompat.checkSelfPermission(PassengerActivity.this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
//            }
        } else if(Build.VERSION.SDK_INT>=23){
            if(ContextCompat.checkSelfPermission(PassengerActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(PassengerActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1000);
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                Location currentPassengerLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                updateCameraPassengerLocation(currentPassengerLocation);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 1000 && grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(PassengerActivity.this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                Location currentPassengerLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                updateCameraPassengerLocation(currentPassengerLocation);
            }
        }
    }
    private void updateCameraPassengerLocation(Location location){
        LatLng passengerLocation= new LatLng(location.getLatitude(), location.getLongitude());
        mMap.clear();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(passengerLocation, 20));
        mMap.addMarker(new MarkerOptions().position(passengerLocation).title("Your location"));
    }

    @Override
    public void onClick(View view) {
        if(isUberCancelled == true){

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                Location passengerCurrentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (passengerCurrentLocation != null) {
                    ParseObject requestCar = new ParseObject("RequestCar");
                    requestCar.put("username", ParseUser.getCurrentUser().getUsername());

                    ParseGeoPoint userLocation = new ParseGeoPoint(passengerCurrentLocation.getLatitude(), passengerCurrentLocation.getLongitude());
                    requestCar.put("passengerLocation", userLocation);
                    requestCar.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {

                                FancyToast.makeText(PassengerActivity.this, "Car request is sent", FancyToast.LENGTH_SHORT, FancyToast.INFO, true).show();

                                btnRequestCar.setText("Cancel your uber order");
                                isUberCancelled=false;
                            }
                        }
                    });


                } else {
                    FancyToast.makeText(this, "Unknown error, something went wrong", FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show();

                }
            }
        } else{

            ParseQuery<ParseObject> carRequestQuery=ParseQuery.getQuery("RequestCar");
            carRequestQuery.whereEqualTo("username",ParseUser.getCurrentUser().getUsername());
            carRequestQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> requestList, ParseException e) {
                    if(requestList.size()>0 && e==null){
                        isUberCancelled= true;
                        btnRequestCar.setText("Request a new uber");
                        for(ParseObject uberRequest : requestList){
                            uberRequest.deleteInBackground(new DeleteCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null) {
                                        FancyToast.makeText(PassengerActivity.this,"Request/s deleted",FancyToast.LENGTH_SHORT,FancyToast.INFO,true ).show();
                                    }
                                }
                            });
                        }
                    }
                }
            });

        }
    }
}
