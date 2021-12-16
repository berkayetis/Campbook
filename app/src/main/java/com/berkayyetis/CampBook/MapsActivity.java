package com.berkayyetis.CampBook;

import static com.berkayyetis.CampBook.DetailsActivity.artId;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.berkayyetis.CampBook.databinding.ActivityMapsBinding;
import com.google.android.material.snackbar.Snackbar;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    ActivityResultLauncher<String> permissionLauncher;
    LocationManager locationManager;
    LocationListener locationListener;
    Double selectedLatitude;
    Double selectedLongitude;
    SharedPreferences sharedPreferences;
    boolean trackBoolean;
    SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = this.openOrCreateDatabase("CampDatabase",MODE_PRIVATE,null);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        registerLauncher();

        sharedPreferences = MapsActivity.this.getSharedPreferences("com.berkayyetis.CampBook",MODE_PRIVATE);
        trackBoolean = false;

        selectedLatitude = 0.0;
        selectedLongitude= 0.0;

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);
        Intent intent = getIntent();
        String info = intent.getStringExtra("info");

        if (info.equals("new")) {

            //binding.saveButton.setVisibility(View.VISIBLE);
            //binding.deleteButton.setVisibility(View.GONE);

            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    trackBoolean = sharedPreferences.getBoolean("trackBoolean",false);

                    if(!trackBoolean) {
                        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
                        sharedPreferences.edit().putBoolean("trackBoolean",true).apply();
                    }
                }
            };

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //request permission
                if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    Snackbar.make(binding.getRoot(),"Permission needed for gallery", Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                        }
                    }).show();

                } else {
                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);

                }

            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);

                Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (lastLocation != null) {
                    LatLng lastUserLocation = new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,15));
                }

                mMap.setMyLocationEnabled(true);

            }
        } else {
            //Sqlite data && intent data
            mMap.clear();
            /*placeFromMain = (Place) intent.getSerializableExtra("place");
            LatLng latLng = new LatLng(placeFromMain.latitude,placeFromMain.longitude);

            mMap.addMarker(new MarkerOptions().position(latLng).title(placeFromMain.name));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));

            binding.placeNameText.setText(placeFromMain.name);
            binding.saveButton.setVisibility(View.GONE);
            binding.deleteButton.setVisibility(View.VISIBLE);*/

            try {
                Cursor cursor = database.rawQuery("SELECT * FROM camps WHERE id = ?",new String[] {String.valueOf(artId)});
                int langitude = cursor.getColumnIndex("latitude");
                int longitude = cursor.getColumnIndex("longitude");
                int artNameIx =cursor.getColumnIndex("campname");
                while (cursor.moveToNext()) {
                    LatLng latLng = new LatLng(Double.parseDouble(cursor.getString(langitude)), Double.parseDouble(cursor.getString(longitude)));
                    mMap.addMarker(new MarkerOptions().position(latLng).title(cursor.getString(artNameIx)));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));
                    //setTitle(cursor.getString(artNameIx));
                }
                cursor.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
}

    @Override
    public void onMapLongClick(@NonNull LatLng latLng) {
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(latLng).title("Selected Location"));

        selectedLatitude = latLng.latitude;
        selectedLongitude = latLng.longitude;

       //binding.saveButton.setEnabled(true);
    }

    private void registerLauncher() {
        permissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
                    @Override
                    public void onActivityResult(Boolean result) {
                        if(result) {
                            //permission granted
                            if (ContextCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                                Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                if (lastLocation != null) {
                                    LatLng lastUserLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation, 15));
                                }
                            }

                        } else {
                            //permission denied
                            Toast.makeText(MapsActivity.this,"Permisson needed!",Toast.LENGTH_LONG).show();
                        }
                    }

                });
    }


    public void saveClicked(View view){

        try {

            database = this.openOrCreateDatabase("CampDatabase",MODE_PRIVATE,null);

            ContentValues cv = new ContentValues();
            cv.put("latitude",selectedLatitude);
            cv.put("longitude",selectedLongitude);

            database.update("camps",cv, "id=?", new String[]{artId.toString()});
            Intent intent = new Intent(MapsActivity.this,MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);


        } catch (Exception e) {

        }

        Intent intent = new Intent(MapsActivity.this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}