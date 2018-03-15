package com.ahnelson.csce4623.mycamera;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.FragmentTransaction;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;

import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends AppCompatActivity implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener, OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private static final String LOGTAG = "MainActivity";
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private String mCurrentPhotoPath;
    private MapFragment mMapFragment;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private ImageView mImageView;
    static final int REQUEST_TAKE_PHOTO = 1;
    private static final String CAMERA_FP_AUTHORITY = "com.ahnelson.csce4623.mycamera.fileprovider";

    private LocationManager mLocationManager;
    SQLiteDatabase sqliteDB;


    /**
     * dispatchTakePictureIntent() -- Start the camera Intent
     *
     */
    private void dispatchTakePictureIntent() {
        //Create an Intent to use the default Camera Application
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
                //saveToDB(photoFile.getAbsolutePath());
            } catch (IOException ex) {
                Log.e(LOGTAG, ex.getMessage());
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                //Use the FileProvider defined in the Manifest as the authority for sharing across the Intent
                //Provides a content:// URI instead of a file:// URI which throws an error post API 24
                Uri photoURI = FileProvider.getUriForFile(this, CAMERA_FP_AUTHORITY, photoFile);
                //Put the content:// URI as the output location for the photo
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                takePictureIntent.putExtra("imageURL", photoFile.getAbsolutePath());
                //Start the Camera Application for a result
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    /**
     * Create a file to place the Image taken by the photo
     * Associate with a Timestamp so that the filename will be unique
     * @return
     * @throws IOException
     */
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        //Use ExternalStoragePublicDirectory so that it is accessible for the MediaScanner
        //Associate the directory with your application by adding an additional subdirectory
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyCamera");
        if (!storageDir.exists()) {
            storageDir.mkdir();
        }
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        Log.d(LOGTAG, "Storage Directory: " + storageDir.getAbsolutePath());
        return image;
    }

    /**
     * Boolean function to check if permissions are granted
     * If not, create an activity to request permissions
     * @return
     */
    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(LOGTAG, "Permission is granted");
                return true;
            } else {

                Log.v(LOGTAG, "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(LOGTAG, "Permission is granted");
            return true;
        }
    }

    public boolean checkLocationPermissionGranted() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Access Location")
                        .setMessage("You must have location enabled in order to associate images with locations")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Now lets connect to the API
        mGoogleApiClient.connect();
    }

    /**
     * onCreate callback.
     * Set up all private instances and check for external write permissions
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //This function returns a boolean for if you have permissions
        isStoragePermissionGranted();
        checkLocationPermissionGranted();

        sqliteDB = new SQLiteDBHelper(this).getWritableDatabase();
        //this.deleteDatabase("photo.db");

        //Set the mImageView private member to associate with the ImageView widget on the MainLayout
        //mImageView = (ImageView)findViewById(R.id.ivPicture);
        //Associate the TakePicture button with the dispatchTakePictureIntent Function
        findViewById(R.id.btnTakePicture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        mMapFragment = MapFragment.newInstance();
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.container_wrapper, mMapFragment);
        fragmentTransaction.commit();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                // The next two lines tell the new client that “this” current class will handle connection stuff
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                //fourth line adds the LocationServices API endpoint from GooglePlayServices
                .addApi(LocationServices.API)
                .build();

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds

        mLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        mMapFragment.getMapAsync(this);
    }

    /**
     * galleryAddPic()
     * This function puts the photo from mCurrentPhotoPath and allows the Media Gallery to access it
     * Photo must be in publicly accessible location
     */
    private void galleryAddPic() {

        //Fire off an Intent to use the MediaScanner
        //Place the URI of the file as the data
        File f = new File(mCurrentPhotoPath);
        Intent myMediaIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        myMediaIntent.setData(Uri.fromFile(f));
        getApplicationContext().sendBroadcast(myMediaIntent);
    }

    /**
     * onActivityResult callback fires after startActivityForResult finishes the new activity
     * @param requestCode -- Int associated with the activity request for switching activity
     * @param resultCode -- Result value from the startedActivity
     * @param data -- Return Values
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Intent intent = getIntent();
        //If the Request is to take a photo and it returned OK
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            //Extras does not contain bitmap if Image is saved to a file
            //Bundle extras = data.getExtras();
            //System.out.println(extras);
            //Create a BitmapFactoryOptions object to get Bitmap from a file
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            //Load the Bitmap from the Image file created by the camera
            Bitmap imageBitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, options);
            //Set the ImageView to the bitmap
            //mImageView.setImageBitmap(imageBitmap);
            //Add the photo to the gallery
            galleryAddPic();
            saveToDB(mCurrentPhotoPath);
        }
    }

    private void saveToDB(String url) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (location == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, (LocationListener) this);

        } else {
            //If everything went fine lets get latitude and longitude
            double currentLatitude = location.getLatitude();
            double currentLongitude = location.getLongitude();

            //set values to the current values and insert into db
            ContentValues values = new ContentValues();
            values.put(SQLiteDBHelper.PHOTO_COLUMN_URL, url);
            values.put(SQLiteDBHelper.PHOTO_COLUMN_LAT, currentLatitude);
            values.put(SQLiteDBHelper.PHOTO_COLUMN_LON, currentLongitude);
            long newRowId = sqliteDB.insert(SQLiteDBHelper.PHOTO_TABLE_NAME, null, values);

            System.out.println(url);
            readFromDB();
        }
    }

    private void readFromDB() {

        //create a cursor and query the sqlite db
        Cursor cursor = sqliteDB.query(SQLiteDBHelper.PHOTO_TABLE_NAME, new String[] {"_id", "latitude", "longitude", "url"},
                null, null, null, null, null);

        //iterate through the cursor to read all of the values
        while(cursor.moveToNext()) {

            int latIndex = cursor.getColumnIndex("latitude");
            int longIndex = cursor.getColumnIndex("longitude");
            int urlIndex = cursor.getColumnIndex("url");

            //create a marker at the latitude and longitude retrieved from the db
            LatLng latLng = new LatLng(cursor.getDouble(latIndex), cursor.getDouble(longIndex));
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .snippet(cursor.getString(urlIndex)));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.v(LOGTAG, "Permission: " + permissions[0] + "was " + grantResults[0]);
            //resume tasks needing this permission
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (location == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, (LocationListener) this);

        } else {
            //If everything went fine lets get latitude and longitude
            double currentLatitude = location.getLatitude();
            double currentLongitude = location.getLongitude();

            float zoomLevel = 16.0f; //This goes up to 21
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel));
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
/*
             * Google Play services can resolve some errors it detects.
             * If the error has a resolution, try sending an Intent to
             * start a Google Play services activity that can resolve
             * error.
             */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
                    /*
                     * Thrown if Google Play services canceled the original
                     * PendingIntent
                     */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
                /*
                 * If no resolution is available, display a dialog to the
                 * user with the error.
                 */
            Log.e("Error", "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        System.out.println("ok1");
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        //initialize the google map
        mMap = googleMap;
        mMap.setPadding(0,0,0, 200);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setOnMarkerClickListener(this);
        readFromDB();
    }

    /**
     * handle marker click event
     */
    @Override
    public boolean onMarkerClick(Marker marker) {
        // TODO Auto-generated method stub

        //When marker is clicked, get the image url from the marker snippet
        //and then display an alert dialog with an imageview inside of it
        //to display the picture associated with the marker
        ImageView image = new ImageView(this);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        //Load the Bitmap from the Image file created by the camera
        Bitmap imageBitmap = BitmapFactory.decodeFile(marker.getSnippet(), options);
        //Set the ImageView to the bitmap
        image.setImageBitmap(imageBitmap);

        AlertDialog.Builder builder =
                new AlertDialog.Builder(this).
                        setMessage("").
                        setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).
                        setView(image);
        builder.create().show();
        return false;
    }
}
