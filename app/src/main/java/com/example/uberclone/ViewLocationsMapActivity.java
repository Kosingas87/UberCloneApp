package com.example.uberclone;

import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ViewLocationsMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Button btnRide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_locations_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        btnRide = findViewById(R.id.btnRide);
        btnRide.setText("I want to give " + getIntent().getStringExtra("rUsername") + " a ride!");
        btnRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                FancyToast.makeText(ViewLocationsMapActivity.this, getIntent().getStringExtra("rUsername"),FancyToast.LENGTH_SHORT,FancyToast.INFO,true).show();
                ParseQuery<ParseObject> carRequestQuery=ParseQuery.getQuery("RequestCar");
                carRequestQuery.whereEqualTo("username", getIntent().getStringExtra("rUsername"));
                carRequestQuery.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> objects, ParseException e) {
                        if(objects.size() > 0 && e == null) {
                            for(ParseObject uberRequest: objects){
                                uberRequest.put("driverOfUser", ParseUser.getCurrentUser().getUsername());
                                uberRequest.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        Intent googleIntent= new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?saddr="+ getIntent().getDoubleExtra("dLatitude",0)+","+getIntent().getDoubleExtra("dLongitude",0)+"&"+"daddr="+getIntent().getDoubleExtra("pLatitude",0)+","+getIntent().getDoubleExtra("pLongitude",0)));
                                        FancyToast.makeText(ViewLocationsMapActivity.this, "Ride of " +getIntent().getStringExtra("rUsername")+" accepted by "+ParseUser.getCurrentUser().getUsername(),FancyToast.LENGTH_SHORT,FancyToast.INFO,true).show();
                                        startActivity(googleIntent);
                                    }
                                });

                            }
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
//        File file = new File("C:\\!!! DOCUMENTS\\!Outsourcing\\AppStore\\logo.png");
//        Bitmap bit = BitmapFactory.decodeFile(String.valueOf(file));
        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
        LatLng dLocation = new LatLng(getIntent().getDoubleExtra("dLatitude", 0), getIntent().getDoubleExtra("dLongitude", 0));
////////        mMap.addMarker(new MarkerOptions().position(sydney).title("Driver"));
////////        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        LatLng pLocation = new LatLng(getIntent().getDoubleExtra("pLatitude", 0), getIntent().getDoubleExtra("pLongitude", 0));
//        mMap.addMarker(new MarkerOptions().position(pLocation).title("Passenger Location").icon(BitmapDescriptorFactory.fromResource(R.drawable.logo)));
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pLocation,15));
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        Marker driverMarker = mMap.addMarker(new MarkerOptions().position(dLocation).title("Driver Location").icon(BitmapDescriptorFactory.fromResource(R.drawable.logo)));
        Marker passengerMarker = mMap.addMarker(new MarkerOptions().position(pLocation).title("Passenger Location").icon(BitmapDescriptorFactory.fromResource(R.drawable.logo1)));
        ArrayList<Marker> myMarkers = new ArrayList<>();
        myMarkers.add(driverMarker);
        myMarkers.add(passengerMarker);
        for (Marker marker : myMarkers) {
            builder.include(marker.getPosition());
        }
        LatLngBounds bounds = builder.build();

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 0);
        mMap.animateCamera(cameraUpdate);


    }
}
